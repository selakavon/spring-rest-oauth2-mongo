package sixkiller.sample.configuration;

import org.springframework.context.annotation.Bean;

import java.time.temporal.WeekFields;

/**
 * Created by ala on 12.5.16.
 */
public class WeekFieldsFactory {

    public static WeekFields weekFields() {
        return WeekFields.ISO;
    }

}