package PATATA.auth.oauth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AppleTokenRequest {
    private String client_id;
    private String client_secret;
    private String authorization_code;
    private String grant_type;
}
