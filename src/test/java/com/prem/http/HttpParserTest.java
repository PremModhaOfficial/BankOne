package com.prem.http;

import junit.framework.TestCase;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import java.io.ByteArrayInputStream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpParserTest extends TestCase {

    private HttpParser httpParser;

    @BeforeAll
    public void beforeClass() {
        httpParser = new HttpParser();
    }

    public void testParseHttpRequest() {
    }

    private InputStream generateValidTestCase() {
        String rawData = "GET / HTTP/2.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:141.0) Gecko/20100101 Firefox/141.0\r\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                "Accept-Language: en-US,en;q=0.5\r\n" +
                "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
                "Sec-GPC: 1\r\n" +
                "Connection: keep-alive\r\n" +
                "Upgrade-Insecure-Requests: 1\r\n" +
                "Sec-Fetch-Dest: document\r\n" +
                "Sec-Fetch-Mode: navigate\r\n" +
                "Sec-Fetch-Site: none\r\n" +
                "Sec-Fetch-User: ?1\r\n" +
                "Priority: u=0, i\r\n" +
                "\r\n";

        InputStream inputStream = new ByteArrayInputStream(rawData.getBytes(StandardCharsets.US_ASCII));

        return inputStream;
    }
}
