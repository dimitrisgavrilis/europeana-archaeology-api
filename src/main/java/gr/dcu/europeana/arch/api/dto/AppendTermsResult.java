package gr.dcu.europeana.arch.api.dto;

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
public class AppendTermsResult {
    
    private long existingTermCount;
    private long appendTermCount;
    
}
