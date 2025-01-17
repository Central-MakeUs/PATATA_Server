package PATATA.domain.spot.service;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.ReviewRequestDto;
import PATATA.domain.spot.dto.ReviewResponseDto;
import PATATA.domain.spot.entity.Review;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.ReviewRepository;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.global.error.exception.SpotHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static PATATA.global.error.code.status.ErrorStatus.SPOT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final SpotRepository spotRepository;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, Member member) {
        Spot spot = spotRepository.findById(requestDto.getSpotId())
                .orElseThrow(() -> new SpotHandler(SPOT_NOT_FOUND));

        Review review = Review.builder()
                .member(member)
                .reviewText(requestDto.getReviewText())
                .spot(spot)
                .build();
        Review savedReview = reviewRepository.save(review);
        return new ReviewResponseDto(savedReview.getReviewId(), savedReview.getReviewText());
    }
}
