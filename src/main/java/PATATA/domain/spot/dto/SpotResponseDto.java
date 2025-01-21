package PATATA.domain.spot.dto;

import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class SpotResponseDto {

    @Getter
    @Builder
    public static class CreateResponse {
        private Long spotId;
        private String spotName;

        public static CreateResponse from(Spot spot) {
            return CreateResponse.builder()
                    .spotId(spot.getSpotId())
                    .spotName(spot.getSpotName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long spotId;
        private String spotName;
        private String spotDescription;
        private String spotAddress;
        private String spotAddressDetail;
        private String categoryName;
        private String memberName;
        private List<String> tags;
        private List<ReviewInfo> reviews;

        @Getter
        @Builder
        public static class ReviewInfo {
            private Long reviewId;
            private String memberName;
            private String reviewText;
        }

        public static DetailResponse from(Spot spot, List<Review> reviews, List<Tag> tags) {
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
                    .spotName(spot.getSpotName())
                    .spotDescription(spot.getSpotDescription())
                    .spotAddress(spot.getSpotAddress())
                    .spotAddressDetail(spot.getSpotAddressDetail())
                    .categoryName(spot.getSpotCategory().getCategoryName())
                    .memberName(spot.getMember().getNickName())
                    .tags(tagNames)
                    .reviews(reviewInfos)
                    .build();
        }
    }

//    @Getter
//    @Builder
//    public static class UpdateResponse {
//        private Long spotId;
//        private String spotName;
//
//        public static UpdateResponse from(Spot spot) {
//            return UpdateResponse.builder()
//                    .spotId(spot.getSpotId())
//                    .spotName(spot.getSpotName())
//                    .build();
//        }
//    }
}
