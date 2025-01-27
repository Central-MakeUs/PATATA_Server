package PATATA.domain.spot.repository;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Scrap;
import PATATA.domain.spot.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    List<Scrap> findByMember(Member member);

    Optional<Scrap> findBySpotAndMember(Spot spot, Member member);

    List<Scrap> findByMemberAndDeletedFalse(Member member);

    boolean existsByMemberAndSpotAndDeletedFalse(Member member, Spot spot);
}
