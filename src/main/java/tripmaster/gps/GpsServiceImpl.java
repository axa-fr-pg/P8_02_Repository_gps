package tripmaster.gps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

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
    private static final int NUMBER_OF_EXPECTED_USER_PARTITIONS = 10;
    private static final int THREAD_POOL_SIZE = NUMBER_OF_EXPECTED_USER_PARTITIONS * 2;

	private Logger logger = LoggerFactory.getLogger(GpsServiceImpl.class);
	@Autowired private GpsUtil gpsUtil;
	
	// For performance reasons it is required to split users for submission on several threads
	private List<List<User>> divideUserList(List<User> userList) {
		List<List<User>> partitionList = new LinkedList<List<User>>();
		int expectedSize = userList.size() / NUMBER_OF_EXPECTED_USER_PARTITIONS;
		if (expectedSize == 0) {
			partitionList.add(userList);
			return partitionList;
		}
		for (int i = 0; i < userList.size(); i += expectedSize) {
			partitionList.add(userList.subList(i, Math.min(i + expectedSize, userList.size())));
		}
		return partitionList;
	}
	
	/**
	 * Registers the current location for all users of the given list.
	 * @param userList of all users for whom the current location shall be registered.
	 * @return List of users with updated visited locations history.
	 * @see gpsUtil.getUserLocation
	 */
	@Override
	public List<User> trackAllUserLocations(List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		// The number of threads has been defined after several tests to match the performance target
		ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_POOL_SIZE);
		// Divide user list into several parts and submit work separately for these parts
		divideUserList(userList).stream().parallel().forEach( partition -> {
			try {
				logger.debug("trackAllUserLocations submits calculation for user partition of size" +  partition.size());
				forkJoinPool.submit( () -> partition.stream().parallel().forEach(user -> {
					VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.userId);
					user.addToVisitedLocations(newVisitedLocationData(visitedLocation));
				})).get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("trackAllUserLocations got an exception");
				e.printStackTrace();
				throw new RuntimeException("trackAllUserLocations got an exception");
			}
		});
		forkJoinPool.shutdown();
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
