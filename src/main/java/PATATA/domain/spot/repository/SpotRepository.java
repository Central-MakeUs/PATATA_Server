package PATATA.domain.spot.repository;

import PATATA.domain.spot.entity.Spot;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {

    @Query("SELECT s FROM Spot s WHERE s.spotId = :spotId AND s.isDeleted = false")
    Optional<Spot> findByIdAndDeletedFalse(@Param("spotId") Long spotId);

    Page<Spot> findBySpotNameContainingAndDeletedFalse(String spotName, Pageable pageable);

    @Query(value = """
        SELECT s.*,
        ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000 as distance
        FROM spot s
        WHERE s.spot_name LIKE CONCAT('%', :spotName, '%')
        AND s.is_deleted = false
        ORDER BY distance
        """,
            nativeQuery = true)
    Page<Object[]> findNearbySpotsWithDistance(
            @Param("spotName") String spotName,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );

    @Query(value = """
        SELECT s.*,
        ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000 as distance
        FROM spot s
        WHERE s.spot_name LIKE CONCAT('%', :spotName, '%')
        AND s.is_deleted = false
        ORDER BY s.spot_scraps DESC
        """,
            nativeQuery = true)
    Page<Object[]> findBySpotNameWithDistanceOrderByScrap(
            @Param("spotName") String spotName,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );
}
