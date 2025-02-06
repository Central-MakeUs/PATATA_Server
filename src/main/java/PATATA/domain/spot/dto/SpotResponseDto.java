package PATATA.domain.spot.dto;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class SpotResponseDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateResponse {
        private Long spotId;
        private String spotName;

        public static CreateResponse from(Spot spot) {
            return new CreateResponse(spot.getSpotId(), spot.getSpotName());
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {

        private Long spotId;
        private Boolean isAuthor;
        private Boolean isScraped;
        private String spotName;
        private String spotDescription;
        private String spotAddress;
        private String spotAddressDetail;
        private Long categoryId;
        private String memberName;
        private List<String> images;
        private List<String> tags;
        private Integer reviewCount;
        private List<ReviewInfo> reviews;

        @Getter
        @Builder
        public static class ReviewInfo {
            private Long reviewId;
            private String memberName;
            private String reviewText;
        }

        public static DetailResponse from(Spot spot, Boolean isAuthor, Boolean isScraped, List<Review> reviews, List<Tag> tags, List<SpotImage> spotImages) {
            List<ReviewInfo> reviewInfos = reviews.stream()
                    .map(review -> ReviewInfo.builder()
                            .reviewId(review.getReviewId())
                            .reviewText(review.getReviewText())
                            .memberName(review.getMember().getNickName())
                            .build())
                    .collect(Collectors.toList());

            List<String> tagNames = tags.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            List<String> images = spotImages.stream()
                    .map(SpotImage::getImageUrl)
                    .collect(Collectors.toList());

            return DetailResponse.builder()
                    .spotId(spot.getSpotId())
                    .isAuthor(isAuthor)
                    .isScraped(isScraped)
                    .spotName(spot.getSpotName())
                    .spotDescription(spot.getSpotDescription())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .categoryId(spot.getSpotCategory().getCategoryId())
                    .memberName(spot.getMember().getNickName())
                    .images(images)
                    .tags(tagNames)
                    .reviewCount(reviewInfos.size())
                    .reviews(reviewInfos)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateResponse {
        private Long spotId;
        private String spotName;
        private String spotDescription;
        private String spotAddress;
        private String spotAddressDetail;
        private String categoryName;

        public static UpdateResponse from(Spot spot, Category category) {
            return new UpdateResponse(spot.getSpotId(), spot.getSpotName(), spot.getSpotDescription(), spot.getSpotAddress(), spot.getSpotAddressDetail(), category.getCategoryName());
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteResponse {
        private Long spotId;
        private String message;

        public static DeleteResponse of(Long spotId) {
            return new DeleteResponse(spotId, "스팟이 성공적으로 삭제되었습니다.");
        }
    }

    @Getter
    @Builder
    public static class SearchListResponse {
        private long totalCount;
        private List<SearchResponse> spots;

        public static SearchListResponse of(Page<SearchResponse> spots) {
            return SearchListResponse.builder()
                    .totalCount(spots.getTotalElements())
                    .spots(spots.getContent())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SearchResponse {
        private Long spotId;
        private String spotName;
        private String imageUrl;
        private Integer spotScraps;   // 스크랩 수
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private Integer reviews;
        private Double distance;

        public static SearchResponse from(Spot spot, String imageUrl, Boolean isScraped, Integer reviews, Double distance) {

            return SearchResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .imageUrl(imageUrl)
                    .spotScraps(spot.getSpotScraps())
                    .isScraped(isScraped)
                    .reviews(reviews)
                    .distance(distance)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryListResponse {
        private long totalCount;
        private List<CategoryResponse> spots;

        public static CategoryListResponse of(Page<CategoryResponse> spots) {
            return CategoryListResponse.builder()
                    .totalCount(spots.getTotalElements())
                    .spots(spots.getContent())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryResponse {
        private Long spotId;
        private String spotAddress;
        private String spotName;
        private String category;
        private String imageUrl;
        private Integer reviews;
        private Integer spotScraps;   // 스크랩 수
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private List<String> tags;

        public static CategoryResponse from(Spot spot, String imageUrl, Boolean isScraped, Integer reviews, List<String> tags) {

            return CategoryResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotAddress(spot.getSpotAddress())
                    .spotName(spot.getSpotName())
                    .category(spot.getSpotCategory().getCategoryName())
                    .imageUrl(imageUrl)
                    .spotScraps(spot.getSpotScraps())
                    .isScraped(isScraped)
                    .reviews(reviews)
                    .tags(tags)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ReportResponse {
        private String message;

        public static ReportResponse of() {
            return ReportResponse.builder()
                    .message("신고가 접수되었습니다")
                    .build();
        }
    }
}
