package PATATA.domain.spot.dto;

import PATATA.domain.spot.entity.Spot;
import lombok.Builder;
import lombok.Getter;

import java.awt.*;

public class SpotResponseDTO {

    @Getter
    @Builder
    public static class CreateResponse {
        private Long spotId;
        private String spotName;

        public static CreateResponse from(Spot spot) {
            return CreateResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long spotId;
        private String spotName;
        private String spotDescription;
        private String spotAddress;
        private String spotAddressDetail;
        private Point spotLocation;
        private Integer spotScraps;
        private String categoryName;
        private String memberName;

        public static DetailResponse from(Spot spot) {
            return DetailResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .spotDescription(spot.getSpotDescription())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .spotLocation(spot.getSpotLocation())
                    .spotScraps(spot.getSpotScraps())
                    .categoryName(spot.getSpotCategory().getCategoryName())
                    .memberName(spot.getMember().getNickName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class UpdateResponse {
        private Long spotId;
        private String spotName;

        public static UpdateResponse from(Spot spot) {
            return UpdateResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .build();
        }
    }
}
