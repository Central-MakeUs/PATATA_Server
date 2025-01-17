package PATATA.domain.spot.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {
    @NotEmpty
    private Long spotId;
    @NotEmpty
    private String reviewText;
}
