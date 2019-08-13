package gr.dcu.europeana.arch.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "aat_subjects")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AatSubject implements Serializable  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
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

    /*
    @Override
    public String toString() {
        return "AatSubject{" + "id=" + id + ", aatId=" + aatId + ", name=" + name + ", uri=" + uri + ", path=" + path + ", type=" + type + '}';
    }*/
    
    
    
}
