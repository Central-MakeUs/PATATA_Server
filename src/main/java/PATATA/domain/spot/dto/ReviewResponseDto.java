package PATATA.domain.spot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReviewResponseDto {
    private Long reviewId;
    private String reviewText;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewDate;

    @Builder
    public ReviewResponseDto(Long reviewId, String reviewText, LocalDateTime reviewDate) {
        this.reviewId = reviewId;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

}
