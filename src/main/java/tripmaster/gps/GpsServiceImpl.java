package tripmaster.gps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import tripmaster.common.attraction.AttractionData;
import tripmaster.common.location.LocationData;
import tripmaster.common.location.VisitedLocationData;
import tripmaster.common.user.User;

/**
 * Class for gps services. Implements GpsService interface.
 * @see tripmaster.gps.GpsService
 */
@Service
public class GpsServiceImpl implements GpsService {

	private Logger logger = LoggerFactory.getLogger(GpsServiceImpl.class);
	@Autowired private GpsUtil gpsUtil;

	/**
	 * Registers the current location for all users of the given list.
	 * @param userList of all users for whom the current location shall be registered.
	 * @return List of users with updated visited locations history.
	 * @see gpsUtil.getUserLocation
	 */
	@Override
	public List<User> trackAllUserLocations(List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		userList.stream().parallel().forEach(user -> {
			VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.userId);
			user.addToVisitedLocations(newVisitedLocationData(visitedLocation));
		});
		return userList;
	}

	/**
	 * Gets current user location based on gpsUtil.
	 * @param userIdString of the user for whom the location shall be determined.
	 * @return VisitedLocationData containing the current location of the user.
	 * @see gpsUtil.getUserLocation
	 */
	@Override
	public VisitedLocationData getCurrentUserLocation(String userIdString) {
		logger.debug("getUserLocation with userId = " + userIdString);
		UUID userId = UUID.fromString(userIdString);
		return newVisitedLocationData(gpsUtil.getUserLocation(userId));
	}
	
	/**
	 * Gets the list of all known attractions in the ecosystem.
	 * @return List of AttractionData containing one entry for each existing attraction.
	 * @see gpsUtil.getAttractions
	 */
	@Override
	public List<AttractionData> getAllAttractions() {
		logger.debug("getAllAttractions");
		List<AttractionData> dataList = new ArrayList<AttractionData>();
		gpsUtil.getAttractions().stream().forEach(attraction -> {
			AttractionData data = new AttractionData();
			data.id = attraction.attractionId;
			data.name = attraction.attractionName;
			data.city = attraction.city;
			data.state = attraction.state;
			data.latitude = attraction.latitude;
			data.longitude = attraction.longitude;
			dataList.add(data);
		});
		return dataList;
	}
	
	/**
	 * Converts a visited location to the VisitedLocationData structure.
	 * @param visitedLocation of type VisitedLocation.
	 * @return VisitedLocationData containing the current location of the user.
	 */
	@Override
	public VisitedLocationData newVisitedLocationData(VisitedLocation visitedLocation) {
		return new VisitedLocationData(visitedLocation.userId,
				new LocationData(visitedLocation.location.latitude, visitedLocation.location.longitude),
				visitedLocation.timeVisited);
	}
}
