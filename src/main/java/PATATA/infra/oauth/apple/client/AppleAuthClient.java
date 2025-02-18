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
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "appleAuthClient", url = "https://appleid.apple.com/auth")
public interface AppleAuthClient {
    @GetMapping(value = "/keys")
    ApplePublicKeyResponse getAppleAuthPublicKey();

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AppleTokenResponse findAppleToken(@RequestPart(value = "code") String code,
                                      @RequestPart(value = "client_id") String client_id,
                                      @RequestPart(value = "client_secret") String client_secret,
                                      @RequestPart(value = "grant_type") String grant_type);

    @PostMapping(value = "/revoke", consumes = "application/x-www-form-urlencoded")
    void revoke(@RequestPart(value = "token") String token,
                @RequestPart(value = "client_id") String client_id,
                @RequestPart(value = "client_secret") String client_secret,
                @RequestPart(value = "token_type_hint") String token_type_hint);
}
