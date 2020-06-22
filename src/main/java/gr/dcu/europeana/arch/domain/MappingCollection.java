package gr.dcu.europeana.arch.domain;

import java.util.List;

import gr.dcu.europeana.arch.domain.entity.SubjectTermEntity;
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
    
    private List<SubjectTermEntity> mappings;
    
}
