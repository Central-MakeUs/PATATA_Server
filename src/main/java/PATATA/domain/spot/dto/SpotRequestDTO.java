package PATATA.domain.spot.dto;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.Category;
import PATATA.domain.spot.entity.Spot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;

public class SpotRequestDTO {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateRequest {
        @NotBlank(message = "스팟 이름은 필수입니다.")
        @Size(max = 15, message = "스팟 이름은 15자 이하여야 합니다.")
        private String spotName;

        @Size(max = 100, message = "스팟 설명은 100자 이하여야 합니다.")
        private String spotDescription;

        @NotBlank(message = "스팟 주소는 필수입니다.")
        private String spotAddress;

        private String spotAddressDetail;

        @NotNull(message = "스팟 위치는 필수입니다.")
        private Point spotLocation;

        @NotNull(message = "카테고리는 필수입니다.")
        private Long categoryId;

        @Builder
        public CreateRequest(String spotName, String spotDescription, String spotAddress,
                             String spotAddressDetail, Point spotLocation, Long categoryId) {
            this.spotName = spotName;
            this.spotDescription = spotDescription;
            this.spotAddress = spotAddress;
            this.spotAddressDetail = spotAddressDetail;
            this.spotLocation = spotLocation;
            this.categoryId = categoryId;
        }

        public Spot toEntity(Category category, Member member) {
            return Spot.builder()
                    .spotName(this.spotName)
                    .spotDescription(this.spotDescription)
                    .spotAddress(this.spotAddress)
                    .spotAddressDetail(this.spotAddressDetail)
                    .spotLocation(this.spotLocation)
                    .spotScraps(0)
                    .spotCategory(category)
                    .member(member)
                    .build();
        }
}
