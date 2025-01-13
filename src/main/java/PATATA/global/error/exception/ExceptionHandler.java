package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class ExceptionHandler extends GeneralException {
    public ExceptionHandler(BaseErrorCode code) {
        super(code);
    }
}
