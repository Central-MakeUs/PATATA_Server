package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.ScrapResponseDto;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.service.SpotService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SpotController {

    private final SpotService spotService;

    @Operation(summary = "스팟 생성 API")
    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<SpotResponseDto.CreateResponse> createRecord(
            @AuthenticationPrincipal Member member,
            @ModelAttribute @Valid SpotRequestDto.CreateRequest requestDTOs) {
        SpotResponseDto.CreateResponse responseDTO = spotService.createSpot(requestDTOs, member);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "스팟 상세 조회 API")
    @GetMapping("/{spot_id}")
    public ApiResponse<SpotResponseDto.DetailResponse> getRecordDetails(
            @PathVariable("spot_id") Long spotId,
            @AuthenticationPrincipal Member member
            ) {
        SpotResponseDto.DetailResponse spotDetails = spotService.getSpotDetail(spotId, member);
        return ApiResponse.onSuccess(spotDetails);
    }

    @Operation(summary = "스팟 수정 API")
    @PatchMapping("/{spot_id}")
    public ApiResponse<SpotResponseDto.UpdateResponse> updateSpot(
            @AuthenticationPrincipal Member member,
            @PathVariable("spot_id") Long spotId,
            @RequestBody @Valid SpotRequestDto.UpdateRequest requestDTO) {
        SpotResponseDto.UpdateResponse responseDTO = spotService.updateSpot(spotId, requestDTO, member);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "스팟 삭제 API")
    @DeleteMapping("/{spot_id}")
    public ApiResponse<SpotResponseDto.DeleteResponse> deleteSpot(
            @AuthenticationPrincipal Member member,
            @PathVariable("spot_id") Long spotId
    ) {
        SpotResponseDto.DeleteResponse responseDTO = spotService.deleteSpot(spotId, member);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "스팟 검색 API")
    @GetMapping("/search")
    public ApiResponse<SpotResponseDto.SearchListResponse> searchSpot(
            @RequestParam("spotName") String spotName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "latitude") Double latitude,
            @RequestParam(value = "longitude") Double longitude,
            @RequestParam(value = "sortBy", defaultValue = "RECOMMEND") String sortBy,
            @AuthenticationPrincipal Member member
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpotResponseDto.SearchResponse> searchResults = spotService.searchSpotsByName(
                spotName, latitude, longitude, sortBy, pageable, member);
        return ApiResponse.onSuccess(SpotResponseDto.SearchListResponse.of(searchResults));

    }

    @Operation(summary = "내가 작성한 스팟 조회 API")
    @GetMapping("/my-spots")
    public ApiResponse<List<ScrapResponseDto.SpotDto>> getMySpots(
            @AuthenticationPrincipal Member member
    ) {
        List<ScrapResponseDto.SpotDto> mySpots = spotService.getMySpots(member);
        return ApiResponse.onSuccess(mySpots);
    }

    @Operation(summary = "카테고리별 스팟 조회 API")
    @GetMapping("/category/{category_id}")
    public ApiResponse<SpotResponseDto.CategoryListResponse> getSpotsByCategory(
            @PathVariable("category_id") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "latitude") Double latitude,
            @RequestParam(value = "longitude") Double longitude,
            @RequestParam(value = "sortBy", defaultValue = "RECOMMEND") String sortBy,
            @AuthenticationPrincipal Member member
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpotResponseDto.CategoryResponse> categoryResults = spotService.getSpotsByCategory(
                categoryId, latitude, longitude, sortBy, pageable, member);
        return ApiResponse.onSuccess(SpotResponseDto.CategoryListResponse.of(categoryResults));
    }
}
