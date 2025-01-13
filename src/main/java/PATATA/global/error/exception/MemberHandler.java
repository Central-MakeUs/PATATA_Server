package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}
