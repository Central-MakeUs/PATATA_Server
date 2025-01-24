package PATATA.domain.member.controller;

import PATATA.domain.member.dto.NickNameDto;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.service.MemberService;
import PATATA.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberService memberService;

    @PatchMapping("/nickname")
    public ApiResponse<Void> updateNickname(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid NickNameDto nickNameDto
    ) {
        memberService.updateNickname(member, nickNameDto.getNickName());
        return ApiResponse.onSuccess(null);
    }
}
