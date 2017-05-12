package sixkiller.sample.configuration;

import com.thetransactioncompany.cors.CORSConfiguration;
import com.thetransactioncompany.cors.CORSConfigurationException;
import com.thetransactioncompany.cors.CORSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.Properties;

@Configuration
public class FilterConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(FilterConfig.class);

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    public OnPopStateRedirectFilter angularRedirectFilter() {
        return new OnPopStateRedirectFilter();
    }

    @Bean
    public CORSFilter corsFilter() {

        Properties properties = new Properties();

        properties.setProperty("cors.allowGenericHttpRequests", "true");
        properties.setProperty("cors.allowOrigin", "*");
        properties.setProperty("cors.supportedMethods", "true");
        properties.setProperty("cors.GET, HEAD, POST, DELETE, OPTIONS", "true");
        properties.setProperty("cors.supportedHeaders", "*");
        properties.setProperty("cors.supportsCredentials", "true");
        properties.setProperty("cors.maxAge", "3600");


        try {
            return new CORSFilter(
                    new CORSConfiguration(properties)
            );
        } catch (CORSConfigurationException e) {
            LOGGER.error("Error initializing CORSFilter", e);

            return null;
        }
    }

}
