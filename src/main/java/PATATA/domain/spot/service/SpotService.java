package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.converter.SpotConverter;
import PATATA.domain.spot.dto.ScrapResponseDto;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.*;
import PATATA.domain.spot.repository.*;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    private final S3ImageService s3Service;
    private final SpotConverter spotConverter;

    @Transactional
    public SpotResponseDto.CreateResponse createSpot(SpotRequestDto.CreateRequest requestDTO, Member member) {
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));
        Point point = geometryFactory.createPoint(new Coordinate(requestDTO.getLongitude(), requestDTO.getLatitude()));
        //point.setSRID(4326);

        Spot spot = SpotConverter.toEntity(requestDTO, point, category, member);
        Spot savedSpot = spotRepository.save(spot);

        if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
            List<SpotImage> spotImages = requestDTO.getImages().stream()
                    .map(imageRequest -> {
                        String imageUrl = s3Service.upload(imageRequest.getFile());
                        return SpotImage.builder()
                                .spot(savedSpot)
                                .imageUrl(imageUrl)
                                .isRepresentative(imageRequest.getIsRepresentative())
                                .sequence(imageRequest.getSequence())
                                .build();
                    })
                    .collect(Collectors.toList());
            spotImageRepository.saveAll(spotImages);
        }

        if (requestDTO.getTags() != null && !requestDTO.getTags().isEmpty()) {
            requestDTO.getTags().forEach(tagName -> {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder()
                                        .tagName(tagName)
                                        .build()
                        ));
                SpotTag spotTag = SpotTag.builder()
                        .spot(savedSpot)
                        .tag(tag)
                        .build();
                spotTagRepository.save(spotTag);
            });
        }
        return SpotResponseDto.CreateResponse.from(savedSpot);
    }

    public SpotResponseDto.DetailResponse getSpotDetail(Long spotId, Member member) {
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));
        Boolean isAuthor = spot.getMember().getMemberId().equals(member.getMemberId());
        log.info(String.valueOf(spot.getMember().getMemberId()));
        log.info(String.valueOf(member.getMemberId()));
        List<Review> reviews = reviewRepository.findBySpot(spot);
        List<Tag> tags = spotTagRepository.findBySpot(spot).stream()
                .map(SpotTag::getTag)
                .collect(Collectors.toList());
        return SpotResponseDto.DetailResponse.from(spot, isAuthor, reviews, tags);
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
                category
        );
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
            throw new SpotHandler(SPOT_ALREADY_DELETE);
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

    public List<ScrapResponseDto.SpotDto> getMySpots(Member member) {
        List<Spot> mySpots = spotRepository.findAllByMemberOrderByCreatedAtDesc(member);
        return mySpots.stream()
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
    }

    public Page<SpotResponseDto.CategoryResponse> getSpotsByCategory(Long categoryId, Double latitude, Double longitude, String sortBy, Pageable pageable, Member member) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));

        // 사용자 위치 Point 객체 생성
        GeometryFactory geometryFactory = new GeometryFactory();
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        if (sortBy.equals("DISTANCE")) {
            return spotRepository.findByCategoryOrderByDistance(category.getCategoryId(), userLocation, pageable)
                    .map(result -> spotConverter.toCategoryResponse(result, member));
        } else if (sortBy.equals("RECOMMEND")) {
            return spotRepository.findByCategoryOrderByScrap(category.getCategoryId(), userLocation, pageable)
                    .map(result -> spotConverter.toCategoryResponse(result, member));
        }
        throw new SpotHandler(INVALID_SORT_TYPE);
    }
}
