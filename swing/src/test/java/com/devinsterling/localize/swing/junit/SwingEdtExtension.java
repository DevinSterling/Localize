package com.devinsterling.localize.swing.junit;

import com.devinsterling.localize.swing.TestUtil;

import org.junit.jupiter.api.extension.*;

import javax.swing.SwingUtilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SwingEdtExtension implements InvocationInterceptor {
    // Ensures methods annotated with @Test run on the Swing EDT thread
    @Override public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext
    ) throws Throwable {
        runOnEdtThread(invocation);
    }

    // Ensures methods annotated with @ParameterizedTest run on the Swing EDT thread
    @Override public void interceptTestTemplateMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext
    ) throws Throwable {
        runOnEdtThread(invocation);
    }

    private static void runOnEdtThread(Invocation<Void> invocation) throws Throwable {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    invocation.proceed();
                } catch (Throwable t) {
                    TestUtil.sneakyThrow(t);
                }
            });
        }
        // Swing wraps any caught exceptions, so unwrap it to get the original exception
        catch (InvocationTargetException exception) {
            // `getCause` will never return `null` here.
            // > See `EventQueue#invokeAndWait(Object, Runnable)`
            throw exception.getCause();
        }
    }
}
