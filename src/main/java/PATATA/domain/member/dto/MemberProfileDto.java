package PATATA.domain.member.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileDto {

    private Long memberId;
    private String nickName;
    private String email;
    private String profileImage;

    @Builder
    public MemberProfileDto(Long memberId, String nickName, String email, String profileImage) {
        this.memberId = memberId;
        this.nickName = nickName;
        this.email = email;
        this.profileImage = profileImage;
    }
}
