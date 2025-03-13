package com.devinsterling.localize.examples;

import com.devinsterling.localize.fx.LocalizeFX;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class ClickCount extends Application {

    @Override public void start(Stage stage) {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        localize.putBundleProvider("Provider1", locale -> ResourceBundle.getBundle("messages", locale));

        // Properties
        DoubleProperty clickCount = new SimpleDoubleProperty();

        // Controls
        Button clickButton = new Button();
        Button changeLocale = new Button();
        Button resetButton = new Button();
        Label label = new Label();
        TextField textField = new TextField("Snowball");

        // Binding
        clickButton.textProperty().bind(localize.getBinding("MyApp.clickMe"));
        changeLocale.textProperty().bind(localize.getBinding("MyApp.changeLocale"));
        resetButton.textProperty().bind(localize.getBinding("MyApp.reset"));
        textField.promptTextProperty().bind(localize.getBinding("MyApp.prompt"));
        label.textProperty().bind(localize.get("MyApp.clickMessage")
                                          .arg("click_count", clickCount)
                                          .arg("name", textField.textProperty())
                                          .binding());

        // Actions
        clickButton.setOnAction(
                event -> clickCount.set(clickCount.get() + 1));
        changeLocale.setOnAction(
                event -> localize.setLocale(localize.getLocale() == Locale.ENGLISH ? Locale.JAPANESE : Locale.ENGLISH));
        resetButton.setOnAction(event -> {
            clickCount.set(0);
            textField.setText("");
        });

        // Arrangement
        HBox buttons = new HBox(clickButton, changeLocale, resetButton);
        VBox content = new VBox(buttons, label, textField);
        StackPane container = new StackPane(content);

        buttons.setSpacing(5);
        content.setSpacing(5);
        content.setMaxWidth(300);
        content.setMaxHeight(VBox.USE_PREF_SIZE);

        stage.setScene(new Scene(container, 400, 150));
        stage.show();
    }
}
