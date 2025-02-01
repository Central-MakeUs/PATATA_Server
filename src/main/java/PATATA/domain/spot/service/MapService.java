package PATATA.domain.spot.service;


import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.MapRequestDto;
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

import static PATATA.global.error.code.status.ErrorStatus.SPOT_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {

    private final SpotRepository spotRepository;
    private final GeometryFactory geometryFactory;
    private final ScrapRepository scrapRepository;
    private final SpotTagRepository spotTagRepository;
    private final SpotImageRepository spotImageRepository;

    public List<MapResponseDto.InBoundsResponse> getSpotsInBounds(Double minLat, Double minLng, Double maxLat, Double maxLng, Double userLat, Double userLng, Long categoryId, Member member) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(userLng, userLat));

        List<Object[]> results = spotRepository.findSpotsInBounds(minLat, minLng, maxLat, maxLng, userLocation, categoryId);

        return results.stream()
                .map(result -> {
                    Long spotId = (Long) result[0];
                    Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                            .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
                    List<SpotImage> spotImages = spotImageRepository.findBySpot(spot);
                    List<String> imageUrls = spotImages.stream()
                            .map(SpotImage::getImageUrl)
                            .collect(Collectors.toList());
                    List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
                    List<String> tags = spotTags.stream()
                            .map(spotTag -> spotTag.getTag().getTagName())
                            .collect(Collectors.toList());                    Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
                    Boolean isAuthor = spot.getMember().getMemberId().equals(member.getMemberId());
                    Double distance = Math.round(((Number) result[12]).doubleValue() * 10.0) / 10.0;

                    return MapResponseDto.InBoundsResponse.from(spot, imageUrls, tags, isScraped, isAuthor, distance);
                })
                .collect(Collectors.toList());
    }
}
