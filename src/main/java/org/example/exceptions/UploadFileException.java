package org.example.exceptions;

public class UploadFileException extends Exception {

    private final String message;

    public UploadFileException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
