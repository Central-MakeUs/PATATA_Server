package PATATA.domain.spot.repository;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.entity.Spot;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {

    List<Spot> findByMember(Member member);

    @Query("SELECT s FROM Spot s WHERE s.spotId = :spotId AND s.deleted = false")
    Optional<Spot> findByIdAndDeletedFalse(@Param("spotId") Long spotId);

    //스팟 목록 검색(거리순)
    @Query(value = """
        SELECT s.*,
        ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000, 3) as distance
        FROM spot s
        WHERE s.spot_name LIKE CONCAT('%', :spotName, '%')
        AND s.deleted = false
        ORDER BY distance
        """,
            nativeQuery = true)
    Page<Object[]> findNearbySpotsWithDistance(
            @Param("spotName") String spotName,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );

    //스팟 목록 검색(추천순)
    @Query(value = """
        SELECT s.*,
        ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000, 3) as distance
        FROM spot s
        WHERE s.spot_name LIKE CONCAT('%', :spotName, '%')
        AND s.deleted = false
        ORDER BY s.spot_scraps DESC
        """,
            nativeQuery = true)
    Page<Object[]> findBySpotNameWithDistanceOrderByScrap(
            @Param("spotName") String spotName,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );

    List<Spot> findAllByMemberAndDeletedFalseOrderByCreatedAtDesc(Member member);

    //스팟 카테고리 조회(추천순)
    @Query(value = """
        SELECT s.*,
        ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000, 3) as distance
        FROM spot s
        WHERE (:categoryId IS NULL OR s.category_id = :categoryId)
        AND s.deleted = false
        ORDER BY s.spot_scraps DESC
        """,
            nativeQuery = true)
    Page<Object[]> findByCategoryOrderByScrap(
            @Param("categoryId") Long categoryId,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );

    //스팟 카테고리 조회(거리순)
    @Query(value = """
        SELECT s.*,
        ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000, 3) as distance
        FROM spot s
        WHERE (:categoryId IS NULL OR s.category_id = :categoryId)
        AND s.deleted = false
        ORDER BY distance
        """,
            nativeQuery = true)
    Page<Object[]> findByCategoryOrderByDistance(
            @Param("categoryId") Long categoryId,
            @Param("userLocation") Point userLocation,
            Pageable pageable
    );

    //지도 내 스팟 불러오기
    @Query(value = """
            SELECT s.*,
                   ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), :userLocation) / 1000, 3) as distance
            FROM spot s
            WHERE MBRContains(
                    ST_SRID(
                        ST_LineStringFromText(
                            CONCAT('LINESTRING(',
                                    :minLng, ' ', :minLat, ',',
                                    :maxLng, ' ', :maxLat, ')'
                                    )),4326
                     ), s.spot_location)
            AND (:categoryId IS NULL OR s.category_id = :categoryId)
            AND s.deleted = false
            ORDER BY s.spot_scraps DESC
            LIMIT :limit
            """
            , nativeQuery = true)
    List<Object[]> findSpotsInBounds(
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng,
            @Param("userLocation") Point userLocation,
            @Param("categoryId") Long categoryId,
            @Param("limit") Integer limit
    );

    //지도 내 스팟 검색(좌표x)
    Optional<Spot> findTopBySpotNameContainingAndDeletedFalseOrderBySpotScrapsDesc(String spotSName);

    //지도 내 스팟 검색(좌표o)
    @Query(value = """
        SELECT s.* FROM spot s
        WHERE s.spot_name LIKE CONCAT('%', :spotName, '%')
        AND MBRContains(
            ST_SRID(
                ST_LineStringFromText(
                    CONCAT('LINESTRING(',
                            :minLng, ' ', :minLat, ',',
                            :maxLng, ' ', :maxLat, ')'
                            )),4326
             ), s.spot_location)
        AND s.deleted = false
        ORDER BY s.spot_scraps DESC
        LIMIT 1
        """
            , nativeQuery = true)
    Optional<Spot> findTopInBoundsByNameOrderByScrap(
            @Param("spotName") String spotName,
            @Param("minLat") Double minLat,
            @Param("minLng") Double minLng,
            @Param("maxLat") Double maxLat,
            @Param("maxLng") Double maxLng
    );


    //스팟과 사용자 거리 계산
    @Query(value = "SELECT ROUND(ST_Distance_Sphere(Point(ST_Y(s.spot_location), ST_X(s.spot_location)), Point(:userLongitude, :userLatitude)) / 1000, 3) " +
            "FROM spot s WHERE s.spot_id = :spotId", nativeQuery = true)
    Double calculateDistance(
            @Param("spotId") Long spotId,
            @Param("userLatitude") Double userLatitude,
            @Param("userLongitude") Double userLongitude);

    //반경 내 스팟 개수
    @Query(value = """
        SELECT COUNT(*) FROM spot s
        WHERE ST_Distance_Sphere(
            Point(ST_Y(s.spot_location), ST_X(s.spot_location)),
            POINT(:longitude, :latitude)
        ) <= :radius
        AND s.deleted = false
        """, nativeQuery = true)
    Integer countSpotsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radius
    );

    @Query(value = "SELECT * FROM spot s WHERE s.deleted = false ORDER BY RAND(CAST(DATE_FORMAT(CURRENT_DATE, '%Y%m%d') AS UNSIGNED))  LIMIT :limit", nativeQuery = true)
    List<Spot> findRandomSpots(@Param("limit") int limit);
}
