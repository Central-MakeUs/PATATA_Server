package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.member.entity.Role;
import PATATA.domain.spot.dto.ReviewRequestDto;
import PATATA.domain.spot.dto.ReviewResponseDto;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.ReviewRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.global.error.exception.ReportHandler;
import PATATA.global.error.exception.ReviewHandler;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static PATATA.global.error.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final SpotRepository spotRepository;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, Member member) {

        // 신고된 사용자 체크
        if (member.getRole().equals(Role.REPORTED)) {
            throw new ReportHandler(MEMBER_IS_REPORTED);
        }

        Spot spot = spotRepository.findByIdAndDeletedFalse(requestDto.getSpotId())
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        Review review = Review.builder()
                .member(member)
                .reviewText(requestDto.getReviewText())
                .spot(spot)
                .build();
        Review savedReview = reviewRepository.save(review);
        return new ReviewResponseDto(savedReview.getReviewId(), member.getNickName(), savedReview.getReviewText(), savedReview.getCreatedAt());
    }

    @Transactional
    public void deleteReview(Long reviewId, Member member) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewHandler(REVIEW_NOT_FOUND));
        if (!review.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ReviewHandler(NOT_REVIEW_OWNER);
        }
        review.delete();
    }
}
