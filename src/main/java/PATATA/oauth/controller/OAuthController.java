package PATATA.oauth.controller;

import PATATA.apiPayLoad.ApiResponse;
import PATATA.apiPayLoad.exception.JwtHandler;
import PATATA.apiPayLoad.exception.OAuthHandler;
import PATATA.member.entity.Member;
import PATATA.oauth.dto.AppleLoginRequestDTO;
import PATATA.oauth.dto.GoogleLoginRequestDTO;
import PATATA.oauth.dto.LoginResponseDTO;
import PATATA.oauth.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static PATATA.apiPayLoad.code.status.ErrorStatus.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class OAuthController {

    private final OAuthService oAuthService;

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

        LoginResponseDTO result = oAuthService.regenerateAccessToken(refreshToken.substring(7));
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

    @Operation(summary = "햐로그아웃 API")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal Member member) {
        String token = member.getRefreshToken();

        if (StringUtils.hasText(token)) {
            oAuthService.logout(token);
            return ApiResponse.onSuccess("LOGOUT SUCCESS");
        } else
            throw new JwtHandler(TOKEN_EMPTY);
    }
}
