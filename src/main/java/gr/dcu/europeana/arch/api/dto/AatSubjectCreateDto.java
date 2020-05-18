package gr.dcu.europeana.arch.api.dto;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class AatSubjectCreateDto implements Serializable  {

    @Column (name="aat_uid")
    private String aatUid;
    
    @Column (name="label")
    private String label;
    
    @Column
    private String uri;
    
    @Column (name="lod_uri")
    private String lodUri;
    
    @Column
    private String path;
    
    @Column
    private String type;
}
