package gr.dcu.share3d.exception;

/**
 *
 * @author Vangelis Nomikos
 */
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
