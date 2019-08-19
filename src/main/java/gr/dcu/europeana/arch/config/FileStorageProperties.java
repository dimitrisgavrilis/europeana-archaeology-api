package gr.dcu.europeana.arch.config;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Vangelis Nomikos
 */
@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties (prefix = "file")
public class FileStorageProperties {
    
    public static final String UPLOAD_MAPPING_DIR     = "upload";
    public static final String UPLOAD_EDM_PACKAGE_DIR = "upload_edm";
    public static final String EXPORT_MAPPING_DIR     = "export";
    
    
    @NotBlank
    private String storageHome;
}
