package PATATA.infra.oauth.apple;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AppleClientSecretGenerator {
    private static final String AUDIENCE = "https://appleid.apple.com";

    @Value("${spring.social-login.provider.apple.key-id}")
    private String keyId;
    @Value("${spring.social-login.provider.apple.team-id}")
    private String teamId;
    @Value("${spring.social-login.provider.apple.client-id}")
    private String clientId;
    @Value("${spring.social-login.provider.apple.private-key}")
    private String privateKeyString;

    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyPEM = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // 모든 공백 제거

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC"); // 알고리즘을 필요에 맞게 설정하세요.
        return keyFactory.generatePrivate(keySpec);
    }


    public String createClientSecret() throws Exception {
        Date expirationDate =
                Date.from(LocalDateTime.now().plusHours(2).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간 - UNIX 시간
                .setExpiration(expirationDate) // 만료 시간
                .setAudience(AUDIENCE)
                .setSubject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }
}
