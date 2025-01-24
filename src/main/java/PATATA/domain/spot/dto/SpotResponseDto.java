package PATATA.domain.spot.dto;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.entity.Category;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.Tag;
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
        private String spotName;
        private String spotDescription;
        private String spotAddress;
        private String spotAddressDetail;
        private String categoryName;
        private String memberName;
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

        public static DetailResponse from(Spot spot, Boolean isAuthor, List<Review> reviews, List<Tag> tags) {
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

            return DetailResponse.builder()
                    .spotId(spot.getSpotId())
                    .isAuthor(isAuthor)
                    .spotName(spot.getSpotName())
                    .spotDescription(spot.getSpotDescription())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .categoryName(spot.getSpotCategory().getCategoryName())
                    .memberName(spot.getMember().getNickName())
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
        private Double latitude;
        private Double longitude;
        private String imageUrl;
        private Integer spotScraps;
        private Boolean isScraped;    // 현재 사용자의 스크랩 여부
        private Integer reviews;

        public static SearchResponse from(Spot spot, String imageUrl, Boolean isScraped, Integer reviews) {

            return SearchResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .latitude(spot.getSpotLocation().getX())
                    .longitude(spot.getSpotLocation().getY())
                    .imageUrl(imageUrl)
                    .spotScraps(spot.getSpotScraps())
                    .isScraped(isScraped)
                    .reviews(reviews)
                    .build();
        }
    }



}
