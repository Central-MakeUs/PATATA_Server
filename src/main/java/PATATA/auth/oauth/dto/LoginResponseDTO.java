package PATATA.auth.oauth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponseDTO {

    private String nickName;
    private String email;
    private String accessToken;
    private String refreshToken;

    @Builder
    public LoginResponseDTO(String nickName, String email, String accessToken, String refreshToken) {
        this.nickName = nickName;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

