package PATATA.domain.spot.dto;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SpotResponseDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateResponse {
        private Long spotId;
        private String spotName;
        private String nickName;

        public static CreateResponse from(Spot spot) {
            return new CreateResponse(spot.getSpotId(), spot.getSpotName(), spot.getMember().getNickName());
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
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime reviewDate;
            private Boolean isAuthor;
        }

        public static DetailResponse from(Spot spot, Boolean isAuthor, Boolean isScraped, List<Review> reviews, List<Tag> tags, List<SpotImage> spotImages, Member member) {
            List<ReviewInfo> reviewInfos = reviews.stream()
                    .map(review -> ReviewInfo.builder()
                            .reviewId(review.getReviewId())
                            .reviewText(review.getReviewText())
                            .memberName(review.getMember() != null ? review.getMember().getNickName() : "알 수 없음")
                            .reviewDate(review.getCreatedAt())
                            .isAuthor(review.getMember() != null && review.getMember().getMemberId().equals(member.getMemberId()))
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
                    .memberName(spot.getMember() != null ? spot.getMember().getNickName() : "알 수 없음")
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
        private int currentPage;    // 현재 페이지
        private int totalPages;
        private int totalCount;
        private List<SearchResponse> spots;

        public static SearchListResponse of(Page<SearchResponse> spots) {
            return SearchListResponse.builder()
                    .currentPage(spots.getNumber())
                    .totalPages(spots.getTotalPages())
                    .totalCount((int) spots.getTotalElements())
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
        private int currentPage;    // 현재 페이지
        private int totalPages;
        private int totalCount;
        private List<CategoryResponse> spots;

        public static CategoryListResponse of(Page<CategoryResponse> spots) {
            return CategoryListResponse.builder()
                    .currentPage(spots.getNumber())
                    .totalPages(spots.getTotalPages())
                    .totalCount((int) spots.getTotalElements())
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
        private Long categoryId;
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
                    .categoryId(spot.getSpotCategory().getCategoryId())
                    .imageUrl(imageUrl)
                    .spotScraps(spot.getSpotScraps())
                    .isScraped(isScraped)
                    .reviews(reviews)
                    .tags(tags)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TodaySpotResponse {
        private Long spotId;
        private String spotAddress;
        private String spotName;
        private Long categoryId;
        private String imageUrl;
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private List<String> tags;

        public static TodaySpotResponse from(Spot spot, String imageUrl, Boolean isScraped, List<String> tags) {

            return TodaySpotResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotAddress(spot.getSpotAddress())
                    .spotName(spot.getSpotName())
                    .categoryId(spot.getSpotCategory().getCategoryId())
                    .imageUrl(imageUrl)
                    .isScraped(isScraped)
                    .tags(tags)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TodaySpotListResponse {
        private Long spotId;
        private String spotName;
        private String spotAddress;
        private String spotAddressDetail;
        private Long categoryId;
        private Double distance;
        private List<String> images;
        private Boolean isScraped;
        private List<String> tags;

        public static TodaySpotListResponse from(Spot spot, Double distance, List<SpotImage> images, Boolean isScraped, List<String> tags) {

            List<String> imageUrls = images.stream()
                    .map(SpotImage::getImageUrl)
                    .collect(Collectors.toList());

            return TodaySpotListResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .categoryId(spot.getSpotCategory().getCategoryId())
                    .distance(distance)
                    .images(imageUrls)
                    .isScraped(isScraped)
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
