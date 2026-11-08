package com.devinsterling.localize.swing.fixtures;

public class TextSetter {
    public SetTextException exception = new SetTextException();
    public boolean throwOnSetText;
    public String text;

    public TextSetter throwOnSetText(boolean throwOnSetText) {
        this.throwOnSetText = throwOnSetText;
        return this;
    }

    public TextSetter exception(SetTextException exception) {
        this.exception = exception;
        return this;
    }

    public void setText(String text) {
        if (throwOnSetText) {
            throw exception;
        }
        this.text = text;
    }

    public static class SetTextException extends RuntimeException {}
}
