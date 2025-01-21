package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;
import PATATA.global.error.code.DynamicErrorCode;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
    public MemberHandler(String message) {
        super(new DynamicErrorCode(message));
    }
}
