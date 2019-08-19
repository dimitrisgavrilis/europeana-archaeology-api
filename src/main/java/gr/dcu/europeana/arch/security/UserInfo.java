package gr.dcu.europeana.arch.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vangelis Nomikos
 */
@Component
@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
class UserInfo {
    
   private long userId;
}