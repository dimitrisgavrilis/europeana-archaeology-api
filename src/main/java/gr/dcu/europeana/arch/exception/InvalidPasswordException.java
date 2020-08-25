package gr.dcu.europeana.arch.exception;

public class InvalidPasswordException extends RuntimeException {
    
    public InvalidPasswordException(Long id) {
        super("Invalid password exception. (e.g. password cannot be blank)");
    }
}
