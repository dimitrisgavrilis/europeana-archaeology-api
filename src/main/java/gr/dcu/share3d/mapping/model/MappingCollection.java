package gr.dcu.share3d.mapping.model;

import gr.dcu.share3d.mapping.model.MappingTerm;
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
    
    private List<MappingTerm> mappings;
    
}
