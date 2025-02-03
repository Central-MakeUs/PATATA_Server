package PATATA.infra.oauth.apple.client;

import PATATA.auth.oauth.dto.AppleRevokeRequest;
import PATATA.auth.oauth.dto.AppleTokenRequest;
import PATATA.auth.oauth.dto.AppleTokenResponse;
import PATATA.infra.oauth.apple.ApplePublicKeyResponse;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "appleAuthClient", url = "https://appleid.apple.com/auth")
public interface AppleAuthClient {
    @GetMapping(value = "/keys")
    ApplePublicKeyResponse getAppleAuthPublicKey();

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AppleTokenResponse findAppleToken(@RequestBody AppleTokenRequest request);

    @PostMapping(value = "/revoke", consumes = "application/x-www-form-urlencoded")
    void revoke(AppleRevokeRequest request);
}
