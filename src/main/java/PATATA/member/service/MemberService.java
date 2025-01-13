package PATATA.member.service;

import PATATA.apiPayLoad.exception.JwtHandler;
import PATATA.apiPayLoad.exception.MemberHandler;
import PATATA.jwt.service.JwtService;
import PATATA.member.entity.Member;
import PATATA.member.repository.MemberRepository;
import PATATA.oauth.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static PATATA.apiPayLoad.code.status.ErrorStatus.MEMBER_NOT_FOUND;
import static PATATA.apiPayLoad.code.status.ErrorStatus.REFRESH_TOKEN_UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

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
}
