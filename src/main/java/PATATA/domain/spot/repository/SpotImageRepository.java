package PATATA.domain.spot.repository;

import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpotImageRepository extends JpaRepository<SpotImage, Long> {
    List<SpotImage> findBySpot(Spot spot);
}
