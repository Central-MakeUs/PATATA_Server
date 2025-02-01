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
        private String category;
        private List<String> tags;
        private List<String> imageUrls;
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private Boolean isAuthor;    //작성자 여부
        private Double distance;     //사용자와의 거리

        public static MapResponseDto.InBoundsResponse from(Spot spot, List<String> imageUrls, List<String> tags, Boolean isScraped, Boolean isAuthor, Double distance) {

            return InBoundsResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .category(spot.getSpotCategory().getCategoryName())
                    .tags(tags)
                    .imageUrls(imageUrls)
                    .isScraped(isScraped)
                    .isAuthor(isAuthor)
                    .distance(distance)
                    .build();
        }
    }
}
