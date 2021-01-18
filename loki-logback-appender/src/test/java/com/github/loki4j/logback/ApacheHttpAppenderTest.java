package com.github.loki4j.logback;

import java.util.Random;

import com.github.loki4j.testkit.dummy.LokiHttpServerMock;

import static com.github.loki4j.logback.Generators.*;
import static com.github.loki4j.logback.Loki4jAppenderTest.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApacheHttpAppenderTest {

    private static int testPort = -1;
    private static LokiHttpServerMock mockLoki;
    private static String url;
    static String expected =
            "LogRecord [ts=100, nanos=1, stream=level=INFO,app=my-app, message=l=INFO c=test.TestApp t=thread-1 | Test message 1 ]\n" +
                    "LogRecord [ts=107, nanos=3, stream=level=INFO,app=my-app, message=l=INFO c=test.TestApp t=thread-1 | Test message 3 ]\n" +
                    "LogRecord [ts=104, nanos=2, stream=level=WARN,app=my-app, message=l=WARN c=test.TestApp t=thread-2 | Test message 2 ]\n";
    @BeforeClass
    public static void startMockLoki() {
        testPort = 20_000 + new Random().nextInt(10_000);
        mockLoki = lokiMock(testPort);
        mockLoki.start();

        url = String.format("http://localhost:%s/loki/api/v1/push", testPort);
    }

    @AfterClass
    public static void stopMockLoki() {
        mockLoki.stop();
    }

    @Before
    public void resetMockLoki() {
        mockLoki.reset();
    }

    @Test
    public void testApacheHttpSend() {
        withAppender(appender(3, 1000L, defaultToStringEncoder(), apacheHttpSender(url)), a -> {
            a.appendAndWait(events[0], events[1]);
            assertTrue("no batches before batchSize reached", mockLoki.lastBatch == null);

            a.appendAndWait(events[2]);
            assertEquals("http send", expected, new String(mockLoki.lastBatch));

            return null;
        });
    }

}
