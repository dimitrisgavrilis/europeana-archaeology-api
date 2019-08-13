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
}
