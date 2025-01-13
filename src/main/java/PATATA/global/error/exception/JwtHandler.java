package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class JwtHandler extends GeneralException {
    public JwtHandler(BaseErrorCode code) {
        super(code);
    }
}
