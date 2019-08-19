package gr.dcu.europeana.arch.api.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@NoArgsConstructor
public class EnrichDetails {
    
    private boolean success;
    private String message;
    
    private String edmArchiveName;
    private int edmFileCount;
    
    private String enrichedArchiveName;
    private int enrichedFileCount;
    private String enrichedArchiveUrl;
    
}
