package PATATA.domain.member.converter;

import PATATA.domain.member.entity.LoginType;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.entity.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberConverter {
    public static Member toAppleMember(String sub, String email) {
        return Member.builder()
                .role(Role.USER)
                .email(email)
                .nickName(null) // 닉네임 가져오기
                .appleSub(sub)
                .loginType(LoginType.APPLE)
                .refreshToken("") // 초기 빈 값 설정
                .refreshTokenExpiresAt(LocalDateTime.now()) // 초기 시간 설정
                .build();
    }
    public static Member toGoogleMember(String sub, String email) {
        return Member.builder()
                .role(Role.USER)
                .email(email)
                .nickName(null)
                .googleSub(sub)
                .loginType(LoginType.GOOGLE)
                .refreshToken("") // 초기 빈 값 설정
                .refreshTokenExpiresAt(LocalDateTime.now()) // 초기 시간 설정
                .build();
    }
}
