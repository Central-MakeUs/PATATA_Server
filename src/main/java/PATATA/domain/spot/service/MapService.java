package PATATA.domain.spot.service;


import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.converter.SpotConverter;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotImage;
import PATATA.domain.spot.entity.SpotTag;
import PATATA.domain.spot.repository.ScrapRepository;
import PATATA.domain.spot.repository.SpotImageRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.domain.spot.repository.SpotTagRepository;
import PATATA.global.error.exception.SpotHandler;
import PATATA.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {

    private final SpotRepository spotRepository;
    private final ScrapRepository scrapRepository;
    private final SpotTagRepository spotTagRepository;
    private final SpotImageRepository spotImageRepository;
    private final SpotConverter spotConverter;

    public Page<MapResponseDto.InBoundsResponse> getSpotsListInBounds(Double minLat, Double minLng, Double maxLat, Double maxLng, Double userLat, Double userLng, Long categoryId, Boolean withSearch, Pageable pageable, Member member, int size) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(userLng, userLat));

        int limit = withSearch ? 29 : 30;
        List<Object[]> results = spotRepository.findSpotsInBounds(minLat, minLng, maxLat, maxLng, userLocation, categoryId, limit);

        List<MapResponseDto.InBoundsResponse> content = results.stream()
                .map(result -> spotConverter.toInBoundsResponse(result, member, size))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), content.size());
        return new PageImpl<>(content.subList(start, end), pageable, content.size());
    }

    public List<MapResponseDto.InBoundsResponse> getSpotsInBounds(Double minLat, Double minLng, Double maxLat, Double maxLng, Double userLat, Double userLng, Long categoryId, Boolean withSearch, Member member, int size) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(userLng, userLat));

        int limit = withSearch ? 29 : 30;
        List<Object[]> results = spotRepository.findSpotsInBounds(minLat, minLng, maxLat, maxLng, userLocation, categoryId, limit);

        return results.stream()
                .map(result -> {
                    Long spotId = (Long) result[0];
                    Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                            .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
                    List<String> imageUrls = getResizedImageUrls(spot, size);
                    List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
                    List<String> tags = spotTags.stream()
                            .map(spotTag -> spotTag.getTag().getTagName())
                            .collect(Collectors.toList());
                    Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
                    Double distance = (Double) result[12];

                    return MapResponseDto.InBoundsResponse.from(spot, imageUrls, tags, isScraped, distance);
                })
                .collect(Collectors.toList());
    }

    public MapResponseDto.InBoundsResponse getSpotSearched(String spotName, Double minLatitude, Double minLongitude, Double maxLatitude, Double maxLongitude, Double userLatitude, Double userLongitude, Member member, int size) {
        Spot spotSearched = findSpotByConditions(spotName, minLatitude, minLongitude, maxLatitude, maxLongitude);
        List<String> imageUrls = getResizedImageUrls(spotSearched, size);
//        String representativeImageUrl = spotImages.stream()
//                .filter(SpotImage::getIsRepresentative)
//                .findFirst()
//                .map(SpotImage::getImageUrl)
//                .orElse(null);
        List<SpotTag> spotTags = spotTagRepository.findBySpot(spotSearched);
        List<String> tags = spotTags.stream()
                .map(spotTag -> spotTag.getTag().getTagName())
                .collect(Collectors.toList());
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spotSearched);
        Double distance = spotRepository.calculateDistance(spotSearched.getSpotId(), userLatitude, userLongitude);
        return MapResponseDto.InBoundsResponse.from(spotSearched, imageUrls, tags, isScraped, distance);
    }

    // 조건에 따른 스팟 검색
    private Spot findSpotByConditions(String spotName, Double minLatitude, Double minLongitude,
                                      Double maxLatitude, Double maxLongitude) {
        if (minLatitude == null || minLongitude == null || maxLatitude == null || maxLongitude == null) {
            return spotRepository.findTopBySpotNameContainingAndDeletedFalseOrderBySpotScrapsDesc(spotName)
                    .orElseThrow(() -> new SpotHandler(SPOT_CANNOT_SEARCH));
        }
        return spotRepository.findTopInBoundsByNameOrderByScrap(
                        spotName, minLatitude, minLongitude, maxLatitude, maxLongitude)
                .orElseThrow(() -> new SpotHandler(SPOT_CANNOT_SEARCH));
    }

    public List<String> getResizedImageUrls(Spot spot, int size) {
        // Spot에 관련된 모든 SpotImage 리스트 가져오기
        List<SpotImage> spotImages = spotImageRepository.findBySpot(spot);

        // 이미지 URL 리스트 생성
        return spotImages.stream()
                .map(spotImage -> {
                    // 각 이미지에 대해 리사이징된 URL 반환
                    return switch (size) {
                        case 0 -> spotImage.getOriginalImageUrl();   // original
                        case 1 -> spotImage.getResizedImageUrl400();  // 400px
                        case 2 -> spotImage.getResizedImageUrl800();  // 800px
                        case 3 -> spotImage.getResizedImageUrl1200(); // 1200px
                        default -> spotImage.getOriginalImageUrl();   // 기본값은 original
                    };
                })
                .collect(Collectors.toList());
    }
}
