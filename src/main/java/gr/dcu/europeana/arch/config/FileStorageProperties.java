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
    
    @NotBlank
    private String storageHome;
    
    public static final String EUROPEANA_ARCH_HOME_DIR = "europeana_arch";
    public static final String MAPPINGS_HOME_DIR       = EUROPEANA_ARCH_HOME_DIR + "/mappings";
    public static final String PACKAGES_HOME_DIR       = EUROPEANA_ARCH_HOME_DIR + "/packages";
    
    // public static final String UPLOAD_MAPPING_DIR      = "uploads";
    // public static final String UPLOAD_EDM_PACKAGE_DIR  = "upload_edm";
    // public static final String EXPORT_MAPPING_DIR      = "exports";
}
