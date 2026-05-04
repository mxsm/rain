package com.github.mxsm.rain.uid.handler;

import com.github.mxsm.rain.uid.core.common.ErrorCode;
import com.github.mxsm.rain.uid.core.common.Result;
import com.github.mxsm.rain.uid.core.exception.UidGenerateException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class CommonExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder("validate error,");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage()).append(",");
        }
        String msg = sb.substring(0, sb.length() - 1);
        return Result.buildError(null, ErrorCode.VALIDATION_ERROR, msg);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.buildError(null, ErrorCode.VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler({UidGenerateException.class})
    @ResponseBody
    public ResponseEntity<Result<Void>> handleUidGenerateException(UidGenerateException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case BIZ_CODE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UID_UNAVAILABLE, SEGMENT_ALLOCATE_FAILED, CLOCK_MOVED_BACKWARDS, WORKER_ID_UNAVAILABLE ->
                HttpStatus.SERVICE_UNAVAILABLE;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
            .body(Result.buildError(null, ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Result.buildError(null, ErrorCode.VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result<Void> handleException(Exception ex) {
        LOGGER.error("Unhandled server exception", ex);
        return Result.buildError(null, ErrorCode.INTERNAL_ERROR, "Internal server error");
    }
}
