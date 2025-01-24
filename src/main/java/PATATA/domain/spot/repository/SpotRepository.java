package PATATA.domain.spot.repository;

import PATATA.domain.spot.entity.Spot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {

    @Query("SELECT s FROM Spot s WHERE s.spotId = :spotId AND s.deleted = false")
    Optional<Spot> findByIdAndDeletedFalse(@Param("spotId") Long spotId);

    Page<Spot> findBySpotNameContainingAndDeletedFalse(String spotName, Pageable pageable);


}
