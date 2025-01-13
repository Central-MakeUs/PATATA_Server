package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class OAuthHandler extends GeneralException {
    public OAuthHandler(BaseErrorCode code) {
        super(code);
    }
}
