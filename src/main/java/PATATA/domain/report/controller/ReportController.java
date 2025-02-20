package PATATA.domain.report.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.report.service.ReportService;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "스팟 신고하기 API")
    @PostMapping("/spot/{spot_id}")
    public ApiResponse<SpotResponseDto.ReportResponse> reportSpot(
            @AuthenticationPrincipal Member member,
            @PathVariable("spot_id") Long spotId,
            @RequestBody @Valid SpotRequestDto.ReportRequest requestDTO
    ) {
        SpotResponseDto.ReportResponse responseDTO = reportService.reportSpot(spotId, requestDTO, member);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "리뷰 신고하기 API")
    @PostMapping("/review/{review_id}")
    public ApiResponse<SpotResponseDto.ReportResponse> reportReview(
            @AuthenticationPrincipal Member member,
            @PathVariable("review_id") Long reviewId,
            @RequestBody @Valid SpotRequestDto.ReportRequest requestDTO
    ) {
        SpotResponseDto.ReportResponse responseDTO = reportService.reportReview(reviewId, requestDTO, member);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "사용자 신고하기 API")
    @PostMapping("/member/{member_id}")
    public ApiResponse<SpotResponseDto.ReportResponse> reportMember(
            @AuthenticationPrincipal Member member,
            @PathVariable("member_id") Long reportedMember,
            @RequestBody @Valid SpotRequestDto.ReportRequest requestDTO
    ) {
        SpotResponseDto.ReportResponse responseDTO = reportService.reportMember(reportedMember, requestDTO, member);
        return ApiResponse.onSuccess(responseDTO);
    }

}
