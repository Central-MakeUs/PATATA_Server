package PATATA.auth.jwt.service;

import PATATA.global.error.code.status.ErrorStatus;
import PATATA.global.error.exception.ExceptionHandler;
import PATATA.global.error.exception.JwtHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String JWT_SECRET;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final UserDetailServiceImpl userDetailService;
    private static final String IDENTITY_TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long accesstokenValidTime = 1000L * 60 * 60 * 24; // 1d
    private Long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7; // 7d

    //Secret Key 인코딩
    public String encodeBase64SecretKey(String secretKey) {
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //JWT 서명에 사용할 Secret Key 생성
    private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // access token 생성
    public String generateAccessToken(Long memberId) {
        Date now = new Date();
        String base64EncodedSecretKey = encodeBase64SecretKey("" + JWT_SECRET);  //사용자 설정 secret 키 인코딩
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);            //JWT 서명에 사용할 키 생성

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)   // JWT 헤더 설정, "typ" : "JWT"
                .setIssuedAt(now)       // JWT 발행 일자 설정
                .setSubject(String.valueOf(memberId))  // JWT sub 설정
                .setExpiration(new Date(now.getTime() + accesstokenValidTime))  // JWT 만료 일자 설정(1d)
                .claim("memberId", memberId)  // 커스텀 클레임 설정
                .signWith(key)          // 서명을 위한 Key 객체 설정
                .compact();             // JWT 생성 및 직렬화
    }

    // refresh token 생성
    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        String base64EncodedSecretKey = encodeBase64SecretKey("" + JWT_SECRET);
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setIssuedAt(now)
                .setSubject(memberId.toString())
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .claim("memberId", memberId)
                .signWith(key)
                .compact();
    }

    // JWT 토큰 으로부터 memberId 추출
    public Long getMemberIdFromJwtToken(String token) {
        try {
            String base64EncodedSecretKey = encodeBase64SecretKey("" + JWT_SECRET);
            Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch(Exception e) {
            throw new JwtException(e.getMessage());
        }
    }

    // token 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null; // 혹은 적절한 예외 처리
    }

    // 토큰 유효성 + 만료일자 확인
    public Boolean validateTokenBoolean(String token) {
        Date now = new Date();

        try {
            String base64EncodedSecretKey = encodeBase64SecretKey("" + JWT_SECRET);
            Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

            // 주어진 토큰을 파싱하고 검증.
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date(now.getTime()));
        } catch (JwtException e) {
            // JWT 검증 실패 시 JwtHandler 예외 던지기
            throw new JwtHandler(ErrorStatus.ACCESS_TOKEN_UNAUTHORIZED);
        }
    }

    //JWT 토큰 인증 정보 조회 (토큰 복호화)
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailService.loadUserByUsername(this.getMemberIdFromJwtToken(token).toString());
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public Map<String, String> parseHeader(final String appleToken) {
        try {
            final String encodedHeader = appleToken.split(IDENTITY_TOKEN_VALUE_DELIMITER)[HEADER_INDEX];
            final String decodedHeader = new String(Base64.getUrlDecoder().decode(encodedHeader));
            return objectMapper.readValue(decodedHeader, Map.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException("apple token 값이 jwt 형식인지, 값이 정상적인지 확인해주세요.");
        } catch (JsonProcessingException e) {
            throw new ExceptionHandler(ErrorStatus.INVALID_APPLE_ID_TOKEN);
        }
    }

    public Claims getTokenClaims(final String token, final PublicKey publicKey) {
        try {
            return Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            throw new ExceptionHandler(ErrorStatus.INVALID_APPLE_ID_TOKEN);
        }
    }
}
