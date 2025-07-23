package gift.common.config;

import gift.common.annotation.resolver.LogInMemberArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LogInMemberArgumentResolver logInMemberArgumentResolver;

    public WebConfig(LogInMemberArgumentResolver logInMemberArgumentResolver) {
        this.logInMemberArgumentResolver = logInMemberArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(logInMemberArgumentResolver);
    }

}
