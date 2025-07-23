package gift.common.config;

import gift.jwt.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public FilterConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistrationResolver() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean
            = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns(
            "/api/members",
            "/api/wishes/*"
        );
        registrationBean.setOrder(1);
        return registrationBean;
    }
}