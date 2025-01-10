package PATATA.oauth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleLoginRequestDTO {

    @NotEmpty
    private String identityToken;
    private String email;

}