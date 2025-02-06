package PATATA.global.error.exception;

import PATATA.global.error.code.BaseErrorCode;

public class ReportHandler extends GeneralException {
    public ReportHandler(BaseErrorCode code) {
        super(code);
    }
}
