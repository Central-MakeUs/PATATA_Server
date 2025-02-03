package PATATA.infra.oauth.apple;

import PATATA.auth.oauth.dto.AppleTokenRequest;
import PATATA.infra.oauth.apple.client.AppleAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleOAuthProvider {

    private final AppleAuthClient appleAuthClient;
    @Value("${spring.social-login.provider.apple.client-id}")
    private String clientId;

    public String getAppleRefreshToken(String code, String clientSecret) {
        AppleTokenRequest appleTokenRequest = AppleTokenRequest.builder()
                .client_id(clientId)
                .client_secret(clientSecret)
                .authorization_code(code)
                .grant_type("AUTHORIZATION_CODE")
                .build();

        return appleAuthClient.findAppleToken(appleTokenRequest).refreshToken();
    }
}

