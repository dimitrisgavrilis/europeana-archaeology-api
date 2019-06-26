package gr.dcu.share3d.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Vangelis Nomikos
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

     public ResourceNotFoundException(Long id) {
        super("Resource not found. ID " + id);
    }
}
