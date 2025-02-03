package PATATA.domain.spot.repository;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySpot(Spot spot);

    List<Review> findByMember(Member member);
}
