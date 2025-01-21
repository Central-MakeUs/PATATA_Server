package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.converter.SpotConverter;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.entity.*;
import PATATA.domain.spot.repository.*;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
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

    public SpotResponseDto.DetailResponse getSpotDetail(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        List<Review> reviews = reviewRepository.findBySpot(spot);
        List<Tag> tags = spotTagRepository.findBySpot(spot).stream()
                .map(SpotTag::getTag)
                .collect(Collectors.toList());
        return SpotResponseDto.DetailResponse.from(spot, reviews, tags);
    }

    @Transactional
    public SpotResponseDto.UpdateResponse updateSpot(Long spotId, SpotRequestDto.UpdateRequest request, Member member) {
        Spot spot = spotRepository.findById(spotId)
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
}
