package social.entourage.android.map.tour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.tools.BusProvider;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Manager is like a presenter but for a service
 * controlling the TourService
 * @see TourService
 */
public class TourServiceManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int POINT_PER_REQUEST = 10;
    private static final double ALIGNMENT_PRECISION = .000001;
    private static final long VIBRATION_DURATION = 1000;
    private static final double RETRIEVE_TOURS_DISTANCE = 0.04;

    private final TourService tourService;
    private final TourRequest tourRequest;
    private final EncounterRequest encounterRequest;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Tour tour;
    private Location previousLocation;
    private long tourId;
    private int pointsNeededForNextRequest;
    private List<TourPoint> pointsToSend;
    private List<TourPoint> pointsToDraw;
    private Timer timerFinish;
    private ConnectivityManager connectivityManager;
    private LocationManager locationManager;
    private CustomLocationListener locationListener;
    private boolean isBetterLocationUpdated;

    public TourServiceManager(final TourService tourService, final TourRequest tourRequest, final EncounterRequest encounterRequest) {
        Log.i("TourServiceManager", "constructor");
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        this.encounterRequest = encounterRequest;
        this.pointsNeededForNextRequest = 1;
        this.pointsToSend = new ArrayList<>();
        this.pointsToDraw = new ArrayList<>();
        this.connectivityManager = (ConnectivityManager) this.tourService.getSystemService(Context.CONNECTIVITY_SERVICE);
        BusProvider.getInstance().register(this);
        initializeLocationService();
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return this.tour;
    }

    public long getTourId() {
        return tourId;
    }

    public List<TourPoint> getPointsToDraw() {
        return pointsToDraw;
    }

    public void setTourDuration(String duration) {
        tour.setDuration(duration);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void stopLocationService() {
        if (checkPermission()) {
            if (locationListener != null) {
                locationManager.removeUpdates(locationListener);
                locationManager = null;
            }
        }
    }

    public void startTour(String transportMode, String type) {
        tour = new Tour(transportMode, type);
        sendTour();
    }

    public void finishTour() {
        addLastTourPoint();
        closeTour();
    }

    public boolean isRunning() {
        return tour != null;
    }

    public void addEncounter(Encounter encounter) {
        tour.addEncounter(encounter);
    }

    public void unregisterFromBus() {
        BusProvider.getInstance().unregister(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private boolean checkPermission() {
        return (checkSelfPermission(tourService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(tourService, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void initializeLocationService() {
        if (checkPermission()) {
            locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new CustomLocationListener();
            updateLocationServiceFrequency();
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                EntourageLocation.getInstance().setInitialLocation(lastKnownLocation);
            }
        }
    }

    private void updateLocationServiceFrequency() {
        if (checkPermission()) {
            long minTime = Constants.UPDATE_TIMER_MILLIS_OFF_TOUR;
            float minDistance = Constants.DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR;
            if (tour != null) {
                if (tour.getTourVehicleType().equals(TourTransportMode.FEET.getName())) {
                    minTime = Constants.UPDATE_TIMER_MILLIS_ON_TOUR_FEET;
                    minDistance = Constants.DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR_FEET;
                } else if (tour.getTourVehicleType().equals(TourTransportMode.CAR.getName())) {
                    minTime = Constants.UPDATE_TIMER_MILLIS_ON_TOUR_CAR;
                    minDistance = Constants.DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR_CAR;
                }
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        }
    }

    private void initializeTimerFinishTask() {
        long duration = 1000 * 60 * 60 * 5;
        timerFinish = new Timer();
        timerFinish.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOut();
            }
        }, duration, duration);
    }

    private void timeOut() {
        Vibrator vibrator = (Vibrator) tourService.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATION_DURATION);
        tourService.sendBroadcast(new Intent(TourService.KEY_NOTIFICATION_PAUSE_TOUR));
    }

    private void addLastTourPoint() {
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            TourPoint lastPoint = new TourPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            pointsToSend.add(lastPoint);
        }
        updateTourCoordinates();
    }

    private void sendTour() {
        Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.tour(tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    //initializeLocationService();
                    Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                    if (currentLocation != null) {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                    }
                    updateLocationServiceFrequency();
                    initializeTimerFinishTask();
                    tourId = response.body().getTour().getId();
                    tour.setId(tourId);
                    tourService.notifyListenersTourCreated(true, tourId);

                    if (checkPermission()) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (location != null) {
                            TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude());
                            pointsToDraw.add(point);
                            pointsToSend.add(point);
                            previousLocation = location;
                            updateTourCoordinates();
                            tourService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
                        } else {
                            Log.e(this.getClass().getSimpleName(), "no location provided");
                        }
                    }
                } else {
                    tour = null;
                    tourService.notifyListenersTourCreated(false, -1);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tour = null;
                tourService.notifyListenersTourCreated(false, -1);
            }
        });
    }

    private void closeTour() {
        tour.setTourStatus(Tour.TOUR_CLOSED);
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.closeTour(tourId, tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    Log.d("Success", response.body().getTour().toString());
                    tour = null;
                    pointsToSend.clear();
                    pointsToDraw.clear();
                    cancelFinishTimer();
                    updateLocationServiceFrequency();
                    tourService.notifyListenersTourClosed(true);
                } else {
                    tourService.notifyListenersTourClosed(false);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tourService.notifyListenersTourClosed(false);
            }
        });
    }

    private void updateTourCoordinates() {
        final TourPoint.TourPointWrapper tourPointWrapper = new TourPoint.TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourPointWrapper.setDistance(tour.getDistance());
        Call<Tour.TourWrapper> call = tourRequest.tourPoints(tourId, tourPointWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    if (response.body().getTour() != null) {
                        pointsToSend.removeAll(response.body().getTour().getTourPoints());
                        Log.v(this.getClass().getSimpleName(), response.body().getTour().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e(this.getClass().getSimpleName(), t.getLocalizedMessage());
            }
        });
    }

    protected void retrieveToursNearbyLarge() {
        CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            float zoom = currentPosition.zoom;
            float distance = 40000f / (float) Math.pow(2f, zoom) / 2.5f;
            Call<Tour.ToursWrapper> call = tourRequest.retrieveToursNearby(10, null, null, location.latitude, location.longitude, distance);
            call.enqueue(new Callback<Tour.ToursWrapper>() {
                @Override
                public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersToursNearby(response.body().getTours());
                    }
                }

                @Override
                public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                    Log.e("Error", t.getLocalizedMessage());
                }
            });
        }
    }

    protected void retrieveToursNearbySmall(final LatLng point, final boolean isUserHistory, final int userId, final int page, final int per) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ExecutorService executorService = Executors.newFixedThreadPool((isUserHistory ? 2 : 1));
                    final Future<List<Tour>> futureCloseTours;
                    final Future<List<Tour>> futureCloseUserTours;
                    final List<Tour> closeTours;
                    final List<Tour> closeUserTours;

                    futureCloseTours = executorService.submit(
                        new Callable<List<Tour>>() {
                            @Override
                            public List<Tour> call() throws Exception {
                                return tourRequest.retrieveToursNearby(5, null, null, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE)
                                        .execute().body().getTours();
                            }
                        }
                    );
                    closeTours = futureCloseTours.get();
                    //if (isUserHistory) {
                        futureCloseUserTours = executorService.submit(
                                new Callable<List<Tour>>() {
                                    @Override
                                    public List<Tour> call() throws Exception {
                                        return tourRequest.retrieveToursByUserIdAndPoint(userId, page, per, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE)
                                                .execute().body().getTours();
                                    }
                                }
                        );
                        closeUserTours = futureCloseUserTours.get();
                    //}
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Map<Long, Tour> toursMap = new HashMap<>();
                            for (Tour tour : closeTours) {
                                toursMap.put(tour.getId(), tour);
                            }
                            //if (isUserHistory) {
                                for (Tour tour : closeUserTours) {
                                    toursMap.put(tour.getId(), tour);
                                }
                            //}
                            tourService.notifyListenersToursFound(toursMap);
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TourServiceManager.class.getSimpleName(), "", e);
                }
            }
        }).start();
    }

    /*protected void retrieveToursNearbySmall(LatLng point) {
        if (point != null) {
            Call<Tour.ToursWrapper> call = tourRequest.retrieveToursNearby(5, null, null, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE);
            call.enqueue(new Callback<Tour.ToursWrapper>() {
                @Override
                public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                    if (response.isSuccess()) {
                        Map<Long, Tour> toursMap = new HashMap<>();
                        for (Tour tour : response.body().getTours()) {
                            toursMap.put(tour.getId(), tour);
                        }
                        tourService.notifyListenersToursFound(toursMap);
                    }
                }

                @Override
                public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                    Log.e("Error", t.getLocalizedMessage());
                }
            });
        }
    }*/

    protected void retrieveToursByUserId(int userId, int page, int per) {
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(userId, page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                if (response.isSuccess()) {
                    tourService.notifyListenersUserToursFound(response.body().getTours());
                }
            }

            @Override
            public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
            }
        });
    }

    protected void retrieveToursByUserIdAndPoint(int userId, int page, int per, LatLng point) {
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserIdAndPoint(userId, page, per, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                if (response.isSuccess()) {
                    Map<Long, Tour> toursMap = new HashMap<>();
                    for (Tour tour : response.body().getTours()) {
                        toursMap.put(tour.getId(), tour);
                    }
                    tourService.notifyListenersUserToursFoundFromPoint(toursMap);
                }
            }

            @Override
            public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
            }
        });
    }

    protected void sendEncounter(final Encounter encounter) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            Call<EncounterResponse> call = encounterRequest.create(encounter.getTourId(), encounterWrapper);
            call.enqueue(new Callback<EncounterResponse>() {
                @Override
                public void onResponse(Call<EncounterResponse> call, Response<EncounterResponse> response) {
                    if (response.isSuccess()) {
                        Log.d("tape:", "success");
                        BusProvider.getInstance().post(new EncounterTaskResult(true, encounter));
                    }
                 }

                @Override
                public void onFailure(Call<EncounterResponse> call, Throwable t) {
                    Log.d("tape:", "failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null));
                }
            });
        } else {
            Log.d("tape:", "no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null));
            //Toast.makeText(tourService, "pas de réseau", Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void cancelFinishTimer() {
        if (timerFinish != null) {
            timerFinish.cancel();
            timerFinish = null;
        }
    }

    private void onLocationChanged(Location location, TourPoint point) {
        pointsToDraw.add(point);
        pointsToSend.add(point);
        if (pointsToSend.size() >= 3) {
            TourPoint a = pointsToSend.get(pointsToSend.size() - 3);
            TourPoint b = pointsToSend.get(pointsToSend.size() - 2);
            TourPoint c = pointsToSend.get(pointsToSend.size() - 1);
            if (distanceToLine(a, b, c) < ALIGNMENT_PRECISION) {
                pointsToSend.remove(b);
            }
        }
        pointsNeededForNextRequest--;

        tour.addCoordinate(new TourPoint(location.getLatitude(), location.getLongitude()));
        if (previousLocation != null) {
            tour.updateDistance(location.distanceTo(previousLocation));
        }
        previousLocation = location;

        if (isWebServiceUpdateNeeded()) {
            pointsNeededForNextRequest = POINT_PER_REQUEST;
            updateTourCoordinates();
        }
        tourService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private boolean isWebServiceUpdateNeeded() {
        return pointsNeededForNextRequest <= 0;
    }

    private double distanceToLine(TourPoint startPoint, TourPoint middlePoint, TourPoint endPoint) {
        double scalarProduct = (middlePoint.getLatitude() - startPoint.getLatitude()) * (endPoint.getLatitude() - startPoint.getLatitude()) + (middlePoint.getLongitude() - startPoint.getLongitude()) * (endPoint.getLongitude() - startPoint.getLongitude());
        double distanceProjection = scalarProduct / Math.sqrt(Math.pow(endPoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(endPoint.getLongitude() - startPoint.getLongitude(), 2));
        double distanceToMiddle = Math.sqrt(Math.pow(middlePoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(middlePoint.getLongitude() - startPoint.getLongitude(), 2));
        return Math.sqrt(Math.pow(distanceToMiddle, 2) - Math.pow(distanceProjection, 2));
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onLocationPermissionGranted(OnLocationPermissionGranted event) {
        if (locationListener == null && event.isPermissionGranted()) {
            initializeLocationService();
        }
    }

    @Subscribe
    public void encounterToSend(EncounterUploadTask task) {
        sendEncounter(task.getEncounter());
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
            tourService.notifyListenersPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            if (tour != null && !tourService.isPaused()) {
                TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude());
                TourServiceManager.this.onLocationChanged(location, point);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Intent intent = new Intent();
            intent.setAction(TourService.KEY_GPS_ENABLED);
            TourServiceManager.this.tourService.sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent();
            intent.setAction(TourService.KEY_GPS_DISABLED);
            TourServiceManager.this.tourService.sendBroadcast(intent);
        }

        private void updateLocation(Location location) {
            EntourageLocation.getInstance().saveCurrentLocation(location);
            Location bestLocation = EntourageLocation.getInstance().getLocation();
            boolean shouldCenterMap = false;
            if (bestLocation == null || (location.getAccuracy() > 0.0 && bestLocation.getAccuracy() == 0.0)) {
                EntourageLocation.getInstance().saveLocation(location);
                isBetterLocationUpdated = true;
                shouldCenterMap = true;
            }

            if (isBetterLocationUpdated) {
                isBetterLocationUpdated = false;
                LatLng latLng = EntourageLocation.getInstance().getLatLng();
                if (shouldCenterMap) {
                    BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                }
            }
        }
    }
}
