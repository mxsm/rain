package com.github.mxsm.rain.uid.core.common;

/**
 * Stable machine-readable error codes shared by server and SDK.
 */
public enum ErrorCode {

    SUCCESS,

    VALIDATION_ERROR,

    UNAUTHORIZED,

    BIZ_CODE_NOT_FOUND,

    SEGMENT_ALLOCATE_FAILED,

    UID_UNAVAILABLE,

    CLOCK_MOVED_BACKWARDS,

    WORKER_ID_UNAVAILABLE,

    UPSTREAM_UNAVAILABLE,

    INTERNAL_ERROR
}
