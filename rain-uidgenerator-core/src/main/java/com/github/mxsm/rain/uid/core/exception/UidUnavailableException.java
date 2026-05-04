package com.github.mxsm.rain.uid.core.exception;

import com.github.mxsm.rain.uid.core.common.ErrorCode;

public class UidUnavailableException extends UidGenerateException {

    public UidUnavailableException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UidUnavailableException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
