package gr.dcu.europeana.arch.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppendTermsResult {
    
    private long mappingTermCount;
    private long archiveTermCount;
    private long appendTermCount;
    
}
