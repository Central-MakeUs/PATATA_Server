package PATATA.domain.spot.repository;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpotRepositoryTest {

    @Autowired
    private SpotRepository spotRepository;

    @Test
    void testSpotLocationQuery() {
        // 테스트용 사용자 위치 생성 (예: 서울시청 좌표)
        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(
                new Coordinate(126.7567, 37.5574)  // 경도, 위도 순서
        );

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(0, 10);  // 첫 페이지, 10개 항목

        // 쿼리 실행
        Page<Object[]> results = spotRepository.findBySpotNameWithDistanceOrderByScrap(
                "",  // 빈 문자열로 모든 장소 검색
                userLocation,
                pageable
        );

        // 결과 출력
        results.getContent().forEach(row -> {
            System.out.println("=== 장소 정보 ===");
            System.out.println("원본 위치: " + row[1]);  // original_location
            System.out.println("변환된 위치: " + row[2]);  // swapped_location
            System.out.println("사용자 위치: " + row[3]);  // user_location
            System.out.println("거리(km): " + row[12]);  // distance
            System.out.println("----------------");
        });
    }

}