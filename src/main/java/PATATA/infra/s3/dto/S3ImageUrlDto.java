package PATATA.infra.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3ImageUrlDto {
    private String originalImageUrl;
    private String resizedImage400Url;
    private String resizedImage800Url;
    private String resizedImage1200Url;

    public static S3ImageUrlDto of(String original, String url400, String url800, String url1200) {
        return S3ImageUrlDto.builder()
                .originalImageUrl(original)
                .resizedImage400Url(url400)
                .resizedImage800Url(url800)
                .resizedImage1200Url(url1200)
                .build();
    }
}
