package net.sonmoosans.u3.api.core;

public class APIException extends RuntimeException {
    public final int code;

    public APIException(int code) {
        super("HTTP error code : " + code);
        this.code = code;
    }
}
