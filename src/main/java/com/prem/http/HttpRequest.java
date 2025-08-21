package com.prem.http;

public class HttpRequest extends HttpMessage {

    private HttpMethod method;

    public HttpMethod getMethod() {
        return method;
    }

    void setMethod(HttpMethod method) {
        this.method = method;
    }

    private String requestTarget;
    private String httpVersion;

    HttpRequest() {
    }

    public void setMethod(String methodName) throws HttpParsingException {
        for (HttpMethod method : HttpMethod.values()) {
            if (method.name().equalsIgnoreCase(methodName)) {
                this.method = method;
                return;
            }
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
        }
    }
}
