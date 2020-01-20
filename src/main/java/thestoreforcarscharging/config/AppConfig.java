package thestoreforcarscharging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class AppConfig {

    @Bean
    public ResourceBundleMessageSource messageSource() {

        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("message/messages");
        source.setDefaultEncoding(UTF_8.name());
        source.setCacheSeconds(10); //reload messages every 10 seconds

        return source;
    }
}