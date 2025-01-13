package PATATA.domain.spot.controller;

import PATATA.domain.member.entity.Member;
import PATATA.domain.spot.dto.SpotRequestDTO;
import PATATA.domain.spot.dto.SpotResponseDTO;
import PATATA.domain.spot.service.SpotService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
@Validated
public class SpotController {

    private final SpotService spotService;
    @Operation(summary = "스팟 생성")
    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<SpotResponseDTO> createRecord(
            @AuthenticationPrincipal Member member,
            @ModelAttribute SpotRequestDTO requestDTOs) {

        SpotResponseDTO responseDTO = spotService.createSpot(requestDTOs);
        return ApiResponse.onSuccess(responseDTO);
    }

    @Operation(summary = "일지 상세 내용 조회 API")
    @GetMapping("/detail/{pet_id}/{record_id}")
    public ApiResponse<RecordResponseDTO.DetailResultDTO> getRecordDetails(
            @PathVariable("pet_id") Long petId,
            @PathVariable("record_id") Long recordId
    ) {
        RecordResponseDTO.DetailResultDTO recordDetails = recordService.getRecordDetails(petId, recordId);
        return ApiResponse.onSuccess(recordDetails);
    }

    @Operation(summary = "일지 삭제 API")
    @DeleteMapping("/{record_id}")
    public ApiResponse<RecordResponseDTO.DeleteResultDTO> deleteRecord(
            @PathVariable("record_id") Long record_id
    ) {
        RecordResponseDTO.DeleteResultDTO deleteResultDTO = recordService.deleteRecord(record_id);
        return ApiResponse.onSuccess(deleteResultDTO);
    }
}
