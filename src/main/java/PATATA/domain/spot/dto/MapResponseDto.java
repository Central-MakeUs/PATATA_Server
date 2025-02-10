package PATATA.domain.spot.dto;

import PATATA.domain.spot.entity.Spot;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class MapResponseDto {

    @Getter
    @Builder
    public static class InBoundsResponse {
        private Long spotId;
        private String spotName;
        private String spotAddress;
        private String spotAddressDetail;
        private Double latitude;
        private Double longitude;
        private Long categoryId;
        private List<String> tags;
        private String representativeImageUrl;
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private Double distance;     //사용자와의 거리

        public static MapResponseDto.InBoundsResponse from(Spot spot, String representativeImageUrl, List<String> tags, Boolean isScraped, Double distance) {

            return InBoundsResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .latitude(spot.getSpotLocation().getY())
                    .longitude(spot.getSpotLocation().getX())
                    .categoryId(spot.getSpotCategory().getCategoryId())
                    .tags(tags)
                    .representativeImageUrl(representativeImageUrl)
                    .isScraped(isScraped)
                    .distance(distance)
                    .build();
        }
    }
}
