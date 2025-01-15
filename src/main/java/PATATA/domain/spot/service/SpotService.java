package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.converter.SpotConverter;
import PATATA.domain.spot.dto.SpotRequestDTO;
import PATATA.domain.spot.dto.SpotResponseDTO;
import PATATA.domain.spot.entity.Category;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotTag;
import PATATA.domain.spot.entity.Tag;
import PATATA.domain.spot.repository.CategoryRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.domain.spot.repository.SpotTagRepository;
import PATATA.domain.spot.repository.TagRepository;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.CATEGORY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;
    private final CategoryRepository categoryRepository;
    private final GeometryFactory geometryFactory;
    private final TagRepository tagRepository;
    private final SpotTagRepository spotTagRepository;

    @Transactional
    public SpotResponseDTO.CreateResponse createSpot(SpotRequestDTO.CreateRequest requestDTO, Member member) {
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new SpotHandler(CATEGORY_NOT_FOUND));
        Point point = geometryFactory.createPoint(new Coordinate(requestDTO.getLongitude(), requestDTO.getLatitude()));

        Spot spot = SpotConverter.toEntity(requestDTO, point, category, member);
        Spot savedSpot = spotRepository.save(spot);

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
        return SpotResponseDTO.CreateResponse.from(savedSpot);
    }
}
