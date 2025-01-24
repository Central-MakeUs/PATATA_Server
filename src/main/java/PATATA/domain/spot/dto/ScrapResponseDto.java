package PATATA.domain.spot.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ScrapResponseDto {

    @Getter
    @NoArgsConstructor
    public static class ToggleResponse {
        private String message;
        private int totalScraps;

        @Builder
        public ToggleResponse(String message, int totalScraps) {
            this.message = message;
            this.totalScraps = totalScraps;
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
