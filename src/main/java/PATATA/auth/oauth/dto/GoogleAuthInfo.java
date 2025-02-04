package PATATA.auth.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleAuthInfo {
    private String email;
    private String sub;
}
