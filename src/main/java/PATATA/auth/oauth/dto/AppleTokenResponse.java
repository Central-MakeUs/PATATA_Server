package PATATA.auth.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleTokenResponse(
        @JsonProperty(value = "access_token")
        @Schema(description = "애플 access_token") String accessToken,

        @JsonProperty(value = "expires_in")
        @Schema(description = "애플 토큰 만료 기한 expires_in") String expiresIn,

        @JsonProperty(value = "id_token")
        @Schema(description = "애플 id_token") String idToken,

        @JsonProperty(value = "refresh_token")
        @Schema(description = "애플 token_tyoe") String refreshToken,

        @Schema(description = "error") String error) {
}

