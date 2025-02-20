package PATATA.domain.member.service;

import PATATA.domain.member.dto.MemberProfileDto;
import PATATA.domain.member.entity.Role;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Scrap;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.ReviewRepository;
import PATATA.domain.spot.repository.ScrapRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.domain.spot.service.S3ImageService;
import PATATA.global.error.exception.JwtHandler;
import PATATA.global.error.exception.MemberHandler;
import PATATA.auth.jwt.service.JwtService;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.auth.oauth.dto.LoginResponseDTO;
import PATATA.global.error.exception.S3ImageHandler;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final SpotRepository spotRepository;
    private final ReviewRepository reviewRepository;
    private final ScrapRepository scrapRepository;
    private final S3ImageService s3ImageService;

    //accessToken, refreshToken 발급
    @Transactional
    public LoginResponseDTO createToken(Member member) {
        String newAccessToken = jwtService.generateAccessToken(member.getMemberId());
        String newRefreshToken = jwtService.generateRefreshToken(member.getMemberId());

        // DB에 refreshToken 자동 저장
        member.updateRefreshToken(newRefreshToken);
        return new LoginResponseDTO(member.getNickName(), member.getEmail(), newAccessToken, newRefreshToken);
    }

    // refreshToken으로 accessToken 발급하기
    @Transactional
    public LoginResponseDTO regenerateAccessToken(String refreshToken) {
        // refresh token 유효성 검사
        if (!jwtService.validateTokenBoolean(refreshToken))
            throw new JwtHandler(REFRESH_TOKEN_UNAUTHORIZED);

        return memberRepository.findByRefreshToken(refreshToken)
                .filter(member -> refreshToken.equals(member.getRefreshToken()))
                .map(member -> {
                    String newRefreshToken = jwtService.generateRefreshToken(member.getMemberId());
                    String newAccessToken = jwtService.generateAccessToken(member.getMemberId());
                    member.updateRefreshToken(newRefreshToken);
                    return new LoginResponseDTO(member.getNickName(), member.getEmail(),
                            newAccessToken, newRefreshToken);
                })
                .orElseThrow(() -> new MemberHandler(MEMBER_NOT_FOUND));
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtService.validateTokenBoolean(refreshToken)) {
            throw new JwtHandler(REFRESH_TOKEN_UNAUTHORIZED);
        }

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new MemberHandler(MEMBER_NOT_FOUND));

        member.refreshTokenExpires();
    }

    @Transactional
    public void updateNickname(Member member, String newNickname) {
        String currentNickname = member.getNickName();

        if (currentNickname == null || !currentNickname.equals(newNickname)) {
            if (memberRepository.existsByNickName(newNickname)) {
                throw new MemberHandler(NICKNAME_ALREADY_EXIST);
            }
        }

        // 닉네임 업데이트
        member.updateNickname(newNickname);
        memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Member member) {
        //스팟 삭제
        deleteSpot(member);
        //리뷰 삭제
        deleteReview(member);
        //스크랩 내역 삭제
        deleteScrap(member);
        //멤버 역할 변경
        member.updateRole(Role.WITHDRAWAL);
        memberRepository.save(member);
    }

    private void deleteSpot(Member member) {
        List<Spot> spots = spotRepository.findByMember(member);
        spots.forEach(spot -> spot.setMember(null));
        spotRepository.saveAll(spots);
    }

    private void deleteReview(Member member) {
        List<Review> reviews = reviewRepository.findByMember(member);
        reviews.forEach(review -> review.setMember(null));
        reviewRepository.saveAll(reviews);
    }

    private void deleteScrap(Member member) {
        List<Scrap> scraps = scrapRepository.findByMember(member);
        scraps.forEach(Scrap::delete);
        scrapRepository.saveAll(scraps);
    }

    @Transactional
    public String updateProfileImage(Member member, MultipartFile profileImage) {
        if (profileImage.isEmpty()) {
            log.info("애초에 이미지가 첨부안됐음");
            throw new S3ImageHandler(IMAGE_EMPTY);
        }
        try {
            String imageUrl = s3ImageService.upload(profileImage);
            member.updateImage(imageUrl);
            memberRepository.save(member);
            return imageUrl;
        } catch (Exception e) {
            throw new SpotHandler(S3_UPLOAD_FAIL);
        }
    }

    public MemberProfileDto getProfile(Member member) {

        MemberProfileDto profileDto = MemberProfileDto.builder()
                .memberId(member.getMemberId())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .build();

        return profileDto;
    }
}
