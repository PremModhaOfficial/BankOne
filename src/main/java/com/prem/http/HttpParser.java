package com.prem.http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    public HttpRequest parseHttpRequest(InputStream inputStream) {

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        HttpRequest request = new HttpRequest();

        parseRequestLine(reader, request);
        parseHeaders(reader, request);
        parseBody(reader, request);
    }

    private void parseBody(InputStreamReader reader, HttpRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseBody'");
    }

    private void parseHeaders(InputStreamReader reader, HttpRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseHeaders'");
    }

    private void parseRequestLine(InputStreamReader reader, HttpRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseRequestLine'");
    }

}
