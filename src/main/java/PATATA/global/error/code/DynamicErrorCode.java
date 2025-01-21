package PATATA.global.error.code;
import org.springframework.http.HttpStatus;

/**
 * @param message 동적 메시지
 */
public record DynamicErrorCode(String message) implements BaseErrorCode {
    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code("MEMBER4001")
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code("MEMBER4001")
                .isSuccess(false)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
    }
}
