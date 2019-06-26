package gr.dcu.share3d.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Vangelis Nomikos
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class AuthorizationException extends RuntimeException {
    
      public AuthorizationException(String message) {
        super(message);
    }
    
}