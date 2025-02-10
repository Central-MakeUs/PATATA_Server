package PATATA.domain.spot.service;


import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotImage;
import PATATA.domain.spot.entity.SpotTag;
import PATATA.domain.spot.repository.ScrapRepository;
import PATATA.domain.spot.repository.SpotImageRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.domain.spot.repository.SpotTagRepository;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

    public List<MapResponseDto.InBoundsResponse> getSpotsInBounds(Double minLat, Double minLng, Double maxLat, Double maxLng, Double userLat, Double userLng, Long categoryId, Boolean withSearch, Member member) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(userLng, userLat));

        int limit = withSearch ? 29 : 30;
        List<Object[]> results = spotRepository.findSpotsInBounds(minLat, minLng, maxLat, maxLng, userLocation, categoryId, limit);

        return results.stream()
                .map(result -> {
                    Long spotId = (Long) result[0];
                    Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                            .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
                    List<SpotImage> spotImages = spotImageRepository.findBySpot(spot);
//                    List<String> imageUrls = spotImages.stream()
//                            .map(SpotImage::getImageUrl)
//                            .collect(Collectors.toList());
                    String representativeImageUrl = spotImages.stream()
                            .filter(SpotImage::getIsRepresentative)
                            .findFirst()
                            .map(SpotImage::getImageUrl)
                            .orElse(null);
                    List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
                    List<String> tags = spotTags.stream()
                            .map(spotTag -> spotTag.getTag().getTagName())
                            .collect(Collectors.toList());
                    Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
                    Double distance = (Double) result[12];

                    return MapResponseDto.InBoundsResponse.from(spot, representativeImageUrl, tags, isScraped, distance);
                })
                .collect(Collectors.toList());
    }

    public MapResponseDto.InBoundsResponse getSpotSearched(String spotName, Double minLatitude, Double minLongitude, Double maxLatitude, Double maxLongitude, Double userLatitude, Double userLongitude, Member member) {
        Spot spotSearched = findSpotByConditions(spotName, minLatitude, minLongitude, maxLatitude, maxLongitude);
        List<SpotImage> spotImages = spotImageRepository.findBySpot(spotSearched);
//        List<String> imageUrls = spotImages.stream()
//                .map(SpotImage::getImageUrl)
//                .collect(Collectors.toList());
        String representativeImageUrl = spotImages.stream()
                .filter(SpotImage::getIsRepresentative)
                .findFirst()
                .map(SpotImage::getImageUrl)
                .orElse(null);
        List<SpotTag> spotTags = spotTagRepository.findBySpot(spotSearched);
        List<String> tags = spotTags.stream()
                .map(spotTag -> spotTag.getTag().getTagName())
                .collect(Collectors.toList());
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spotSearched);
        Double distance = spotRepository.calculateDistance(spotSearched.getSpotId(), userLatitude, userLongitude);
        return MapResponseDto.InBoundsResponse.from(spotSearched, representativeImageUrl, tags, isScraped, distance);
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

    public int checkSpotDensity(Double latitude, Double longitude) {
        int spotCount = spotRepository.countSpotsWithinRadius(latitude, longitude, 100.0);
        if (spotCount == 25) {
            throw new SpotHandler(TOO_MANY_SPOT);
        } else {
            return spotCount;
        }
    }
}
