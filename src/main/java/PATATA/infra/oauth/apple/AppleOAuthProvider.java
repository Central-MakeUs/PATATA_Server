package PATATA.infra.oauth.apple;

import PATATA.auth.oauth.dto.AppleTokenRequest;
import PATATA.auth.oauth.dto.AppleTokenResponse;
import PATATA.infra.oauth.apple.client.AppleAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthProvider {

    private final AppleAuthClient appleAuthClient;
    @Value("${spring.social-login.provider.apple.client-id}")
    private String clientId;

    public String getAppleRefreshToken(String code, String clientSecret) {
        try {
            AppleTokenRequest appleTokenRequest = AppleTokenRequest.builder()
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .authorization_code(code)
                    .grant_type("authorization_code")
                    .build();
            log.info("Apple Token Request: {}", appleTokenRequest);

            AppleTokenResponse appleTokenResponse = appleAuthClient.findAppleToken(appleTokenRequest.getAuthorization_code(), appleTokenRequest.getClient_id(), appleTokenRequest.getClient_secret(), appleTokenRequest.getGrant_type());
            log.info("Apple Token Response: {}", appleTokenResponse);

            return appleTokenResponse.refreshToken();
        } catch (Exception e) {
            log.error("Apple OAuth 인증 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String getAppleIdToken(String code, String clientSecret) {
        AppleTokenRequest appleTokenRequest = AppleTokenRequest.builder()
                .client_id(clientId)
                .client_secret(clientSecret)
                .authorization_code(code)
                .grant_type("authorization_code")
                .build();

        AppleTokenResponse appleTokenResponse = appleAuthClient.findAppleToken(appleTokenRequest.getAuthorization_code(), appleTokenRequest.getClient_id(), appleTokenRequest.getClient_secret(), appleTokenRequest.getGrant_type());
        return appleTokenResponse.idToken();
    }
}

