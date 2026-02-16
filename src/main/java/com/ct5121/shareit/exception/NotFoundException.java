package com.ct5121.shareit.exception;


public class NotFoundException extends RuntimeException {
    public NotFoundException(final String message) {
        super(message);
    }
}