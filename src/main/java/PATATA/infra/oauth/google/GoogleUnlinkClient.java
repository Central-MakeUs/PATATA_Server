package PATATA.infra.oauth.google;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "GoogleUnlinkClient", url = "https://oauth2.googleapis.com/revoke")
public interface GoogleUnlinkClient {

    @PostMapping
    @Headers("Content-Type: application/x-www-form-urlencoded")
    void unlink(@RequestParam("token") String oauthAccessToken);
}
