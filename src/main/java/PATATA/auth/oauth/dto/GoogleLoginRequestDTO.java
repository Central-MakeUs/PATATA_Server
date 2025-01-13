package PATATA.auth.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleLoginRequestDTO {
    private String idToken;  // Google ID 토큰
}
