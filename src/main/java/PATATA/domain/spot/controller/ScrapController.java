package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.ScrapResponseDto;
import PATATA.domain.spot.service.ScrapService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {

    private final ScrapService scrapService;

    @Operation(summary = "스팟 스크랩/취소 토글 API")
    @PatchMapping("/{spot_id}")
    public ApiResponse<ScrapResponseDto.ToggleResponse> toggleScrapTips(
            @AuthenticationPrincipal Member member,
            @PathVariable("spot_id") Long spotId
    ) {
        ScrapResponseDto.ToggleResponse result = scrapService.toggleScrapSpot(spotId, member);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "스크랩한 스팟 목록 조회 API")
    @GetMapping
    public ApiResponse<List<ScrapResponseDto.SpotDto>> getScrapSpots(
            @AuthenticationPrincipal Member member
    ) {
        List<ScrapResponseDto.SpotDto> result = scrapService.getScrapSpots(member);
        return ApiResponse.onSuccess(result);
    }

}
