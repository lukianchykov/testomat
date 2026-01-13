package io.testomat.junit5;

import com.google.gson.JsonObject;
import io.testomat.junit5.annotations.TestId;
import io.testomat.junit5.annotations.Title;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

@Slf4j
public class TestomatReporterExtension implements
        BeforeAllCallback, AfterAllCallback, TestWatcher {
    
    private static final TestomatApiClient API_CLIENT = new TestomatApiClient();
    private static String globalTestRunUid = null;
    private static long suiteStartTimeMillis;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (globalTestRunUid == null) {
            suiteStartTimeMillis = System.currentTimeMillis();
            String runTitle = "My Awesome Junit 5 Test Run " + java.time.LocalDateTime.now();
            Optional<Title> classTitle = AnnotationSupport.findAnnotation(context.getRequiredTestClass(), Title.class);
            if (classTitle.isPresent()) {
                runTitle = classTitle.get().value();
            }

            log.info("Creating Testomat.io test run: " + runTitle);
            Optional<String> response = API_CLIENT.createTestRun(runTitle);
            response.ifPresent(json -> {
                JsonObject jsonObject = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                globalTestRunUid = jsonObject.get("uid").getAsString();
                log.info("Testomat.io run created with UID: " + globalTestRunUid);
            });
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (globalTestRunUid != null) {
            long duration = System.currentTimeMillis() - suiteStartTimeMillis;
            log.info("Finishing Testomat.io test run " + globalTestRunUid + " with duration " + duration + "ms");
            API_CLIENT.finishTestRun(globalTestRunUid, duration / 1000.0);
            globalTestRunUid = null;
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        reportTestResult(context, "passed", null, null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        reportTestResult(context, "failed", cause.getMessage(), getStackTrace(cause));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        reportTestResult(context, "skipped", "Test aborted: " + cause.getMessage(), null);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        reportTestResult(context, "skipped", reason.orElse("Test disabled"), null);
    }

    private void reportTestResult(ExtensionContext context, String status, String message, String stack) {
        if (globalTestRunUid == null) {
            log.warn("Testomat.io run UID is not initialized. Skipping test report for " + context.getDisplayName());
            return;
        }

        String methodName = context.getRequiredTestMethod().getName();
        String className = context.getRequiredTestClass().getSimpleName();
        String file = context.getRequiredTestClass().getName().replace('.', '/') + ".java";

        String testId = AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), TestId.class)
                .map(TestId::value)
                .orElse(null);

        String testTitle = AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), Title.class)
                .map(Title::value)
                .orElse(methodName);

        JsonObject payload = new JsonObject();
        payload.addProperty("title", testTitle);
        if (testId != null) {
            payload.addProperty("test_id", testId);
        }
        payload.addProperty("suite_title", className);
        payload.addProperty("file", file);
        payload.addProperty("status", status);
        if (message != null) {
            payload.addProperty("message", message);
        }
        if (stack != null) {
            payload.addProperty("stack", stack);
        }

        log.info(String.format("Reporting test '%s' with status '%s' to Testomat.io", testTitle, status));
        API_CLIENT.reportTest(globalTestRunUid, payload);
    }

    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}