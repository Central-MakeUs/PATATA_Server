package PATATA.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NickNameDto {

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickName;
}
