package PATATA.global.error.code.status;

import PATATA.global.error.code.BaseErrorCode;
import PATATA.global.error.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    //일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다. 로그인 정보를 확인해주세요."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다"),

    //JWT 토큰 에러
    TOKEN_EMPTY(HttpStatus.BAD_REQUEST, "TOKEN4000", "토큰값이 존재하지 않습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "TOKEN4001", "잘못된 토큰 형식입니다."),
    ACCESS_TOKEN_UNAUTHORIZED(HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE, "TOKEN4002", "유효하지 않은 AccessToken입니다."),
    REFRESH_TOKEN_UNAUTHORIZED(HttpStatus.I_AM_A_TEAPOT, "TOKEN4003", "유효하지 않은 RefreshToken입니다. 다시 로그인하세요."),

    //OAuth 토큰 에러
    INVALID_APPLE_ID_TOKEN(HttpStatus.UNAUTHORIZED, "OAUTH4000", "APPLE identityToken 값이 올바르지 않습니다."),
    INVALID_APPLE_ID_TOKEN_INFO(HttpStatus.UNAUTHORIZED, "OAUTH4001", "APPLE identityToken 값의 alg, kid 정보가 올바르지 않습니다."),
    APPLE_ID_TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "OAUTH4002", "APPLE ID TOKEN 값이 비어 있습니다."),
    APPLE_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH4003", "APPLE LOGIN 실패"),
    INVALID_GOOGLE_ID_TOKEN(HttpStatus.UNAUTHORIZED, "OAUTH4004", "GOOGLE ID TOKEN 값이 올바르지 않습니다."),
    TOKEN_VALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH4005", "GOOGLE ID TOKEN 검증에 실패했습니다."),
    GOOGLE_ID_TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "OAUTH4006", "GOOGLE ID TOKEN 값이 비어 있습니다."),

    //Member 에러
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "해당하는 사용자를 찾을 수 없습니다."),

    //Spot 에러
    SPOT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SPOT4000", "해당하는 스팟을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "SPOT4001", "해당하는 카테고리를 찾을 수 없습니다."),

    //S3 이미지 에러
    IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "IMAGE4000", "이미지가 첨부되지 않았습니다."),
    IMAGE_NOT_SAVE(HttpStatus.BAD_REQUEST, "IMAGE4001", "이미지 저장에 실패했습니다." ),
    S3_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "S34000", "이미지 업로드에 실패하였습니다."),
    NO_FILE_EXTENTION(HttpStatus.BAD_REQUEST, "S34001", "해당되는 파일 확장자가 없습니다."),
    INVALID_FILE_EXTENTION(HttpStatus.BAD_REQUEST, "S34002", "유효하지 않은 파일 확장자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}


