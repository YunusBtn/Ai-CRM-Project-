package com.yunus.exception;

public class BusinessException extends RuntimeException {

    private final ErrorType errorType;

    public BusinessException(ErrorType errorType, String detailMessage) {
        super(errorType.getMessage() + " " + detailMessage);
        this.errorType = errorType;
    }
    public ErrorType getErrorType() {
        return errorType;
    }


}

