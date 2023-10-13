package com.shudun.dms.Exception;

public class VerifyMessageException extends RuntimeException {
    public VerifyMessageException() {
    }

    public VerifyMessageException(String s) {
        super(s);
    }

    public VerifyMessageException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VerifyMessageException(Throwable throwable) {
        super(throwable);
    }

    public VerifyMessageException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
