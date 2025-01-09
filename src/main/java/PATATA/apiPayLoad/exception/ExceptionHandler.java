package PATATA.apiPayLoad.exception;

import PATATA.apiPayLoad.code.BaseErrorCode;

public class ExceptionHandler extends GeneralException {
    public ExceptionHandler(BaseErrorCode code) {
        super(code);
    }
}
