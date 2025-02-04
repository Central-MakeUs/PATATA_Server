package PATATA.auth.oauth.controller;

import PATATA.global.response.ApiResponse;
import PATATA.global.error.exception.JwtHandler;
import PATATA.global.error.exception.OAuthHandler;
import PATATA.auth.oauth.dto.AppleLoginRequestDTO;
import PATATA.auth.oauth.dto.GoogleLoginRequestDTO;
import PATATA.auth.oauth.dto.LoginResponseDTO;
import PATATA.auth.oauth.service.OAuthService;
import PATATA.domain.member.entity.Member;
import PATATA.domain.member.service.MemberService;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static PATATA.global.error.code.status.ErrorStatus.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class OAuthController {

    private final OAuthService oAuthService;
    private final MemberService memberService;

    @Operation(summary = "토큰 재발급 API")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponseDTO> regenerateAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("RefreshToken");

        if (!StringUtils.hasText(refreshToken)) {
            throw new OAuthHandler(TOKEN_EMPTY);
        }

        if (!refreshToken.startsWith("Bearer ")) {
            throw new OAuthHandler(INVALID_TOKEN_FORMAT);
        }

        LoginResponseDTO result = memberService.regenerateAccessToken(refreshToken.substring(7));
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "애플 로그인 API")
    @PostMapping("/apple/login")
    public ApiResponse<LoginResponseDTO> appleLogin(@RequestBody @Validated AppleLoginRequestDTO appleReqDto) {
        if (appleReqDto.getIdentityToken() == null)
            throw new OAuthHandler(APPLE_ID_TOKEN_EMPTY);
        return ApiResponse.onSuccess(oAuthService.appleLogin(appleReqDto));
    }

    @Operation(summary = "구글 로그인 API")
    @PostMapping("/google/login")
    public ApiResponse<LoginResponseDTO> googleLogin(@RequestBody @Validated GoogleLoginRequestDTO googleReqDto) {
        if (googleReqDto.getIdToken() == null)
            throw new OAuthHandler(GOOGLE_ID_TOKEN_EMPTY);
        return ApiResponse.onSuccess(oAuthService.googleLogin(googleReqDto));
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal Member member) {
        String token = member.getRefreshToken();

        if (StringUtils.hasText(token)) {
            memberService.logout(token);
            return ApiResponse.onSuccess("LOGOUT SUCCESS");
        } else
            throw new JwtHandler(TOKEN_EMPTY);
    }

    @Operation(summary = "애플 탈퇴 API")
    @DeleteMapping("/delete/apple")
    public ApiResponse<String> appleWithdraw(@AuthenticationPrincipal Member member,
                                             @Nullable @RequestHeader("authorization-code") final String code){
        oAuthService.appleDelete(member, code);
        return ApiResponse.onSuccess("apple delete success");
    }

    @Operation(summary = "구글 탈퇴 API")
    @DeleteMapping("/delete/google")
    public ApiResponse<String> googleWithdraw(@AuthenticationPrincipal Member member,
                                             @Nullable @RequestHeader("google-accessToken") final String googleToken){
        oAuthService.googleDelete(member, googleToken);
        return ApiResponse.onSuccess("google delete success");
    }

}
