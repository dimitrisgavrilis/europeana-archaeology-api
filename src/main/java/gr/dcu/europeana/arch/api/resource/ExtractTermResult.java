package gr.dcu.europeana.arch.api.resource;

import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.model.TemporalTerm;
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
public class ExtractTermResult {
    
    private List<SubjectTerm> subjectTerms;
    private List<SpatialTerm> spatialTerms;
    private List<TemporalTerm> temporalTerms;
    
}
