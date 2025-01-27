package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.ReviewRequestDto;
import PATATA.domain.spot.dto.ReviewResponseDto;
import PATATA.domain.spot.service.ReviewService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 생성 API")
    @PostMapping(value = "/create")
    public ApiResponse<ReviewResponseDto> createReview(
            @AuthenticationPrincipal Member member,
            @RequestBody ReviewRequestDto requestDto
    ) {
        ReviewResponseDto responseDto = reviewService.createReview(requestDto, member);
        return ApiResponse.onSuccess(responseDto);
    }

    @Operation(summary = "리뷰 삭제 API")
    @DeleteMapping("/delete/{review_id}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal Member member,
            @PathVariable("review_id") Long reviewId
    ) {
        reviewService.deleteReview(reviewId, member);
        return ApiResponse.onSuccess(null);
    }
}
