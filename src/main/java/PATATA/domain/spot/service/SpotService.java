package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.entity.Role;
import PATATA.domain.spot.converter.SpotConverter;
import PATATA.domain.spot.dto.ScrapResponseDto;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.*;
import PATATA.domain.spot.repository.*;
import PATATA.global.error.exception.ReportHandler;
import PATATA.global.error.exception.S3ImageHandler;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;
    private final CategoryRepository categoryRepository;
    private final GeometryFactory geometryFactory;
    private final TagRepository tagRepository;
    private final SpotTagRepository spotTagRepository;
    private final SpotImageRepository spotImageRepository;
    private final ReviewRepository reviewRepository;
    private final ScrapRepository scrapRepository;
    private final ReportRepository reportRepository;
    private final S3ImageService s3Service;
    private final SpotConverter spotConverter;

    @Transactional
    public SpotResponseDto.CreateResponse createSpot(SpotRequestDto.CreateRequest requestDTO, Member member) {

        // 신고된 사용자 체크
        if (member.getRole().equals(Role.REPORTED)) {
            throw new ReportHandler(MEMBER_IS_REPORTED);
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));
        Point point = createPoint(requestDTO.getLongitude(), requestDTO.getLatitude());

        Spot spot = SpotConverter.toEntity(requestDTO, point, category, member);
        Spot savedSpot = spotRepository.save(spot);

        try {
            // 이미지 처리
            processSpotImages(requestDTO.getImages(), savedSpot);
            // 태그 처리
            processSpotTags(requestDTO.getTags(), savedSpot);
        } catch (Exception e) {
            // 이미지나 태그 처리 실패 시 롤백을 위한 예외 발생
            log.error(e.getMessage(), e);
            throw new SpotHandler(SPOT_UPLOAD_FAIL);
        }

        return SpotResponseDto.CreateResponse.from(savedSpot);
    }

    private Point createPoint(Double longitude, Double latitude) {
        try {
            Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            point.setSRID(4326);
            return point;
        } catch (Exception e) {
            throw new SpotHandler(INVALID_COORDINATES);
        }
    }

    private void processSpotImages(List<SpotRequestDto.SpotImageRequest> images, Spot spot) {
        if (images == null || images.isEmpty()) {
            throw new S3ImageHandler(IMAGE_EMPTY);
        }

        List<SpotImage> spotImages = images.stream()
                .map(imageRequest -> {
                    try {
                        String imageUrl = s3Service.upload(imageRequest.getFile(), "spot-images/").getResizedImageUrl();
                        return SpotImage.builder()
                                .spot(spot)
                                .imageUrl(imageUrl)
                                .isRepresentative(imageRequest.getIsRepresentative())
                                .sequence(imageRequest.getSequence())
                                .build();
                    } catch (Exception e) {
                        throw new SpotHandler(S3_UPLOAD_FAIL);
                    }
                })
                .collect(Collectors.toList());

        spotImageRepository.saveAll(spotImages);
    }

    private void processSpotTags(List<String> tags, Spot spot) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        tags.forEach(tagName -> {
            try {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder()
                                        .tagName(tagName)
                                        .build()
                        ));
                SpotTag spotTag = SpotTag.builder()
                        .spot(spot)
                        .tag(tag)
                        .build();
                spotTagRepository.save(spotTag);
            } catch (Exception e) {
                throw new SpotHandler(SPOT_UPLOAD_FAIL);
            }
        });
    }

    public SpotResponseDto.DetailResponse getSpotDetail(Long spotId, Member member) {
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));
        Boolean isAuthor = spot.getMember() != null && spot.getMember().getMemberId().equals(member.getMemberId());

        Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);
        List<Review> reviews = reviewRepository.findBySpotAndDeletedFalse(spot);
        List<Tag> tags = spotTagRepository.findBySpot(spot).stream()
                .map(SpotTag::getTag)
                .collect(Collectors.toList());
        List<SpotImage> spotImages = spotImageRepository.findBySpot(spot);
        return SpotResponseDto.DetailResponse.from(spot, isAuthor, isScraped, reviews, tags, spotImages, member);
    }

    @Transactional
    public SpotResponseDto.UpdateResponse updateSpot(Long spotId, SpotRequestDto.UpdateRequest request, Member member) {
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        if (!spot.getMember().getMemberId().equals(member.getMemberId())) {
            throw new SpotHandler(NO_AUTHORIZATION);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));

        spot.updateSpot(
                request.getSpotName(),
                request.getSpotDescription(),
                request.getSpotAddress(),
                request.getSpotAddressDetail(),
                category,
                createPoint(request.getLongitude(), request.getLatitude())
        );

        List<Tag> tags = request.getTags().stream()
                .map(name -> tagRepository.findByTagName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().tagName(name).build())))
                .collect(Collectors.toList());
        spot.updateTags(tags);

        return SpotResponseDto.UpdateResponse.from(spot, category);
    }

    @Transactional
    public SpotResponseDto.DeleteResponse deleteSpot(Long spotId, Member member) {
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        if (!spot.getMember().getMemberId().equals(member.getMemberId())) {
            throw new SpotHandler(NO_AUTHORIZATION);
        }

        if (spot.isDeleted()) {
            throw new SpotHandler(SPOT_NOT_FOUND);
        }

        spot.delete();
        return SpotResponseDto.DeleteResponse.of(spotId);
    }

    //스팟 검색(정렬 포함)
    public Page<SpotResponseDto.SearchResponse> searchSpotsByName(String spotName, Double latitude, Double longitude, String sortBy, Pageable pageable, Member member) {
        // 사용자 위치 Point 객체 생성
        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        if (sortBy.equals("DISTANCE")) {
            return spotRepository.findNearbySpotsWithDistance(spotName, userLocation, pageable)
                    .map(result -> spotConverter.toSearchResponse(result, member));
        } else if (sortBy.equals("RECOMMEND")) {
            return spotRepository.findBySpotNameWithDistanceOrderByScrap(spotName, userLocation, pageable)
                    .map(result -> spotConverter.toSearchResponse(result, member));
        }
        throw new SpotHandler(INVALID_SORT_TYPE);
    }

    public ScrapResponseDto.MySpotsResponseDto getMySpots(Member member) {
        List<ScrapResponseDto.SpotDto> spotDtos = spotRepository.findAllByMemberAndDeletedFalseOrderByCreatedAtDesc(member)
                .stream()
                .filter(spot -> !spot.isDeleted())
                .map(spot -> {
                    List<SpotImage> images = spotImageRepository.findBySpot(spot);
                    String representativeImageUrl = images.stream()
                            .filter(SpotImage::getIsRepresentative)
                            .findFirst()
                            .map(SpotImage::getImageUrl)
                            .orElse(null);

                    return ScrapResponseDto.SpotDto.builder()
                            .spotId(spot.getSpotId())
                            .spotName(spot.getSpotName())
                            .representativeImageUrl(representativeImageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        int totalSpots = spotDtos.size();
        return new ScrapResponseDto.MySpotsResponseDto(totalSpots, spotDtos);
    }

    public Page<SpotResponseDto.CategoryResponse> getSpotsByCategory(Long categoryId, Double latitude, Double longitude, String sortBy, Pageable pageable, Member member) {

        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));
        }
        // 사용자 위치 Point 객체 생성
        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        if (sortBy.equals("DISTANCE")) {
            return spotRepository.findByCategoryOrderByDistance(categoryId, userLocation, pageable)
                    .map(result -> spotConverter.toCategoryResponse(result, member));
        } else if (sortBy.equals("RECOMMEND")) {
            return spotRepository.findByCategoryOrderByScrap(categoryId, userLocation, pageable)
                    .map(result -> spotConverter.toCategoryResponse(result, member));
        }
        throw new SpotHandler(INVALID_SORT_TYPE);
    }


    public List<SpotResponseDto.TodaySpotResponse> getTodaySpots(Member member) {
        List<Spot> randomSpots = spotRepository.findRandomSpots(5);

        // 각 스팟을 DTO로 변환
        return randomSpots.stream()
                .map(spot -> {
                    // 대표 이미지 URL 가져오기
                    String imageUrl = spotImageRepository.findBySpot(spot).stream()
                            .filter(SpotImage::getIsRepresentative)
                            .findFirst()
                            .map(SpotImage::getImageUrl)
                            .orElse(null);

                    // 스크랩 여부 확인
                    Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);

                    // 태그 목록 가져오기
                    List<String> tags = spotTagRepository.findBySpot(spot).stream()
                            .map(spotTag -> spotTag.getTag().getTagName())
                            .collect(Collectors.toList());

                    return SpotResponseDto.TodaySpotResponse.from(spot, imageUrl, isScraped, tags);
                })
                .collect(Collectors.toList());
    }

    public List<SpotResponseDto.TodaySpotListResponse> getTodaySpotsList(Double userLatitude, Double userLongitude, Member member) {
        List<Spot> randomSpots = spotRepository.findRandomSpots(5);

        // 각 스팟을 DTO로 변환
        return randomSpots.stream()
                .map(spot -> {
                    List<SpotImage> images = spotImageRepository.findBySpot(spot);

                    Double distance = spotRepository.calculateDistance(spot.getSpotId(), userLatitude, userLongitude);

                    // 스크랩 여부 확인
                    Boolean isScraped = scrapRepository.existsByMemberAndSpotAndDeletedFalse(member, spot);

                    // 태그 목록 가져오기
                    List<String> tags = spotTagRepository.findBySpot(spot).stream()
                            .map(spotTag -> spotTag.getTag().getTagName())
                            .collect(Collectors.toList());

                    return SpotResponseDto.TodaySpotListResponse.from(spot, distance, images, isScraped, tags);
                })
                .collect(Collectors.toList());

    }
}
