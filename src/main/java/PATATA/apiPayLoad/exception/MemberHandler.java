package PATATA.apiPayLoad.exception;

import PATATA.apiPayLoad.code.BaseErrorCode;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}
