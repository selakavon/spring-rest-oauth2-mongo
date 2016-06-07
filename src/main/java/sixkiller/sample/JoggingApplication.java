package sixkiller.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Created by ala on 9.5.16.
 */

@SpringBootApplication
public class JoggingApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(JoggingApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    private static Class<JoggingApplication> applicationClass = JoggingApplication.class;

}
