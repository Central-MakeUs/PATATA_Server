package PATATA.domain.spot.repository;

import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpotTagRepository extends JpaRepository<SpotTag, Long> {
    List<SpotTag> findBySpot(Spot spot);
}
