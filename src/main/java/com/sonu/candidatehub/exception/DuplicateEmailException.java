package com.sonu.candidatehub.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("A candidate with email '" + email + "' already exists");
    }
}