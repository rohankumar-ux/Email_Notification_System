package com.example.EmailNotificationSystem.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void ResourceNotFoundException_ConstructorWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void ResourceNotFoundException_Inheritance() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void DuplicateResourceException_ConstructorWithMessage() {
        String message = "Duplicate resource";
        DuplicateResourceException exception = new DuplicateResourceException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void DuplicateResourceException_Inheritance() {
        DuplicateResourceException exception = new DuplicateResourceException("Test message");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void EmailSendException_ConstructorWithMessage() {
        String message = "Failed to send email";
        EmailSendException exception = new EmailSendException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void EmailSendException_Inheritance() {
        EmailSendException exception = new EmailSendException("Test message");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void ResourceNotFoundException_NullMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void DuplicateResourceException_NullMessage() {
        DuplicateResourceException exception = new DuplicateResourceException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void EmailSendException_NullMessage() {
        EmailSendException exception = new EmailSendException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void ResourceNotFoundException_EmptyMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void DuplicateResourceException_EmptyMessage() {
        DuplicateResourceException exception = new DuplicateResourceException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void EmailSendException_EmptyMessage() {
        EmailSendException exception = new EmailSendException("");

        assertEquals("", exception.getMessage());
    }
}
