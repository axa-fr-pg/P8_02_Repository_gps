package tripmaster.gps;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;

/**
 * Bean class to access gpsUtil library
 */
@Configuration
public class GpsUtilBean {

	@Bean
	public GpsUtil gpsUtil() {
    	Locale.setDefault(Locale.ENGLISH);
		return new GpsUtil();
	}
}
