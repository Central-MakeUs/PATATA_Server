package PATATA.domain.spot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3ImageUrlDto {
    private String originalImageUrl;
    private String resizedImageUrl;
}
