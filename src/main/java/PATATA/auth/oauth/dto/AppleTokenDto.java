package PATATA.auth.oauth.dto;

public record AppleTokenDto(
        String refreshToken,
        String idToken
) {}
