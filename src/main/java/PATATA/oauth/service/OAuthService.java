package PATATA.oauth.service;

import PATATA.apiPayLoad.exception.JwtHandler;
import PATATA.apiPayLoad.exception.MemberHandler;
import PATATA.jwt.service.JwtService;
import PATATA.member.converter.MemberConverter;
import PATATA.member.entity.Member;
import PATATA.member.repository.MemberRepository;
import PATATA.oauth.apple.ApplePublicKeyGenerator;
import PATATA.oauth.client.AppleAuthClient;
import PATATA.oauth.dto.AppleLoginRequestDTO;
import PATATA.oauth.dto.LoginResponseDTO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;

import static PATATA.apiPayLoad.code.status.ErrorStatus.MEMBER_NOT_FOUND;
import static PATATA.apiPayLoad.code.status.ErrorStatus.REFRESH_TOKEN_UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final AppleAuthClient appleAuthClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    //@Value("${spring.social-login.provider.apple.client-id}")
    //private String clientId;

    //accessToken, refreshToken 발급
    @Transactional
    public LoginResponseDTO createToken(Member member) {
        String newAccessToken = jwtService.generateAccessToken(member.getMemberId());
        String newRefreshToken = jwtService.generateRefreshToken(member.getMemberId());

        // DB에 refreshToken 저장
        member.updateRefreshToken(newRefreshToken);
        memberRepository.save(member);
        return new LoginResponseDTO(member.getNickName(), member.getEmail(), newAccessToken, newRefreshToken);
    }

    // refreshToken으로 accessToken 발급하기
    @Transactional
    public LoginResponseDTO regenerateAccessToken(String refreshToken) {
        // refresh token 유효성 검사
        if (!jwtService.validateTokenBoolean(refreshToken))
            throw new JwtHandler(REFRESH_TOKEN_UNAUTHORIZED);

        Optional<Member> getMember = memberRepository.findByRefreshToken(refreshToken);
        if (getMember.isEmpty())
            throw new MemberHandler(MEMBER_NOT_FOUND);

        Member member = getMember.get();
        if (!refreshToken.equals(member.getRefreshToken()))
            throw new JwtHandler(REFRESH_TOKEN_UNAUTHORIZED);

        String newRefreshToken = jwtService.generateRefreshToken(member.getMemberId());
        String newAccessToken = jwtService.generateAccessToken(member.getMemberId());

        member.updateRefreshToken(newRefreshToken);
        memberRepository.save(member);

        return new LoginResponseDTO(member.getNickName(), member.getEmail(), newAccessToken, newRefreshToken);
    }

    // 애플 로그인
    @Transactional
    public LoginResponseDTO appleLogin(AppleLoginRequestDTO appleLoginRequestDto) {

        Map<String, String> headers = jwtService.parseHeader(appleLoginRequestDto.getIdentityToken());
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, appleAuthClient.getAppleAuthPublicKey());

        Claims claims = jwtService.getTokenClaims(appleLoginRequestDto.getIdentityToken(), publicKey);
        String sub = claims.getSubject();

        Member member = memberRepository.findByAppleSub(sub).orElse(null);

        if (member == null) {
            member = memberRepository.save(
                    MemberConverter.toAppleMember(sub, appleLoginRequestDto)
            );
        }

        return createToken(member);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtService.validateTokenBoolean(refreshToken))  // refresh token 유효성 검사
            throw new JwtHandler(REFRESH_TOKEN_UNAUTHORIZED);

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new MemberHandler(MEMBER_NOT_FOUND)); // 예외처리

        member.refreshTokenExpires();
        memberRepository.save(member);
    }
}
