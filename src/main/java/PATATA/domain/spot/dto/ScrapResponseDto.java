package PATATA.domain.spot.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ScrapResponseDto {

    @Getter
    @NoArgsConstructor
    public static class ToggleResponse {
        private Long spotId;
        private int totalScraps;
        private String message;

        @Builder
        public ToggleResponse(Long spotId, int totalScraps, String message) {
            this.spotId = spotId;
            this.totalScraps = totalScraps;
            this.message = message;
        }
    }

    @Getter
    @Builder
    public static class MySpotsResponseDto {
        private int totalSpots;
        private List<SpotDto> spots;

        public MySpotsResponseDto(int totalSpots, List<SpotDto> spots) {
            this.totalSpots = totalSpots;
            this.spots = spots;
        }
    }

    @Getter
    @Builder
    public static class SpotDto {
        private Long spotId;
        private String spotName;
        private String representativeImageUrl;
    }

}
