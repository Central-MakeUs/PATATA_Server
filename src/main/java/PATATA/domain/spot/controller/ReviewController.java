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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
