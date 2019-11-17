package gr.dcu.europeana.arch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String entityName) {
        super("Not found. " + entityName);
    }
    
    public NotFoundException(String entityName, Integer id) {
        super("Not found. " + entityName + " with id " + id);
    }
     
    public NotFoundException(String entityName, Long id) {
        super("Not found. " + entityName + " with id " + id);
    }
    
    public NotFoundException(String entityName, String id) {
        super("Not found. " + entityName + " with id " + id);
    }

    public NotFoundException(String entityName, String name1, Long id1, String name2, Long id2) {
        super("Not found. " + entityName + " with " + name1 + " " + id1 + " and " + name2 + " " + id2);
    }

    public NotFoundException(String entityName, String keyName, String keyValue) { 
        super("Not found. " + entityName + " with " + keyName + " " + keyValue); }
}
