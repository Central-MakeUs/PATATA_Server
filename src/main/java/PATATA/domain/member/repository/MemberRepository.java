package PATATA.domain.member.repository;

import PATATA.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByAppleSub(String sub);

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByRefreshToken(String refreshToken);

    Optional<Member> findByEmail(String email);
}
