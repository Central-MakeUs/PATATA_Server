package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class SpotHandler extends GeneralException{
    public SpotHandler(BaseErrorCode code) {
        super(code);
    }
}
