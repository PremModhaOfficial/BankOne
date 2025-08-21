package com.prem.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);
    private final static int SP = 0x20, CR = 0x0d, LF = 0x0a;

    public HttpRequest parseHttpRequest(InputStream inputStream) throws IOException, HttpParsingException {

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        HttpRequest request = new HttpRequest();

        parseRequestLine(reader, request);
        parseHeaders(reader, request);
        parseBody(reader, request);

        return request;
    }

    private void parseBody(InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
        int _byte;
        StringBuilder processBuffer = new StringBuilder();

        boolean methodParsed = false, requestTargetParsed = false;

        while ((_byte = reader.read()) >= 0) {
            if (_byte == CR) {
                _byte = reader.read();
                if (_byte == LF) {
                    LOGGER.debug("Request Line VERSION : {}", processBuffer.toString());
                    return;
                }
            }

            if (_byte == SP) {
                if (!methodParsed) {
                    LOGGER.debug("Request Line METHOD  : {}", processBuffer.toString());
                    methodParsed = true;
                    request.setMethod(processBuffer.toString());
                } else if (!requestTargetParsed) {
                    LOGGER.debug("Request Line REQ TARG  : {}", processBuffer.toString());
                    requestTargetParsed = true;
                }

                processBuffer.delete(0, processBuffer.length());
            } else {
                processBuffer.append((char) _byte);
            }

        }
    }

    private void parseHeaders(InputStreamReader reader, HttpRequest request) {
    }

    private void parseRequestLine(InputStreamReader reader, HttpRequest request) {
    }

}
