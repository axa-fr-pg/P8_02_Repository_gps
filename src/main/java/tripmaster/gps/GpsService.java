package tripmaster.gps;

import java.util.List;

import gpsUtil.location.VisitedLocation;
import tripmaster.common.attraction.AttractionData;
import tripmaster.common.location.VisitedLocationData;
import tripmaster.common.user.User;

/**
 * Interface for gps services
 * @see tripmaster.gps.GpsServiceImpl
 */
public interface GpsService {

	List<User> trackAllUserLocations(List<User> userList);

	VisitedLocationData getCurrentUserLocation(String userIdString);

	List<AttractionData> getAllAttractions();

	VisitedLocationData newVisitedLocationData(VisitedLocation visitedLocation);

}