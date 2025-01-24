package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.ScrapResponseDto;
import PATATA.domain.spot.entity.Scrap;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.SpotImage;
import PATATA.domain.spot.repository.ScrapRepository;
import PATATA.domain.spot.repository.SpotImageRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.global.error.exception.ScrapHandler;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static PATATA.global.error.code.status.ErrorStatus.SCRAP_FAIL;
import static PATATA.global.error.code.status.ErrorStatus.SPOT_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class ScrapService {

    private final SpotRepository spotRepository;
    private final ScrapRepository scrapRepository;
    private final SpotImageRepository spotImageRepository;

    public ScrapResponseDto.ToggleResponse toggleScrapSpot(Long spotId, Member member) {
        Spot spot = spotRepository.findByIdAndDeletedFalse(spotId)
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));
        try {
            Optional<Scrap> scrap = scrapRepository.findBySpotAndMember(spot, member);

            if(scrap.isPresent()) {
                Scrap existingScrap = scrap.get();
                if(existingScrap.isDeleted()) {
                    existingScrap.restore(); // deleted = false로 설정
                    spot.incrementScrapCount();
                    return new ScrapResponseDto.ToggleResponse("스크랩되었습니다", spot.getSpotScraps());
                } else {
                    existingScrap.delete();
                    spot.decrementScrapCount();
                    return new ScrapResponseDto.ToggleResponse("스크랩이 취소되었습니다", spot.getSpotScraps());
                }
            } else {
                Scrap newScrap = Scrap.builder()
                        .spot(spot)
                        .member(member)
                        .deleted(false)
                        .build();
                scrapRepository.save(newScrap);
                spot.incrementScrapCount();
                return new ScrapResponseDto.ToggleResponse("스크랩되었습니다", spot.getSpotScraps());
            }
        } catch (DataIntegrityViolationException e) {
            throw new ScrapHandler(SCRAP_FAIL);
        }
    }

    public List<ScrapResponseDto.SpotDto> getScrapSpots(Member member) {
        List<Scrap> scraps = scrapRepository.findByMemberAndDeletedFalse(member);

        return scraps.stream()
                .map(Scrap::getSpot)
                .filter(spot -> !spot.isDeleted())  // 삭제되지 않은 스팟만 필터링
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
}
