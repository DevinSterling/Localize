package com.devinsterling.localize.swing.fixtures;

public class TextSetter {
    public RuntimeException exception = new SetTextException();
    public boolean throwOnSetText;
    public String text;
    public int count;

    public TextSetter throwOnSetText(boolean throwOnSetText) {
        this.throwOnSetText = throwOnSetText;
        return this;
    }

    public TextSetter exception(RuntimeException exception) {
        this.exception = exception;
        return this;
    }

    public void setText(String text) {
        if (throwOnSetText) {
            throw exception;
        }
        this.text = text;
        count++;
    }

    public static class SetTextException extends RuntimeException {}
}
