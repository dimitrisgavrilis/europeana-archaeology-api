package gr.dcu.europeana.arch.model;

import gr.dcu.europeana.arch.model.SubjectTerm;
import java.util.List;
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
public class MappingCollection {
    
    private List<SubjectTerm> mappings;
    
}
