package PATATA.domain.spot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewResponseDto {
    private Long reviewId;
    private String reviewText;

    @Builder
    public ReviewResponseDto(Long reviewId, String reviewText) {
        this.reviewId = reviewId;
        this.reviewText = reviewText;
    }

}
