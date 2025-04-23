package PATATA.domain.spot.converter;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.repository.MemberRepository;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.*;
import PATATA.domain.spot.repository.*;
import PATATA.domain.spot.service.MapService;
import PATATA.domain.spot.service.SpotService;
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

    public SpotResponseDto.SearchResponse toSearchResponse(Object[] result, Member member, int size) {
        Long spotId = (Long) result[0];
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
        Double distance = (Double) result[12];

        String representativeImageUrl = getRepresentativeImageUrl(spot, size);
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        Integer reviews = reviewRepository.findBySpotAndDeletedFalse(spot).size();
        return SpotResponseDto.SearchResponse.from(
                spot, representativeImageUrl, isScraped, reviews, distance);
    }

    public SpotResponseDto.CategoryResponse toCategoryResponse(Object[] result, Member member, int size) {
        Long spotId = (Long) result[0];
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(()->new SpotHandler(SPOT_NOT_FOUND));
        String representativeImageUrl = getRepresentativeImageUrl(spot, size);
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        Integer reviews = reviewRepository.findBySpotAndDeletedFalse(spot).size();
        List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
        List<String> tags = spotTags.stream()
                .map(spotTag -> spotTag.getTag().getTagName())
                .collect(Collectors.toList());
        return SpotResponseDto.CategoryResponse.from(
                spot, representativeImageUrl, isScraped, reviews, tags);

    }

    public MapResponseDto.InBoundsResponse toInBoundsResponse(Object[] result, Member member, int size) {
        Long spotId = (Long) result[0];
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));
        List<String> imageUrls = getResizedImageUrls(spot, size);
        List<SpotTag> spotTags = spotTagRepository.findBySpot(spot);
        List<String> tags = spotTags.stream()
                .map(spotTag -> spotTag.getTag().getTagName())
                .collect(Collectors.toList());
        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        Double distance = (Double) result[12];

        return MapResponseDto.InBoundsResponse.from(spot, imageUrls, tags, isScraped, distance);
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

    private String getRepresentativeImageUrl(Spot spot, int size) {
        // 대표 이미지 URL을 먼저 가져오고, 만약 없으면 null 반환
        SpotImage representativeImage = spotImageRepository.findBySpot(spot).stream()
                .filter(SpotImage::getIsRepresentative)
                .findFirst()
                .orElse(null);

        if (representativeImage == null) {
            return null;
        }

        // 리사이징된 URL 반환: size에 따라 다르게 처리
        return switch (size) {
            case 0 -> // original
                    representativeImage.getOriginalImageUrl();
            case 1 -> // 400
                    representativeImage.getResizedImageUrl400();
            case 2 -> // 800
                    representativeImage.getResizedImageUrl800();
            case 3 -> // 1200
                    representativeImage.getResizedImageUrl1200();
            default -> representativeImage.getOriginalImageUrl(); // 기본값은 original
        };
    }

}
