package PATATA.domain.spot.converter;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.*;
import PATATA.domain.spot.repository.*;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.SPOT_NOT_FOUND;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotConverter {

    private final SpotImageRepository spotImageRepository;
    private final ScrapRepository scrapRepository;
    private final ReviewRepository reviewRepository;
    private final SpotRepository spotRepository;
    private final SpotTagRepository spotTagRepository;

    public static Spot toEntity(SpotRequestDto.CreateRequest request, Point point, Category category, Member member) {
        return Spot.builder()
                .spotName(request.getSpotName())
                .spotDescription(request.getSpotDescription())
                .spotAddress(request.getSpotAddress())
                .spotAddressDetail(request.getSpotAddressDetail())
                .spotLocation(point)
                .spotScraps(0)
                .spotCategory(category)
                .deleted(false)
                .member(member)
                .build();
    }

    public SpotResponseDto.SearchResponse toSearchResponse(Object[] result, Member member) {
        Long spotId = (Long) result[0];
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
        Double distance = (Double) result[12];
        List<SpotImage> images = spotImageRepository.findBySpot(spot);
        String representativeImageUrl = images.stream()
                .filter(SpotImage::getIsRepresentative)
                .findFirst()
                .map(SpotImage::getImageUrl)
                .orElse(null);
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        Integer reviews = reviewRepository.findBySpot(spot).size();
        return SpotResponseDto.SearchResponse.from(
                spot, representativeImageUrl, isScraped, reviews, distance);
    }

    public SpotResponseDto.CategoryResponse toCategoryResponse(Object[] result, Member member) {
        Long spotId = (Long) result[0];
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
        List<SpotImage> images = spotImageRepository.findBySpot(spot);
        String representativeImageUrl = images.stream()
                .filter(SpotImage::getIsRepresentative)
                .findFirst()
                .map(SpotImage::getImageUrl)
                .orElse(null);
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        Integer reviews = reviewRepository.findBySpot(spot).size();
        List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
        List<String> tags = spotTags.stream()
                .map(spotTag -> spotTag.getTag().getTagName())
                .collect(Collectors.toList());
        return SpotResponseDto.CategoryResponse.from(
                spot, representativeImageUrl, isScraped, reviews, tags);

    }


}
