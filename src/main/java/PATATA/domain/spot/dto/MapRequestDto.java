package PATATA.domain.spot.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MapRequestDto {

    @Getter
    @NoArgsConstructor
    public static class BoundsRequest {

        @NotEmpty
        private Double minLatitude;
        @NotEmpty
        private Double minLongitude;

        @NotEmpty
        private Double maxLatitude;
        @NotEmpty
        private Double maxLongitude;

        @NotEmpty
        private Double userLatitude;
        @NotEmpty
        private Double userLongitude;

    }
}
