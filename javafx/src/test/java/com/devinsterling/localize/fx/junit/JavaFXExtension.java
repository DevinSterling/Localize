package com.devinsterling.localize.fx.junit;

import com.devinsterling.localize.fx.TestUtil;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

public class JavaFXExtension implements InvocationInterceptor {

    static {
        TestUtil.startupJavaFXThread();
    }

    // Ensures methods annotated with @Test run on the JavaFX application thread
    @Override public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext
    ) {
        runOnJavaFXThreadAndWait(invocation);
    }

    // Ensures methods annotated with @ParameterizedTest run on the JavaFX application thread
    @Override public void interceptTestTemplateMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext
    ) {
        runOnJavaFXThreadAndWait(invocation);
    }

    private static void runOnJavaFXThreadAndWait(Invocation<Void> invocation) {
        TestUtil.runOnJavaFXThreadAndWait(() -> {
            try {
                invocation.proceed();
            } catch (Throwable t) {
                TestUtil.sneakyThrow(t);
            }
        });
    }
}
