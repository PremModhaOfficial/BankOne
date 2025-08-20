
package com.prem.http;

public enum HttpStatusCode {

    // client errors //
    CLIENT_ERROR_400_BAD_REQUEST(400, "Bad Request"),
    CLIENT_ERROR_401_METHOD_NOT_ALLOWED(401, "Method Not Allowed"),
    CLIENT_ERROR_414_URI_TOO_LONG(414, "URI Too Long"),
    // server errors //
    SERVER_ERROR_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVER_ERROR_501_NOT_IMPLEMENTED(501, "Not Emplemented");

    public final int STATUS_CODE;
    public final String MESSAGE;

    HttpStatusCode(int STATUS_CODE, String MESSAGE) {
        this.STATUS_CODE = STATUS_CODE;
        this.MESSAGE = MESSAGE;
    }
}
