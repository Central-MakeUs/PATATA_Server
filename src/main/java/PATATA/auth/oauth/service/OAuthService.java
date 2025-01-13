package PATATA.auth.oauth.service;

import PATATA.global.error.exception.OAuthHandler;
import PATATA.infra.oauth.apple.ApplePublicKeyGenerator;
import PATATA.infra.oauth.apple.client.AppleAuthClient;
import PATATA.auth.oauth.dto.AppleLoginRequestDTO;
import PATATA.auth.oauth.dto.GoogleLoginRequestDTO;
import PATATA.auth.oauth.dto.LoginResponseDTO;
import PATATA.auth.jwt.service.JwtService;
import PATATA.domain.member.converter.MemberConverter;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.member.service.MemberService;
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

import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final AppleAuthClient appleAuthClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final JwtService jwtService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    //@Value("${spring.social-login.provider.apple.client-id}")
    //private String appleClientId;

    @Value("${spring.social-login.provider.google.client-id}")
    private String googleClientId;

    private static final String EMAIL_CLAIM = "email";

    // 애플 로그인
    @Transactional
    public LoginResponseDTO appleLogin(AppleLoginRequestDTO appleLoginRequestDto) {

        try{ Claims claims = validateAndGetClaims(appleLoginRequestDto.getIdentityToken());
        String sub = claims.getSubject();
        String email = claims.get(EMAIL_CLAIM, String.class);

        Member member = memberRepository.findByAppleSub(sub)
                .orElseGet(() -> memberRepository.save(
                        MemberConverter.toAppleMember(sub, email)
                ));

        return memberService.createToken(member);
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

        // Google 로그인 구현
        GoogleIdToken idToken = verifyGoogleToken(googleReqDto.getIdToken());

        if (idToken == null) {
            throw new OAuthHandler(INVALID_GOOGLE_ID_TOKEN);
        }

        Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        MemberConverter.toGoogleMember(email)
                ));

        return memberService.createToken(member);
    }

    @PostConstruct
    public void initializeGoogleVerifier() {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        NetHttpTransport transport = new NetHttpTransport();

        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
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
}
