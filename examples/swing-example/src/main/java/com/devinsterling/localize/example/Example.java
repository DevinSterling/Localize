package com.devinsterling.localize.example;

import com.devinsterling.localize.swing.LocalizeSwing;
import com.devinsterling.localize.swing.Trigger;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.util.Locale;
import java.util.ResourceBundle;

public class Example {
    // Because `Localize` instances are thread-safe,
    // there two recommended options for passing instances through an application:
    // 1. Static app-wide singleton (used here in this example)
    // 2. Dependency Injection (e.g., using a framework such as Spring, Guice, Dagger, etc.)
    public static final LocalizeSwing LOCALIZE = LocalizeSwing.of(Locale.ENGLISH);

    static {
        LOCALIZE.addBundleProvider(locale -> ResourceBundle.getBundle("messages", locale));
        // Alternatively, reference a resource bundle by the base name directly.
        LOCALIZE.addBundleProvider("demo");
    }

    public static void main(String[] args) {
        new Example().start();
    }

    public void start() {
        JFrame frame = new JFrame();
        JTabbedPane tabs = new JTabbedPane();

        // Be notified whenever the locale changes
        LOCALIZE.addLocaleListener((_, newLocale) -> System.out.println(newLocale));

        // Frame title binding
        LOCALIZE.get("app.title")
                .arg("h", frame, JFrame::getHeight)
                .arg("w", frame, JFrame::getWidth)
                .on(Trigger.resize(frame))
                .bind(frame);

        // Click count panel
        tabs.addTab("1", new ClickCountView());
        tabs.addTab("2", new ControlsDemoView());

        frame.setContentPane(tabs);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setSize(500, 400);
    }
}
