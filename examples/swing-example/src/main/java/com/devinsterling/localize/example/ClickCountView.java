package com.devinsterling.localize.example;

import com.devinsterling.localize.swing.Trigger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ClickCountView extends JPanel {

    public ClickCountView() {
        build();
    }

    private void build() {
        AtomicInteger clickCount = new AtomicInteger();
        JButton clickButton = new JButton();
        JButton changeLocale = new JButton();
        JButton resetButton = new JButton();
        JLabel label = new JLabel();
        JTextField textField = new JTextField("Snowball");

        Example.LOCALIZE.bind("MyApp.clickMe", clickButton);
        Example.LOCALIZE.bind("MyApp.changeLocale", changeLocale);
        Example.LOCALIZE.bind("MyApp.reset", resetButton);
        Example.LOCALIZE.get("MyApp.clickMessage")
               .arg("name", textField)
               .arg("click_count", clickCount)
               .on(Trigger.action(clickButton))
               .on(Trigger.action(resetButton))
               .bind(label);

        clickButton.addActionListener(_ -> clickCount.getAndIncrement());
        resetButton.addActionListener(_ -> {
            clickCount.set(0);
            textField.setText("Snowball");
        });
        changeLocale.addActionListener(_ -> Example.LOCALIZE.setLocale(switch (Example.LOCALIZE.getLocale()) {
            case Locale l when l.equals(Locale.ENGLISH) -> Locale.JAPANESE;
            case Locale l when l.equals(Locale.JAPANESE) -> Locale.KOREAN;
            case Locale l when l.equals(Locale.KOREAN) -> Locale.CHINESE;
            default -> Locale.ENGLISH;
        }));

        textField.setSize(500, 30);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(clickButton);
        buttonPanel.add(changeLocale);
        buttonPanel.add(resetButton);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new GridLayout(3, 1, 0, 5));
        mainContent.add(buttonPanel);
        mainContent.add(label);
        mainContent.add(textField);
        mainContent.setSize(new Dimension(300, 300));

        setLayout(new GridBagLayout());
        add(mainContent);
    }
}
