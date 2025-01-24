package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.SpotRequestDto;
import PATATA.domain.spot.dto.SpotResponseDto;
import PATATA.domain.spot.service.SpotService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
