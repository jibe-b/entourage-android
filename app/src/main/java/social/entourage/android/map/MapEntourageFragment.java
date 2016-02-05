package social.entourage.android.map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.event.CheckIntentActionEvent;
import social.entourage.android.api.tape.event.UserChoiceEvent;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.encounter.CreateEncounterActivity;
import social.entourage.android.map.tour.TourListItemView;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.tools.BusProvider;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    private static final int REDRAW_LIMIT = 300;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private int userId;
    private boolean userHistory;

    private View toReturn;

    private SupportMapFragment mapFragment;

    private boolean isBetterLocationUpdated;

    private LatLng previousCoordinates;
    private Location previousCameraLocation;
    private float previousCameraZoom = 1.0f;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private ProgressDialog loaderStop;
    private ProgressDialog loaderSearchTours;
    private boolean isBound;
    private boolean isMapLoaded;
    private boolean isFollowing = true;

    private long currentTourId;
    private int color;

    private List<Polyline> currentTourLines;
    private Map<Long, Polyline> drawnToursMap;
    private Map<Long, Polyline> drawnUserHistory;
    private Map<Long, Marker> markersMap;
    private Map<Long, Tour> retrievedTours;
    private Map<Long, Tour> retrievedHistory;

    private LayoutInflater inflater;

    private float originalMapLayoutWeight;

    @Bind(R.id.fragment_map_pin)
    View mapPin;

    @Bind(R.id.fragment_map_gps_text)
    TextView gpsText;

    @Bind(R.id.fragment_map_follow_button)
    View centerButton;

    @Bind(R.id.button_start_tour_launcher)
    Button buttonStartLauncher;

    @Bind(R.id.layout_map_launcher)
    View mapLauncherLayout;

    @Bind(R.id.launcher_tour_go)
    Button buttonLaunchTour;

    @Bind(R.id.launcher_tour_transport_mode)
    RadioGroup radioGroupTransportMode;

    @Bind(R.id.launcher_tour_type)
    RadioGroup radioGroupType;

    @Bind(R.id.layout_map_tour)
    View layoutMapTour;

    @Bind(R.id.scrollview_tours)
    ScrollView scrollviewTours;

    @Bind(R.id.layout_tours)
    LinearLayout layoutTours;

    @Bind(R.id.layout_map)
    FrameLayout layoutMapMain;

    @Bind(R.id.fragment_map_main_layout)
    LinearLayout layoutMain;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLocationService();
        if (!isBound) {
            doBindService();
        }

        currentTourLines = new ArrayList<>();
        drawnToursMap = new TreeMap<>();
        drawnUserHistory = new TreeMap<>();
        markersMap = new TreeMap<>();
        retrievedTours = new TreeMap<>();
        retrievedHistory = new TreeMap<>();

        FlurryAgent.logEvent(Constants.EVENT_OPEN_TOURS_FROM_MENU);
        BusProvider.getInstance().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        this.inflater = inflater;
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        initializeMap();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMapComponent.builder()
                .entourageComponent(entourageComponent)
                .mapModule(new MapModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CREATE_ENCOUNTER) {
            if (resultCode == Constants.RESULT_CREATE_ENCOUNTER_OK) {
                Encounter encounter = (Encounter) data.getExtras().getSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER);
                addEncounter(encounter);
                presenter.loadEncounterOnMap(encounter);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (int index = 0; index < permissions.length; index++) {
                if ((permissions[index].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[index] != PackageManager.PERMISSION_GRANTED) ||
                    (permissions[index].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[index] != PackageManager.PERMISSION_GRANTED)) {
                    initializeLocationService();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_tours_title);
            if (isMapLoaded) {
                BusProvider.getInstance().post(new CheckIntentActionEvent());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound && tourService != null) {
            tourService.unregister(MapEntourageFragment.this);
            doUnbindService();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mapLauncherLayout.getVisibility() == View.VISIBLE) {
            mapLauncherLayout.setVisibility(View.GONE);
            buttonStartLauncher.setVisibility(View.VISIBLE);
            return true;
        }
        if (scrollviewTours.getVisibility() == View.GONE) {
            showToursList();
            return true;
        }
        return false;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void onNotificationExtras(int id, boolean choice) {
        userId = id;
        userHistory = choice;
    }

    public void setOnMarkerClickListener(MapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    public void putEncounterOnMap(Encounter encounter,
                                  MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        double encounterLatitude = encounter.getLatitude();
        double encounterLongitude = encounter.getLongitude();
        LatLng encounterPosition = new LatLng(encounterLatitude, encounterLongitude);
        BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.encounter);

        MarkerOptions markerOptions = new MarkerOptions().position(encounterPosition)
                .icon(encounterIcon);

        if (mapFragment.getMap() != null) {
            mapFragment.getMap().addMarker(markerOptions);
            onClickListener.addEncounterMarker(encounterPosition, encounter);
        }
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    public void displayChosenTour(Tour tour) {
        presenter.openTour(tour);
    }

    public void displayChosenUser(int userID) {
        Toast.makeText(getContext(), "Show user profile for id="+userID, Toast.LENGTH_SHORT).show();
    }

    public void act(Tour tour) {
        Toast.makeText(getContext(), R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }

    public void checkAction(String action) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (action != null && ConfirmationActivity.KEY_RESUME_TOUR.equals(action)) {
                resumeTour();
            }
            // 2 : Check if should End tour
            else if (action != null && ConfirmationActivity.KEY_END_TOUR.equals(action)) {
                stopTour();
            }
            // 3 : Check if tour is already paused
            else if (tourService.isPaused()) {
                //TODO: check if confirmation screen is already launched
                launchConfirmationActivity();
            }
            // 4 : Check if should pause tour
            else if (action != null && TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                //TODO: check if confirmation screen is already launched
                launchConfirmationActivity();
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onUserChoiceChanged(UserChoiceEvent event) {
        userHistory = event.isUserHistory();
        if (userHistory) {
            tourService.updateUserHistory(userId, 1, 500);
        }
        if (userHistory) {
            showUserHistory();
        } else {
            hideUserHistory();
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TourService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            //isBound = true;
        }
    }

    void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------

    @Override
    public void onTourCreated(boolean created, long tourId) {
        buttonLaunchTour.setText(R.string.tour_go);
        buttonLaunchTour.setEnabled(true);
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourId = tourId;
                presenter.incrementUserToursCount();
                mapLauncherLayout.setVisibility(View.GONE);
                layoutMapTour.setVisibility(View.VISIBLE);
                mapPin.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), R.string.tour_creation_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTourUpdated(LatLng newPoint) {
        drawCurrentLocation(newPoint);
    }

    @Override
    public void onTourResumed(List<TourPoint> pointsToDraw, String tourType, Date startDate) {
        if (!pointsToDraw.isEmpty()) {
            drawCurrentTour(pointsToDraw, tourType, startDate);
            previousCoordinates = pointsToDraw.get(pointsToDraw.size() - 1).getLocation();

            Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            isFollowing = true;
        }
    }

    @Override
    public void onLocationUpdated(LatLng location) {
        centerMap(location);
    }

    @Override
    public void onRetrieveToursNearby(List<Tour> tours) {
        tours = removeRedundantTours(tours, false);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (currentTourId != tour.getId()) {
                drawNearbyTour(tour, false);
            }
        }
    }

    @Override
    public void onRetrieveToursByUserId(List<Tour> tours) {
        tours = removeRedundantTours(tours, true);
        tours = removeRecentTours(tours);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (currentTourId != tour.getId()) {
                drawNearbyTour(tour, true);
            }
        }
    }

    @Override
    public void onToursFound(Map<Long, Tour> tours) {
        if (loaderSearchTours != null) {
            loaderSearchTours.dismiss();
            loaderSearchTours = null;
        }
        if (getActivity() != null) {
            if (tours.isEmpty()) {
                Toast.makeText(getActivity(), tourService.getString(R.string.tour_info_text_nothing_found), Toast.LENGTH_SHORT).show();
            } else {
                if (tours.size() > 1) {
                    List<Tour> tempList = new ArrayList<>();
                    for (Map.Entry<Long, Tour> entry : tours.entrySet()) {
                        tempList.add(entry.getValue());
                    }
                    Tour.Tours toursList = new Tour.Tours(tempList);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    ChoiceFragment choiceFragment = ChoiceFragment.newInstance(toursList);
                    choiceFragment.show(fragmentManager, "fragment_choice");
                } else {
                    TreeMap<Long, Tour> toursTree = new TreeMap<>(tours);
                    presenter.openTour(toursTree.firstEntry().getValue());
                }
            }
        }
    }

    @Override
    public void onTourClosed(boolean closed) {
        if (getActivity() != null) {
            if (closed) {
                mapFragment.getMap().clear();

                currentTourLines.clear();
                drawnToursMap.clear();
                drawnUserHistory.clear();
                retrievedTours.clear();

                layoutTours.removeAllViews();

                previousCoordinates = null;

                mapPin.setVisibility(View.GONE);
                buttonStartLauncher.setVisibility(View.VISIBLE);

                currentTourId = -1;
                tourService.updateNearbyTours();
                if (userHistory) {
                    tourService.updateUserHistory(userId, 1, 1);
                }

                Toast.makeText(getActivity(), R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
            } else {
                layoutMapTour.setVisibility((View.VISIBLE));
                Toast.makeText(getActivity(), R.string.tour_close_fail, Toast.LENGTH_SHORT).show();
            }
            if (loaderStop != null) {
                loaderStop.dismiss();
                loaderStop = null;
            }
        }
    }

    @Override
    public void onGpsStatusChanged(boolean active) {
        if (active) {
             gpsText.setVisibility(View.GONE);
        } else {
            gpsText.setVisibility(View.VISIBLE);
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.fragment_map_follow_button)
    void onFollowGeolocation() {
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
        isFollowing = true;
    }

    @OnClick(R.id.button_start_tour_launcher)
    void onStartTourLauncher() {
        if (!tourService.isRunning()) {
            FlurryAgent.logEvent(Constants.EVENT_OPEN_TOUR_LAUNCHER_FROM_MAP);
            buttonStartLauncher.setVisibility(View.GONE);
            mapLauncherLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.launcher_tour_go)
    void onStartNewTour() {
        buttonLaunchTour.setText(R.string.button_loading);
        buttonLaunchTour.setEnabled(false);
        TourTransportMode tourTransportMode = TourTransportMode.findByRessourceId(radioGroupTransportMode.getCheckedRadioButtonId());
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        startTour(tourTransportMode.getName(), tourType.getName());
        FlurryAgent.logEvent(Constants.EVENT_START_TOUR);
    }

    @OnClick(R.id.tour_stop_button)
    public void onStartStopConfirmation() {
        pauseTour();
        layoutMapTour.setVisibility(View.GONE);
        if (getActivity() != null) {
            launchConfirmationActivity();
        }
    }

    @OnClick(R.id.tour_add_encounter_button)
    public void onAddEncounter() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putLong(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourId);
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
            intent.putExtras(args);
            startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------

    private void initializeLocationService() {
        if (getActivity() != null) {
            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.map_permission_title)
                            .setMessage(R.string.map_permission_description)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_LOCATION);
                                }
                            }).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.map_permission_title)
                            .setMessage(R.string.map_permission_description)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSIONS_REQUEST_LOCATION);
                                }
                            }).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS, Constants.DISTANCE_BETWEEN_UPDATES_METERS, new CustomLocationListener());
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                EntourageLocation.getInstance().setInitialLocation(lastKnownLocation);
            }
        }
    }

    private void initializeMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
            mapFragment.getMap().getUiSettings().setMapToolbarEnabled(false);
            mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                    Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                    Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
                    float newZoom = cameraPosition.zoom;

                    if (tourService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                        previousCameraZoom = newZoom;
                        previousCameraLocation = newLocation;
                        tourService.updateNearbyTours();
                        if (userHistory) {
                            tourService.updateUserHistory(userId, 1, 500);
                        }
                    }

                    if (isFollowing && currentLocation != null) {
                        if (currentLocation.distanceTo(newLocation) > 1) {
                            isFollowing = false;
                        }
                    }
                }
            });
            mapFragment.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (getActivity() != null) {
                        if (scrollviewTours.getVisibility() == View.VISIBLE) {
                            hideToursList();
                        } else {
                            loaderSearchTours = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_search), getActivity().getString(R.string.button_loading), true);
                            loaderSearchTours.setCancelable(true);
                            tourService.searchToursFromPoint(latLng);
                        }
                    }
                }
            });
            mapFragment.getMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    isMapLoaded = true;
                    BusProvider.getInstance().post(new CheckIntentActionEvent());
                }
            });
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    private Tour getCurrentTour() {
        return tourService != null ? tourService.getCurrentTour() : null;
    }

    private void startTour(String transportMode, String type) {
        if (tourService != null && !tourService.isRunning()) {
            color = getTrackColor(false, type, new Date());
            tourService.beginTreatment(transportMode, type);
        }
    }

    private void pauseTour() {
        if (tourService != null && tourService.isRunning()) {
            tourService.pauseTreatment();
        }
    }

    private void resumeTour() {
        if (tourService.isRunning()) {
            tourService.resumeTreatment();
            buttonStartLauncher.setVisibility(View.GONE);
            mapPin.setVisibility(View.VISIBLE);
            layoutMapTour.setVisibility(View.VISIBLE);
        }
    }

    private void stopTour() {
        if (getActivity() != null) {
            if (tourService != null && tourService.isRunning()) {
                loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                loaderStop.setCancelable(true);
                tourService.endTreatment();
                FlurryAgent.logEvent(Constants.EVENT_STOP_TOUR);
            }
        }
    }

    private void launchConfirmationActivity() {
        pauseTour();
        buttonStartLauncher.setVisibility(View.GONE);
        layoutMapTour.setVisibility(View.GONE);
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, getCurrentTour());
        Intent confirmationIntent = new Intent(getActivity(), ConfirmationActivity.class);
        confirmationIntent.putExtras(args);
        getActivity().startActivity(confirmationIntent);
    }

    private void addEncounter(Encounter encounter) {
        tourService.addEncounter(encounter);
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------

    private List<Tour> removeRedundantTours(List<Tour> tours, boolean isHistory) {
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (!isHistory) {
                if (drawnToursMap.containsKey(tour.getId())) {
                    iteratorTours.remove();
                }
            } else {
                if (drawnUserHistory.containsKey(tour.getId())) {
                    iteratorTours.remove();
                }
            }
        }
        return tours;
    }

    private List<Tour> removeRecentTours(List<Tour> tours) {
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (retrievedTours.containsValue(tour)) {
                iteratorTours.remove();
            }
        }
        return tours;
    }

    private int getTrackColor(boolean isHistory, String type, Date date) {
        int color = Color.GRAY;
        if (TourType.MEDICAL.getName().equals(type)) {
            color = Color.RED;
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            color = Color.GREEN;
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            color = Color.BLUE;
        }
        if (!isToday(date)) {
            color = getTransparentColor(color);
        }
        if (isHistory) {
            if (!userHistory) {
                return Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
            } else {
                return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            }
        }
        return color;
    }

    private int getTransparentColor(int color) {
        return Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void hideUserHistory() {
        Iterator iteratorTours = retrievedHistory.entrySet().iterator();
        while (iteratorTours.hasNext()) {
            Map.Entry pair = (Map.Entry) iteratorTours.next();
            Tour tour = (Tour) pair.getValue();
            Polyline line = drawnUserHistory.get(tour.getId());
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    private void showUserHistory() {
        Iterator iteratorLines = drawnUserHistory.entrySet().iterator();
        while (iteratorLines.hasNext()) {
            Map.Entry pair = (Map.Entry) iteratorLines.next();
            Tour tour = retrievedHistory.get(pair.getKey());
            Polyline line = (Polyline) pair.getValue();
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    private boolean isToday(Date date) {
        if (date == null) return false;
        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(date);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    private void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
        centerMap(cameraPosition);
    }

    private void centerMap(CameraPosition cameraPosition) {
        if(mapFragment!= null && mapFragment.getMap() != null && isFollowing) {
            mapFragment.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(mapFragment.getMap().getCameraPosition());
        }
    }

    private void addCurrentTourEncounters() {
        List<Encounter> encounters = tourService.getCurrentTour().getEncounters();
        if (!encounters.isEmpty()) {
            for (Encounter encounter : encounters) {
                presenter.loadEncounterOnMap(encounter);
            }
        }
    }

    private void drawCurrentLocation(LatLng location) {
        if (previousCoordinates != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
        previousCoordinates = location;
    }

    private void drawCurrentTour(List<TourPoint> pointsToDraw, String tourType, Date startDate) {
        if (!pointsToDraw.isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            color = getTrackColor(true, tourType, startDate);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            for (TourPoint tourPoint : pointsToDraw) {
                line.add(tourPoint.getLocation());
            }
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
    }

    private void drawNearbyTour(Tour tour, boolean isHistory) {
        if (mapFragment != null && mapFragment.getMap() != null && drawnToursMap != null && drawnUserHistory != null && tour != null && !tour.getTourPoints().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            if (isToday(tour.getStartTime())) {
                line.zIndex(1f);
            } else {
                line.zIndex(0f);
            }
            line.width(15);
            line.color(getTrackColor(isHistory, tour.getTourType(), tour.getStartTime()));
            for (TourPoint tourPoint : tour.getTourPoints()) {
                line.add(tourPoint.getLocation());
            }
            if (isHistory) {
                retrievedHistory.put(tour.getId(), tour);
                drawnUserHistory.put(tour.getId(), mapFragment.getMap().addPolyline(line));
            } else {
                retrievedTours.put(tour.getId(), tour);
                drawnToursMap.put(tour.getId(), mapFragment.getMap().addPolyline(line));
                addTourCell(tour);
                new GeocoderTask().execute(tour);
            }
            if (tour.getTourStatus() == null) {
                tour.setTourStatus(Tour.TOUR_CLOSED);
            }
            if (Tour.TOUR_ON_GOING.equalsIgnoreCase(tour.getTourStatus())) {
                addTourHead(tour);
            }
        }
    }

    private void addTourCell(Tour tour) {
        //create the cell
        TourListItemView tourCell = (TourListItemView) inflater.inflate(R.layout.layout_tour_cell, null, false);
        tourCell.populate(tour, this);

        //add the cell to the layout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0 ,10);
        layoutTours.addView(tourCell, lp);
    }

    private void updateTourCellStartingPoint(Tour tour) {
        if (tour == null || tour.getStartAddress() == null) return;
        TourListItemView tourCell = (TourListItemView) layoutTours.findViewWithTag(new Long(tour.getId()));
        if (tourCell == null) return;

        tourCell.updateStartLocation(tour);
    }

    private void addTourHead(Tour tour) {
        TourPoint lastPoint = tour.getTourPoints().get(tour.getTourPoints().size() - 1);
        double latitude = lastPoint.getLatitude();
        double longitude = lastPoint.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        BitmapDescriptor icon = null;
        if (tour.getTourVehicleType().equals(TourTransportMode.FEET.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_feet);
        }
        else if (tour.getTourVehicleType().equals(TourTransportMode.CAR.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_car);
        }

        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(icon);

        if (mapFragment.getMap() != null) {
            markersMap.put(tour.getId(), mapFragment.getMap().addMarker(markerOptions));
            presenter.getOnClickListener().addTourMarker(position, tour);
        }
    }

    private void hideToursList() {
        scrollviewTours.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layoutMapMain.getLayoutParams();
        originalMapLayoutWeight = lp.weight;
        lp.weight = layoutMain.getWeightSum();
        layoutMapMain.setLayoutParams(lp);
        layoutMain.forceLayout();
    }

    private void showToursList() {
        scrollviewTours.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layoutMapMain.getLayoutParams();
        lp.weight = originalMapLayoutWeight;
        layoutMapMain.setLayoutParams(lp);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

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
                    centerMap(latLng);
                }
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

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.register(MapEntourageFragment.this);

                boolean isRunning = tourService != null && tourService.isRunning();
                if (isRunning) {
                    buttonStartLauncher.setVisibility(View.GONE);

                    currentTourId = tourService.getCurrentTourId();
                    mapPin.setVisibility(View.VISIBLE);
                    layoutMapTour.setVisibility(View.VISIBLE);

                    addCurrentTourEncounters();
                }

                tourService.updateNearbyTours();
                if (userHistory) {
                    tourService.updateUserHistory(userId, 1, 500);
                }
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapEntourageFragment.this);
            tourService = null;
        }
    }

    private class GeocoderTask extends AsyncTask<Tour, Void, Tour> {

        @Override
        protected Tour doInBackground(final Tour... params) {
            try {
                Geocoder geoCoder = new Geocoder(getContext(), Locale.getDefault());
                Tour tour = params[0];
                if (tour.getTourPoints().isEmpty()) return null;
                TourPoint tourPoint = tour.getTourPoints().get(0);
                List<Address> addresses = geoCoder.getFromLocation(tourPoint.getLatitude(), tourPoint.getLongitude(), 1);
                if (addresses.size() > 0) {
                    tour.setStartAddress(addresses.get(0));
                }
                return tour;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Tour tour) {
            updateTourCellStartingPoint(tour);
        }
    }
}
