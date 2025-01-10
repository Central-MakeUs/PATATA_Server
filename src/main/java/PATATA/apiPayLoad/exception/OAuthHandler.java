package PATATA.apiPayLoad.exception;

import PATATA.apiPayLoad.code.BaseErrorCode;

public class OAuthHandler extends GeneralException {
    public OAuthHandler(BaseErrorCode code) {
        super(code);
    }
}
