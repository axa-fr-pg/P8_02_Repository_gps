package tripmaster.gps;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tripmaster.common.attraction.AttractionData;
import tripmaster.common.location.VisitedLocationData;
import tripmaster.common.user.User;

/**
 * API class for gps methods
 */
@RestController
public class GpsController {

	private Logger logger = LoggerFactory.getLogger(GpsController.class);
	@Autowired private GpsService gpsService;

	/**
	 * Registers the current location for all users of the given list.
	 * @param userList of all users for whom the current location shall be registered.
	 * @return List of users with updated visited locations history.
	 */
	@PatchMapping("/trackAllUserLocations")
	public List<User> trackAllUserLocations(@RequestBody List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		return gpsService.trackAllUserLocations(userList);
	}
	
	/**
	 * Gets the list of all known attractions in the ecosystem.
	 * @return List of AttractionData containing one entry for each existing attraction.
	 */
	@GetMapping("/getAllAttractions")
	public List<AttractionData> getAllAttractions() {
		logger.debug("getAllAttractions");
		return gpsService.getAllAttractions();
	}

	/**
	 * Gets current user location based on gpsUtil.
	 * @param userId of the user in the String format for whom the location shall be determined.
	 * @return VisitedLocationData containing the current location of the user.
	 */
	@GetMapping("/getCurrentUserLocation")
	public VisitedLocationData getCurrentUserLocation(@RequestParam String userId) {
		logger.debug("getCurrentUserLocation for User " + userId);
		return gpsService.getCurrentUserLocation(userId);
	}

}
