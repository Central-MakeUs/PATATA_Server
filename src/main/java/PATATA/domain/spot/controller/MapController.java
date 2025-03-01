package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.MapResponseDto;
import PATATA.domain.spot.entity.Spot;
import PATATA.domain.spot.repository.SpotRepository;
import PATATA.domain.spot.service.MapService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.member;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final MapService mapService;
    private final SpotRepository spotRepository;

    @Operation(summary = "지도 내 스팟 불러오기 API(지도)")
    @GetMapping("/in-bound/map")
    public ApiResponse<List<MapResponseDto.InBoundsResponse>> getSpotsInBounds(
            @Parameter(description = "남서쪽 위도") @RequestParam(value = "minLatitude") Double minLatitude,
            @Parameter(description = "남서쪽 경도") @RequestParam(value = "minLongitude") Double minLongitude,
            @Parameter(description = "북동쪽 위도") @RequestParam(value = "maxLatitude") Double maxLatitude,
            @Parameter(description = "북동쪽 경도") @RequestParam(value = "maxLongitude") Double maxLongitude,
            @Parameter(description = "사용자 위도") @RequestParam(value = "userLatitude") Double userLatitude,
            @Parameter(description = "사용자 경도") @RequestParam(value = "userLongitude") Double userLongitude,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "검색과 함께 사용 여부") @RequestParam(value = "withSearch") Boolean withSearch,
            @AuthenticationPrincipal Member member
            ) {
        List<MapResponseDto.InBoundsResponse> spots = mapService.getSpotsInBounds(minLatitude, minLongitude, maxLatitude, maxLongitude, userLatitude, userLongitude, categoryId, withSearch, member);
        return ApiResponse.onSuccess(spots);
    }

    @Operation(summary = "지도 내 스팟 불러오기 API(목록)")
    @GetMapping("/in-bound/list")
    public ApiResponse<MapResponseDto.InBoundsListResponse> getSpotsListInBounds(
            @Parameter(description = "남서쪽 위도") @RequestParam(value = "minLatitude") Double minLatitude,
            @Parameter(description = "남서쪽 경도") @RequestParam(value = "minLongitude") Double minLongitude,
            @Parameter(description = "북동쪽 위도") @RequestParam(value = "maxLatitude") Double maxLatitude,
            @Parameter(description = "북동쪽 경도") @RequestParam(value = "maxLongitude") Double maxLongitude,
            @Parameter(description = "사용자 위도") @RequestParam(value = "userLatitude") Double userLatitude,
            @Parameter(description = "사용자 경도") @RequestParam(value = "userLongitude") Double userLongitude,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "검색과 함께 사용 여부") @RequestParam(value = "withSearch") Boolean withSearch,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @AuthenticationPrincipal Member member
    ) {
        Pageable pageable = PageRequest.of(page, 3);
        Page<MapResponseDto.InBoundsResponse> spots = mapService.getSpotsListInBounds(
                minLatitude, minLongitude, maxLatitude, maxLongitude,
                userLatitude, userLongitude, categoryId, withSearch,
                pageable, member);
        return ApiResponse.onSuccess(MapResponseDto.InBoundsListResponse.of(spots));
    }

    @Operation(summary = "지도 내 스팟 검색하기 API")
    @GetMapping("/search")
    public ApiResponse<MapResponseDto.InBoundsResponse> getSpotSearched(
            @RequestParam("spotName") String spotName,
            @Parameter(description = "남서쪽 위도") @RequestParam(value = "minLatitude", required = false) Double minLatitude,
            @Parameter(description = "남서쪽 경도") @RequestParam(value = "minLongitude", required = false) Double minLongitude,
            @Parameter(description = "북동쪽 위도") @RequestParam(value = "maxLatitude", required = false) Double maxLatitude,
            @Parameter(description = "북동쪽 경도") @RequestParam(value = "maxLongitude", required = false) Double maxLongitude,
            @Parameter(description = "사용자 위도") @RequestParam(value = "userLatitude") Double userLatitude,
            @Parameter(description = "사용자 경도") @RequestParam(value = "userLongitude") Double userLongitude,
            @AuthenticationPrincipal Member member
    ) {
        MapResponseDto.InBoundsResponse spots = mapService.getSpotSearched(spotName
                , minLatitude, minLongitude, maxLatitude, maxLongitude, userLatitude, userLongitude, member);
        return ApiResponse.onSuccess(spots);
    }

    @Operation(summary = "반경 내 스팟 수 체크 API")
    @GetMapping("/density")
    public ApiResponse<Object> checkSpotDensity(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "위도") @RequestParam(value = "latitude") Double latitude,
            @Parameter(description = "경도") @RequestParam(value = "longitude") Double longitude
    ) {
        List<Spot> nearbySpots = spotRepository.findSpotsWithinRadius(latitude, longitude, 100.0);
        int spotCount = nearbySpots.size();
        if (spotCount >= 25) {
            List<MapResponseDto.SpotLocationInfo> spotLocations =
                    nearbySpots.stream()
                            .map(MapResponseDto.SpotLocationInfo::from)
                            .toList();
            return ApiResponse.onFailure("SPOT4004", "현재 등록된 스팟이 너무 많습니다.", spotLocations);
        } else {
            return ApiResponse.onSuccess("스팟 등록이 가능합니다. 현재 등록 스팟 수 : " + spotCount);
        }
    }
}
