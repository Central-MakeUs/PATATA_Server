package PATATA.member.converter;

import PATATA.member.entity.LoginType;
import PATATA.member.entity.Member;
import PATATA.oauth.dto.AppleLoginRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberConverter {
    public static Member toAppleMember(String sub, AppleLoginRequestDTO appleLoginRequestDto) {
        return Member.builder()
                .email(appleLoginRequestDto.getEmail())
                .nickName("랜덤 생성") // 닉네임 가져오기
                .appleSub(sub)
                .loginType(LoginType.APPLE)
                .refreshToken("") // 초기 빈 값 설정
                .refreshTokenExpiresAt(LocalDateTime.now()) // 초기 시간 설정
                .build();
    }
}
