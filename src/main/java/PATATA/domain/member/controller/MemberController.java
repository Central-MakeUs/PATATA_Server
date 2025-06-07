package PATATA.domain.member.controller;

import PATATA.domain.member.dto.MemberProfileDto;
import PATATA.domain.member.dto.NickNameDto;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.service.MemberService;
import PATATA.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "닉네임 설정/변경 API")
    @PatchMapping("/nickname")
    public ApiResponse<Void> updateNickname(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid NickNameDto nickNameDto
    ) {
        memberService.updateNickname(member, nickNameDto.getNickName());
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "프로필 이미지 추가/변경")
    @PatchMapping(value = "/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> updateProfileImage(
            @AuthenticationPrincipal Member member,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        String image = memberService.updateProfileImage(member, profileImage);
        return ApiResponse.onSuccess(image);
    }

    @Operation(summary = "프로필 불러오기")
    @GetMapping("/profile")
    public ApiResponse<MemberProfileDto> getProfile(
            @AuthenticationPrincipal Member member
    ) {
        MemberProfileDto profileDto = memberService.getProfile(member);
        return ApiResponse.onSuccess(profileDto);
    }
}
