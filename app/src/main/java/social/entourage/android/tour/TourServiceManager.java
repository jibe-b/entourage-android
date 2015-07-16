package social.entourage.android.tour;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.TourResponse;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.common.Constants;

/**
 * Manager is like a presenter but for a service
 * controlling the TourService
 * @see TourService
 */
public class TourServiceManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final TourService tourService;
    private final TourRequest tourRequest;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private CustomLocationListener locationListener;
    private Tour tour;
    private long tourId;

    @Inject
    public TourServiceManager(final TourService tourService, final TourRequest tourRequest) {
        this.tourService = tourService;
        this.tourRequest = tourRequest;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return this.tour;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
    }

    private void sendTour() {
        /*
        tourRequest.tour(tour, new Callback<TourResponse>() {
            @Override
            public void success(TourResponse tourResponse, Response response) {
                tourId = tourResponse.getTour().getId();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
        */
    }

    private void updateTourCoordinates(TourPoint point) {
        /*
        tourRequest.tourPoint(tourId, point, new Callback<TourResponse>() {
            @Override
            public void success(TourResponse tourResponse, Response response) {
                Log.e("test", tourResponse.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
        */
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startTour(String type1, String type2) {
        tour = new Tour();
        tour.setTourType(type2); // social, other, food
        sendTour();
        initializeLocationService();
    }

    public void finishTour() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        tour = null;
    }

    public boolean isRunning() {
        return tour != null;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            System.out.println(tourService.isPaused());
            if (tour != null && !tourService.isPaused()) {
                TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude(), new Date());
                updateTourCoordinates(point);
                tour.updateCoordinates(new LatLng(location.getLatitude(), location.getLongitude()));
                tourService.notifyListeners(tour);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
