package com.devinsterling.localize.example;

import com.devinsterling.localize.event.Subscription;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Locale;

public class ControlsDemoView extends JPanel {
    private Subscription localeListener = Subscription.EMPTY;
    private JComboBox<LocaleOption> localeSelector;

    public ControlsDemoView() {
        build();
    }

    @Override public void addNotify() {
        super.addNotify();

        // Register a locale listener to update the selector whenever the locale changes
        localeListener = Example.LOCALIZE.addLocaleListener((_, newLocale) ->
            localeSelector.setSelectedItem(new LocaleOption(newLocale))
        );

        // Set initial selected locale
        localeSelector.setSelectedItem(new LocaleOption(Example.LOCALIZE.getLocale()));
    }

    @Override public void removeNotify() {
        super.removeNotify();

        // When this panel goes out of scope, dispose the locale listener
        localeListener.dispose();
    }

    private void build() {
        localeSelector = new JComboBox<>(new LocaleOption[] {
            new LocaleOption(Locale.ENGLISH),
            new LocaleOption(Locale.KOREAN),
            new LocaleOption(Locale.CHINESE),
            new LocaleOption(Locale.JAPANESE)
        });
        localeSelector.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                LocaleOption option = (LocaleOption) event.getItem();
                Example.LOCALIZE.setLocale(option.locale());
            }
        });

        JLabel greeting = new JLabel();
        JTextField name = new JTextField("Snowball", 15);
        JButton button = new JButton();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        JComboBox<String> combo = new JComboBox<>(new String[] { "A", "B", "C" });
        JProgressBar progress = new JProgressBar(0, 100);
        JSlider slider = new JSlider(0, 100, 50);

        progress.setValue(50);
        slider.addChangeListener(_ -> progress.setValue(slider.getValue()));
        button.addActionListener(_ -> slider.setValue(slider.getValue() + 5));

        // Label with dynamic text field argument
        Example.LOCALIZE.get("app.label.greeting")
               .arg("name", name)
               .bind(greeting);

        // Button
        Example.LOCALIZE.get("app.button.update")
               .bind(button);

        // Progress bar
        Example.LOCALIZE.get("app.progress.value")
               .arg("name", name)
               .arg("value", progress)
               .bind(progress);

        // Slider
        JLabel sliderLabel = new JLabel();
        Example.LOCALIZE.get("app.slider.value")
               .arg("value", slider)
               .bind(sliderLabel);

        // Spinner
        JLabel spinnerLabel = new JLabel();
        Example.LOCALIZE.get("app.spinner.value")
               .arg("value", spinner)
               .bind(spinnerLabel);

        // Combo
        JLabel comboLabel = new JLabel();
        Example.LOCALIZE.get("app.combo.value")
               .arg("value", combo)
               .bind(comboLabel);

        setLayout(new GridLayout(0, 1));
        add(localeSelector);
        add(greeting);
        add(name);
        add(button);
        add(progress);
        add(sliderLabel);
        add(slider);
        add(spinnerLabel);
        add(spinner);
        add(comboLabel);
        add(combo);
    }

    record LocaleOption(Locale locale) {
        @Override public String toString() {
            return locale.getDisplayLanguage(locale);
        }
    }
}
