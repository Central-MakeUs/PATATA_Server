package PATATA.domain.spot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;

public class SpotRequestDTO {

    @Getter
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

        @Builder
        public CreateRequest(String spotName, String spotDescription, String spotAddress,
                             String spotAddressDetail, Double latitude, Double longitude, Long categoryId) {
            this.spotName = spotName;
            this.spotDescription = spotDescription;
            this.spotAddress = spotAddress;
            this.spotAddressDetail = spotAddressDetail;
            this.latitude = latitude;
            this.longitude = longitude;
            this.categoryId = categoryId;
        }
    }
}
