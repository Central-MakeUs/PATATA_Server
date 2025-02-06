package PATATA.auth.oauth.service;

import PATATA.auth.oauth.dto.*;
import PATATA.domain.member.entity.LoginType;
import PATATA.global.error.code.status.ErrorStatus;
import PATATA.global.error.exception.MemberHandler;
import PATATA.global.error.exception.OAuthHandler;
import PATATA.infra.oauth.apple.AppleClientSecretGenerator;
import PATATA.infra.oauth.apple.AppleOAuthProvider;
import PATATA.infra.oauth.apple.ApplePublicKeyGenerator;
import PATATA.infra.oauth.apple.client.AppleAuthClient;
import PATATA.auth.jwt.service.JwtService;
import PATATA.domain.member.converter.MemberConverter;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.member.service.MemberService;
import PATATA.infra.oauth.google.GoogleAuthClient;
import PATATA.infra.oauth.google.GoogleUnlinkClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final AppleAuthClient appleAuthClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleOAuthProvider appleOAuthProvider;
    private final JwtService jwtService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    private final GoogleAuthClient googleAuthClient;
    private final GoogleUnlinkClient googleUnlinkClient;
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    //애플 로그인
    @Value("${spring.social-login.provider.apple.client-id}")
    private String appleClientId;

    //구글 로그인
    @Value("${spring.social-login.provider.google.client-id.android}")
    private String androidClientId;

    @Value("${spring.social-login.provider.google.client-id.ios}")
    private String iosClientId;

    private static final String EMAIL_CLAIM = "email";

    // 애플 로그인
    @Transactional
    public LoginResponseDTO appleLogin(AppleLoginRequestDTO appleLoginRequestDto) {
        try {
            Claims claims = validateAndGetClaims(appleLoginRequestDto.getIdentityToken());
            String sub = claims.getSubject();
            String email = claims.get(EMAIL_CLAIM, String.class);
            log.info("sub: {}", sub);
            log.info("email: {}", email);


            Optional<Member> memberByEmail = memberRepository.findByEmail(email);
            if (memberByEmail.isPresent()) {
                Member member = memberByEmail.get();
                LoginType loginType = member.getLoginType();
                log.info("loginType: {}", loginType);
                if (!loginType.equals(LoginType.APPLE)) {
                    log.info("loginType: {}", loginType);
                    throw new MemberHandler("이미 " + loginType + "으로 가입한 회원입니다.");
                }
                return memberService.createToken(member);
            }

            Member member = memberRepository.save(MemberConverter.toAppleMember(sub, email));
            return memberService.createToken(member);
        }
        catch (MemberHandler e) {
            log.error("소셜 로그인 타입 불일치: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Apple 로그인 실패: ", e);
            throw new OAuthHandler(APPLE_LOGIN_FAILED);
        }
    }

    private Claims validateAndGetClaims(String identityToken) {
        Map<String, String> headers = jwtService.parseHeader(identityToken);
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers,
                appleAuthClient.getAppleAuthPublicKey());
        return jwtService.getTokenClaims(identityToken, publicKey);
    }

    @Transactional
    public LoginResponseDTO googleLogin(GoogleLoginRequestDTO googleReqDto) {
        GoogleIdToken idToken = verifyGoogleToken(googleReqDto.getIdToken());
        if (idToken == null) {
            throw new OAuthHandler(INVALID_GOOGLE_ID_TOKEN);
        }

        Payload payload = idToken.getPayload();
        String googleSub = payload.getSubject();
        String email = payload.getEmail();

        Optional<Member> memberByEmail = memberRepository.findByEmail(email);

        if (memberByEmail.isPresent()) {
            Member member = memberByEmail.get();
            LoginType loginType = member.getLoginType();
            if (!loginType.equals(LoginType.GOOGLE)) {
                throw new MemberHandler("이미 " + loginType + "으로 가입한 회원입니다.");
            }
            return memberService.createToken(member);
        }

        Member member = memberRepository.save(MemberConverter.toGoogleMember(googleSub, email));
        return memberService.createToken(member);
    }

    @PostConstruct
    public void initializeGoogleVerifier() {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        NetHttpTransport transport = new NetHttpTransport();

        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(androidClientId, iosClientId))
                .build();
    }

    // verifyGoogleToken 메서드 수정
    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            return googleIdTokenVerifier.verify(idTokenString);
        } catch (Exception e) {
            throw new OAuthHandler(TOKEN_VALIDATION_FAILED);
        }
    }

    @jakarta.transaction.Transactional
    public void appleDelete(Member member, String code) {
        try {
            String clientSecret = appleClientSecretGenerator.createClientSecret();
            String refreshToken = appleOAuthProvider.getAppleRefreshToken(code, clientSecret);
            String idToken = appleOAuthProvider.getAppleIdToken(code, clientSecret);
            Claims claims = validateAndGetClaims(idToken);
            String sub = claims.getSubject();

            // 회원 정보 일치 검사
            if (!sub.equals(member.getAppleSub())) {
                throw new MemberHandler(MEMBER_NOT_MATCH);
            }

            // 연결 끊기
            AppleRevokeRequest appleRevokeRequest = AppleRevokeRequest.builder()
                    .client_id(appleClientId)
                    .refresh_token(refreshToken)
                    .client_secret(clientSecret)
                    .token_type("REFRESH_TOKEN")
                    .build();
            appleAuthClient.revoke(appleRevokeRequest);
            memberService.deleteMember(member);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Apple Revoke Error");
        } catch (Exception e) {
            throw new MemberHandler(MEMBER_DELETE_FAILED);
        }


    }

    public void googleDelete(Member member, String googleToken) {

        try {
            GoogleAuthInfo googleAuthInfo = googleAuthClient.getGoogleInfo("Bearer " + googleToken);
            //회원 일치 검사
            if (!googleAuthInfo.getSub().equals(member.getGoogleSub())) {
                throw new MemberHandler(MEMBER_NOT_MATCH);
            }
            googleUnlinkClient.unlink(googleToken);
            memberService.deleteMember(member);
        } catch (MemberHandler e) {
            throw e;
        } catch (Exception e) {
            throw new MemberHandler(MEMBER_DELETE_FAILED);
        }
    }
}
