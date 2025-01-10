package PATATA.apiPayLoad.exception;

import PATATA.apiPayLoad.code.BaseErrorCode;

public class JwtHandler extends GeneralException {
    public JwtHandler(BaseErrorCode code) {
        super(code);
    }
}
