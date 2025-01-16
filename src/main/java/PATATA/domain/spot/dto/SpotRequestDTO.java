package PATATA.domain.spot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.beans.ConstructorProperties;
import java.util.List;

@Builder
public class SpotRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateRequest {
        @NotBlank(message = "스팟 이름은 필수입니다.")
        @Size(max = 15, message = "스팟 이름은 15자 이하여야 합니다.")
        private String spotName;

        @NotBlank(message = "스팟 주소는 필수입니다.")
        private String spotAddress;

        private String spotAddressDetail;

        @NotNull(message = "스팟 위치는 필수입니다.")
        private Double latitude;

        @NotNull(message = "스팟 위치는 필수입니다.")
        private Double longitude;

        @Size(max = 100, message = "스팟 설명은 100자 이하여야 합니다.")
        private String spotDescription;

        @NotNull(message = "카테고리는 필수입니다.")
        private Long categoryId;

        private List<String> tags;

        private List<SpotImageRequest> images;

        @Builder
        @ConstructorProperties({"spotName", "spotDescription", "spotAddress", "spotAddressDetail",
                "latitude", "longitude", "categoryId", "tags", "images"})
        public CreateRequest(String spotName, String spotDescription, String spotAddress,
                             String spotAddressDetail, Double latitude, Double longitude, Long categoryId, List<String> tags, List<SpotImageRequest> images) {
            this.spotName = spotName;
            this.spotDescription = spotDescription;
            this.spotAddress = spotAddress;
            this.spotAddressDetail = spotAddressDetail;
            this.latitude = latitude;
            this.longitude = longitude;
            this.categoryId = categoryId;
            this.tags = tags;
            this.images = images;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SpotImageRequest {
        private MultipartFile file;
        private Boolean isRepresentative;
        private Integer sequence;

        @Builder
        public SpotImageRequest(MultipartFile file, Boolean isRepresentative, Integer sequence) {
            this.file = file;
            this.isRepresentative = isRepresentative;
            this.sequence = sequence;
        }
    }

}
