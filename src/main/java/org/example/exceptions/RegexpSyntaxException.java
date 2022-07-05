package org.example.exceptions;

public class RegexpSyntaxException extends Exception {
    private final String message;

    public RegexpSyntaxException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
