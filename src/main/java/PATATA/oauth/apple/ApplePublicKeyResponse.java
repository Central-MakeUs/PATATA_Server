package PATATA.oauth.apple;

import PATATA.apiPayLoad.code.status.ErrorStatus;
import PATATA.apiPayLoad.exception.ExceptionHandler;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ApplePublicKeyResponse {
    private List<ApplePublicKey> keys;

    public ApplePublicKey getMatchedKeyBy(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findAny()
                .orElseThrow(() -> new ExceptionHandler(ErrorStatus.INVALID_APPLE_ID_TOKEN_INFO));
    }
}
