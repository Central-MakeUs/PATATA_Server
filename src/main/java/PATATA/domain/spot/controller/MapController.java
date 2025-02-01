package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.service.MapService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final MapService mapService;

    @Operation(summary = "지도 내 스팟 불러오기 API")
    @GetMapping("/in-bound")
    public ApiResponse<List<MapResponseDto.InBoundsResponse>> getSpotsInBounds(
            @RequestParam(value = "minLatitude") Double minLatitude,
            @RequestParam(value = "minLongitude") Double minLongitude,
            @RequestParam(value = "maxLatitude") Double maxLatitude,
            @RequestParam(value = "maxLongitude") Double maxLongitude,
            @RequestParam(value = "userLatitude") Double userLatitude,
            @RequestParam(value = "userLongitude") Double userLongitude,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @AuthenticationPrincipal Member member
            ) {
        List<MapResponseDto.InBoundsResponse> spots = mapService.getSpotsInBounds(minLatitude, minLongitude, maxLatitude, maxLongitude, userLatitude, userLongitude, categoryId, member);
        return ApiResponse.onSuccess(spots);
    }
}
