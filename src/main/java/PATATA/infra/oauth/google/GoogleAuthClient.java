package PATATA.infra.oauth.google;

import PATATA.auth.oauth.dto.GoogleAuthInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "GoogleAuthClient", url = "https://www.googleapis.com/oauth2/v3")
public interface GoogleAuthClient {

    @GetMapping("/userinfo")
    GoogleAuthInfo getGoogleInfo(@RequestHeader("Authorization") String token);
}
