package PATATA.jwt.service;

import PATATA.apiPayLoad.code.status.ErrorStatus;
import PATATA.apiPayLoad.exception.ExceptionHandler;
import PATATA.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;

    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        UserDetails result = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return result;
    }
}
