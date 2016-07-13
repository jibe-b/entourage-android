package social.entourage.android.map.tour.information;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.invite.contacts.InviteContactsFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.discussion.DiscussionAdapter;
import social.entourage.android.map.tour.information.members.MembersAdapter;
import social.entourage.android.tools.BusProvider;

public class TourInformationFragment extends DialogFragment implements TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "fragment_tour_information";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;
    private static final int SCROLL_DELTA_Y_THRESHOLD = 20;

    private static final int REQUEST_NONE = 0;
    private static final int REQUEST_QUIT_TOUR = 1;
    private static final int REQUEST_JOIN_TOUR = 2;

    private static final int MAP_SNAPSHOT_ZOOM = 15;

    private static final String KEY_FEEDITEM = "social.entourage.android.KEY_FEEDITEM";
    private static final String KEY_FEEDITEM_ID = "social.entourage.android.KEY_FEEDITEM_ID";
    private static final String KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = false;

    @Bind(R.id.tour_info_title)
    TextView fragmentTitle;

    @Bind(R.id.tour_card_title)
    TextView tourOrganization;

    @Bind(R.id.tour_card_photo)
    ImageView tourAuthorPhoto;

    @Bind(R.id.tour_card_type)
    TextView tourType;

    @Bind(R.id.tour_card_author)
    TextView tourAuthorName;

    @Bind(R.id.tour_card_location)
    TextView tourLocation;

    @Bind(R.id.tour_card_people_count)
    TextView tourPeopleCount;

    @Bind(R.id.tour_card_act_layout)
    RelativeLayout headerActLayout;

    @Bind(R.id.tour_info_details_selector)
    RadioGroup detailsSelectorRadioGroup;

    @Bind(R.id.tour_info_chat_rb)
    RadioButton chatRB;

    @Bind(R.id.tour_info_information_rb)
    RadioButton informationRB;

    @Bind(R.id.tour_info_description)
    TextView tourDescription;

    @Bind(R.id.tour_info_discussion_view)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

    @Bind(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.tour_info_comment_layout)
    LinearLayout commentLayout;

    @Bind(R.id.tour_info_comment)
    EditText commentEditText;

    @Bind(R.id.tour_info_comment_send_button)
    Button commentSendButton;

    @Bind(R.id.tour_info_comment_record_button)
    AppCompatImageButton commentRecordButton;

    @Bind(R.id.tour_info_options)
    LinearLayout optionsLayout;

    @Bind(R.id.tour_info_public_section)
    RelativeLayout publicSection;

    @Bind(R.id.tour_info_private_section)
    RelativeLayout privateSection;

    @Bind(R.id.tour_info_share_button)
    AppCompatImageButton shareButton;

    @Bind(R.id.tour_info_more_button)
    AppCompatImageButton moreButton;

    @Bind(R.id.tour_info_act_layout)
    RelativeLayout actLayout;

    @Bind(R.id.tour_info_join_button)
    Button joinButton;

    @Bind(R.id.tour_info_invite_source_layout)
    RelativeLayout inviteSourceLayout;

    @Bind(R.id.tour_info_members_layout)
    LinearLayout membersLayout;

    @Bind(R.id.tour_info_member_count)
    TextView membersCountTextView;

    @Bind(R.id.tour_info_members)
    RecyclerView membersView;

    MembersAdapter membersAdapter;
    List<TimestampedObject> membersList = new ArrayList<>();

    int apiRequestsCount;

    FeedItem feedItem;
    long requestedFeedItemId;
    int requestedFeedItemType;

    Date oldestChatMessageDate = null;
    boolean needsMoreChatMessaged = true;
    boolean scrollToLastCard = true;
    private OnScrollListener discussionScrollListener = new OnScrollListener();
    private int scrollDeltaY = 0;

    private SupportMapFragment mapFragment;

    private SupportMapFragment hiddenMapFragment;
    private GoogleMap hiddenGoogleMap;
    private boolean isTakingSnapshot = false;
    private Bitmap mapSnapshot;
    private boolean takeSnapshotOnCameraMove = false;
    List<TourTimestamp> tourTimestampList = new ArrayList<>();

    OnTourInformationFragmentFinish mListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(FeedItem feedItem) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FEEDITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    public static TourInformationFragment newInstance(long feedId, int feedItemType) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_FEEDITEM_ID, feedId);
        args.putInt(KEY_FEEDITEM_TYPE, feedItemType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.bind(this, toReturn);

        return toReturn;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        feedItem = (FeedItem) getArguments().getSerializable(KEY_FEEDITEM);
        if (feedItem != null) {
            initializeView();
        }
        else {
            requestedFeedItemId = getArguments().getLong(KEY_FEEDITEM_ID);
            requestedFeedItemType = getArguments().getInt(KEY_FEEDITEM_TYPE);
            if (requestedFeedItemType == TimestampedObject.TOUR_CARD || requestedFeedItemType == TimestampedObject.ENTOURAGE_CARD) {
                presenter.getFeedItem(requestedFeedItemId, requestedFeedItemType);
            }
        }
        if (feedItem != null && feedItem.isPrivate()) {
            loadPrivateCards();
        }

        initializeCommentEditText();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .tourInformationModule(new TourInformationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof  OnTourInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString() + " must implement OnTourInformationFragmentFinish");
        }
        mListener = (OnTourInformationFragmentFinish)activity;
        doBindService();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (isBound) {
            tourService.unregister(this);
        }
        doUnbindService();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    if (commentEditText.getText().toString().equals("")) {
                        commentEditText.setText(textMatchList.get(0));
                    } else {
                        commentEditText.setText(commentEditText.getText() + " " + textMatchList.get(0));
                    }
                    commentEditText.setSelection(commentEditText.getText().length());
                    FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));

        //setup scroll listener
        discussionView.addOnScrollListener(discussionScrollListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        discussionView.removeOnScrollListener(discussionScrollListener);
    }

    public long getFeedItemId() {
        if (feedItem != null) {
            return feedItem.getId();
        }
        return requestedFeedItemId;
    }

    public long getFeedItemType() {
        if (feedItem != null) {
            return feedItem.getType();
        }
        return requestedFeedItemType;
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        this.dismiss();
    }

    @OnClick(R.id.tour_info_comment_send_button)
    protected void onAddCommentButton() {
        if (presenter != null) {
            presenter.sendFeedItemMessage(commentEditText.getText().toString());
        }
    }

    @OnClick({R.id.tour_card_author, R.id.tour_card_photo})
    protected void onAuthorClicked() {
        BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(feedItem.getAuthor().getUserID()));
    }

    @OnClick(R.id.tour_info_comment_record_button)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    @OnClick(R.id.tour_info_more_button)
    public void onMoreButton() {
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_up);

        optionsLayout.startAnimation(bottomUp);
        optionsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.tour_info_button_close, R.id.tour_info_options})
    public void onCloseOptionsButton() {
        Animation bottomDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_down);

        optionsLayout.startAnimation(bottomDown);
        optionsLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.tour_info_button_stop_tour)
    public void onStopTourButton() {
        if (feedItem.getStatus().equals(FeedItem.STATUS_ON_GOING) || feedItem.getStatus().equals(FeedItem.STATUS_OPEN)) {
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                Tour tour = (Tour)feedItem;
                //compute distance
                float distance = 0.0f;
                List<TourPoint> tourPointsList = tour.getTourPoints();
                TourPoint startPoint = tourPointsList.get(0);
                for (int i = 1; i < tourPointsList.size(); i++) {
                    TourPoint p = tourPointsList.get(i);
                    distance += p.distanceTo(startPoint);
                    startPoint = p;
                }
                tour.setDistance(distance);

                //duration
                Date now = new Date();
                Date duration = new Date(now.getTime() - tour.getStartTime().getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                tour.setDuration(dateFormat.format(duration));

                //hide the options
                optionsLayout.setVisibility(View.GONE);

                //show stop tour activity
                if (mListener != null) {
                    mListener.showStopTourActivity(tour);
                }
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                tourService.stopFeedItem(feedItem);
            }
        }
        else if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.getStatus().equals(FeedItem.STATUS_CLOSED)) {
            if (tourService != null) {
                tourService.freezeTour((Tour)feedItem);
            }
        }
    }

    @OnClick(R.id.tour_info_button_quit_tour)
    public void onQuitTourButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int titleId = R.string.tour_info_quit_tour_title;
        int messageId = R.string.tour_info_quit_tour_description;
        if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            titleId = R.string.entourage_info_quit_entourage_title;
            messageId = R.string.entourage_info_quit_entourage_description;
        }
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (tourService == null) {
                            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            showProgressBar();
                            User me = EntourageApplication.me(getActivity());
                            if (me == null) {
                                Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                tourService.removeUserFromFeedItem(feedItem, me.getId());
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @OnClick({R.id.tour_info_join_button, R.id.tour_info_share_button})
    public void onJoinTourButton() {
        if (tourService != null) {
            showProgressBar();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                tourService.requestToJoinTour((Tour)feedItem);
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                tourService.requestToJoinEntourage((Entourage) feedItem);
            }
            else {
                hideProgressBar();
            }
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnCheckedChanged({R.id.tour_info_chat_rb, R.id.tour_info_information_rb})
    protected void onChatSelected(CompoundButton button, boolean checked) {
        if (button == chatRB && checked) {
            publicSection.setVisibility(View.GONE);
            privateSection.setVisibility(View.VISIBLE);
            return;
        }
        if (button == informationRB && checked) {
            publicSection.setVisibility(View.VISIBLE);
            privateSection.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.tour_info_user_add_button)
    protected void onUserAddClicked() {
        inviteSourceLayout.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.invite_source_close_button, R.id.invite_source_close_bottom_button})
    protected void onCloseInviteSourceClicked() {
        inviteSourceLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.invite_source_contacts_button)
    protected void onInviteContactsClicked() {
        // close the invite source view
        onCloseInviteSourceClicked();
        // open the contacts fragment
        InviteContactsFragment fragment = InviteContactsFragment.newInstance(feedItem.getId(), feedItem.getType());
        fragment.show(getFragmentManager(), InviteContactsFragment.TAG);
    }

    public boolean onPushNotificationChatMessageReceived(Message message) {
        //we received a chat notification
        //check if it is referring to this tour
        PushNotificationContent content = message.getContent();
        if (!content.isTourRelated() && content.getJoinableId() != feedItem.getId()) {
            return false;
        }
        //retrieve the last messages from server
        if (presenter != null) {
            scrollToLastCard = true;
            presenter.getFeedItemMessages();
        }
        return true;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        apiRequestsCount = 0;

        // Initialize the header
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            fragmentTitle.setText(R.string.tour_info_title);
        }
        else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            if (feedItem.getFeedType().equals(Entourage.TYPE_DEMAND)) {
                fragmentTitle.setText(R.string.entourage_type_demand);
            }
            else {
                fragmentTitle.setText(R.string.entourage_type_contribution);
            }
        }

        // Initialize the header
        tourOrganization.setText(feedItem.getTitle());

        String displayType = feedItem.getFeedTypeLong(this.getActivity());
        if (displayType != null) {
            tourType.setText(displayType);
        } else {
            tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_info_unknown)));
        }

        tourAuthorName.setText(feedItem.getAuthor().getUserName());

        String avatarURLAsString = feedItem.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            Picasso.with(getContext()).load(Uri.parse(avatarURLAsString))
                    .transform(new CropCircleTransformation())
                    .into(tourAuthorPhoto);
        }

        String location = "";
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            Address tourAddress = feedItem.getStartAddress();
            if (tourAddress != null) {
                location = tourAddress.getAddressLine(0);
                if (tourLocation == null) {
                    location = "";
                }
            }
        }
        else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            TourPoint entourageLocation = ((Entourage)feedItem).getLocation();
            if (entourageLocation != null) {
                Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                if (currentLocation != null) {
                    float distance = entourageLocation.distanceTo(new TourPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    location = String.format("%.2f km", distance/1000.0f);
                }
            }
        }

        tourLocation.setText(String.format(getResources().getString(R.string.tour_cell_location), Tour.getHoursDiffToNow(feedItem.getStartTime()), "h", location));

        tourPeopleCount.setText("" + feedItem.getNumberOfPeople());

        headerActLayout.setVisibility(View.GONE);

        // update description
        tourDescription.setText(feedItem.getDescription());

        // switch to appropiate section
        if (feedItem.isPrivate()) {
            switchToPrivateSection();
        }
        else {
            switchToPublicSection();
        }
    }

    private void initializeOptionsView() {
        User me = EntourageApplication.me(getActivity());
        Button stopTourButton = (Button)optionsLayout.findViewById(R.id.tour_info_button_stop_tour);
        Button quitTourButton = (Button)optionsLayout.findViewById(R.id.tour_info_button_quit_tour);
        stopTourButton.setVisibility(View.GONE);
        quitTourButton.setVisibility(View.GONE);
        if (me != null) {
            int myId = me.getId();
            if (feedItem.getAuthor().getUserID() != myId) {
                quitTourButton.setVisibility(View.VISIBLE);
            }
            else {
                stopTourButton.setVisibility(feedItem.isFreezed() ? View.GONE : View.VISIBLE);
                if (feedItem.isClosed()) {
                    stopTourButton.setText(R.string.tour_info_options_freeze_tour);
                } else {
                    stopTourButton.setText(R.string.tour_info_options_stop_tour);
                }
            }
        }
    }

    private void updateHeaderButtons() {
        boolean isTourPrivate = feedItem.isPrivate();
        shareButton.setVisibility(isTourPrivate ? View.GONE : ( (!feedItem.getJoinStatus().equals(Tour.JOIN_STATUS_NOT_REQUESTED) || feedItem.isFreezed()) ? View.GONE : View.VISIBLE ) );
        moreButton.setVisibility(isTourPrivate ? View.VISIBLE : View.GONE);
    }

    private void initializeDiscussionList() {

        Date now = new Date();

        //add the start time
        if (feedItem.getType() != TimestampedObject.ENTOURAGE_CARD || FeedItem.STATUS_OPEN.equals(feedItem.getStatus())) {
            addDiscussionTourStartCard(now);
        }

        //check if we need to add the Tour closed card
        if (feedItem.isClosed()) {
            addDiscussionTourEndCard();
        }

        //init the recycler view
        discussionView.setLayoutManager(new LinearLayoutManager(getContext()));
        discussionAdapter = new DiscussionAdapter();
        discussionView.setAdapter(discussionAdapter);

        //add the cards
        List<TimestampedObject> cachedCardInfoList = feedItem.getCachedCardInfoList();
        if (cachedCardInfoList != null) {
            discussionAdapter.addItems(cachedCardInfoList);
        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList();

        //scroll to last card
        scrollToLastCard();

        //find the oldest chat message received
        initOldestChatMessageDate();
    }

    private void initOldestChatMessageDate() {
        List<TimestampedObject> cachedCardInfoList = feedItem.getCachedCardInfoList();
        Iterator<TimestampedObject> iterator = cachedCardInfoList.iterator();
        while (iterator.hasNext()) {
            TimestampedObject timestampedObject = iterator.next();
            if (timestampedObject.getClass() != ChatMessage.class) continue;
            ChatMessage chatMessage = (ChatMessage) timestampedObject;
            Date chatCreationDate = chatMessage.getCreationDate();
            if (chatCreationDate == null) continue;
            if (oldestChatMessageDate == null) {
                oldestChatMessageDate = chatCreationDate;
            }
            else {
                if (chatCreationDate.before(oldestChatMessageDate)) {
                    oldestChatMessageDate = chatCreationDate;
                }
            }
        }
    }

    private void initializeMap() {
        if (mapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            mapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_map_layout, mapFragment).commit();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                    Tour tour = (Tour)feedItem;
                    List<TourPoint> tourPoints = tour.getTourPoints();
                    if (tourPoints != null && tourPoints.size() > 0) {
                        //setup the camera position to starting point
                        TourPoint startPoint = tourPoints.get(0);
                        CameraPosition cameraPosition = new CameraPosition(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), EntourageLocation.INITIAL_CAMERA_FACTOR, 0, 0);
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()));
                        googleMap.addMarker(markerOptions);

                        //add the tour points
                        PolylineOptions line = new PolylineOptions();
                        int color = getTrackColor(tour.getTourType(), tour.getStartTime());
                        line.zIndex(2f);
                        line.width(15);
                        line.color(color);
                        for (TourPoint tourPoint : tourPoints) {
                            line.add(tourPoint.getLocation());
                        }
                        googleMap.addPolyline(line);
                    }
                }
                else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    TourPoint startPoint = feedItem.getStartPoint();
                    if (startPoint != null) {
                        LatLng position = startPoint.getLocation();

                        // move camera
                        CameraPosition cameraPosition = new CameraPosition(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), EntourageLocation.INITIAL_CAMERA_FACTOR, 0, 0);
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        // add heatmap
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.heat_zone);
                        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                                .image(icon)
                                .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                                .clickable(true)
                                .anchor(0.5f, 0.5f);

                        googleMap.addGroundOverlay(groundOverlayOptions);
                    }
                }
            }
        });
    }

    private void initializeHiddenMap() {
        if (hiddenMapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            hiddenMapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, hiddenMapFragment).commit();
        hiddenMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                if (tourTimestampList.size() > 0) {
                    TourTimestamp tourTimestamp = tourTimestampList.get(0);
                    if (tourTimestamp.getTourPoint() != null) {
                        //put the pin
                        MarkerOptions pin = new MarkerOptions().position(tourTimestamp.getTourPoint().getLocation());
                        googleMap.addMarker(pin);
                        //move the camera
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(tourTimestamp.getTourPoint().getLocation(), MAP_SNAPSHOT_ZOOM);
                        googleMap.moveCamera(camera);
                    }
                }
                else {
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_SNAPSHOT_ZOOM));
                }

                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        getMapSnapshot();
                    }
                });

                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(final CameraPosition cameraPosition) {
                        if (takeSnapshotOnCameraMove) {
                            getMapSnapshot();
                        }
                    }
                });

                hiddenGoogleMap = googleMap;
            }
        });
    }

    private boolean getMapSnapshot() {
        if (hiddenGoogleMap == null) return false;
        if (tourTimestampList.size() == 0) return true;
        final TourTimestamp tourTimestamp = tourTimestampList.get(0);
        isTakingSnapshot = true;
        //take the snapshot
        hiddenGoogleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(final Bitmap bitmap) {
                //save the snapshot
                mapSnapshot = bitmap;
                snapshotTaken(tourTimestamp);
                //signal it has finished taking the snapshot
                isTakingSnapshot = false;
                //check if we need more snapshots
                if (tourTimestampList.size() > 1) {
                    TourTimestamp nextTourTimestamp = tourTimestampList.get(1);
                    if (nextTourTimestamp.getTourPoint() != null) {
                        float distance = nextTourTimestamp.getTourPoint().distanceTo(tourTimestamp.getTourPoint());
                        VisibleRegion visibleRegion = hiddenGoogleMap.getProjection().getVisibleRegion();
                        LatLng nearLeft = visibleRegion.nearLeft;
                        LatLng nearRight = visibleRegion.nearRight;
                        float[] result = {0};
                        Location.distanceBetween(nearLeft.latitude, nearLeft.longitude, nearRight.latitude, nearRight.longitude, result);
                        takeSnapshotOnCameraMove = (distance < result[0]);

                        //put the pin
                        hiddenGoogleMap.clear();
                        MarkerOptions pin = new MarkerOptions().position(nextTourTimestamp.getTourPoint().getLocation());
                        hiddenGoogleMap.addMarker(pin);
                        //move the camera
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(nextTourTimestamp.getTourPoint().getLocation(), MAP_SNAPSHOT_ZOOM);
                        hiddenGoogleMap.moveCamera(camera);
                    }
                }
                tourTimestampList.remove(tourTimestamp);
            }
        });
        return true;
    }

    private void snapshotTaken(TourTimestamp tourTimestamp) {
        if (mapSnapshot == null || tourTimestamp == null) return;
        tourTimestamp.setSnapshot(mapSnapshot);
        discussionAdapter.updateCard(tourTimestamp);
    }

    private int getTrackColor(String type, Date date) {
        int color = Color.GRAY;
        if (TourType.MEDICAL.getName().equals(type)) {
            color = Color.RED;
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            color = Color.BLUE;
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            color = Color.GREEN;
        }
        if (!MapEntourageFragment.isToday(date)) {
            return MapEntourageFragment.getTransparentColor(color);
        }

        //return Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
        return color;
    }

    private void initializeCommentEditText() {
        commentEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    commentRecordButton.setVisibility(View.GONE);
                    commentSendButton.setVisibility(View.VISIBLE);
                } else {
                    commentRecordButton.setVisibility(View.VISIBLE);
                    commentSendButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initializeMembersView() {

        // Show the members count
        membersCountTextView.setText(getString(R.string.tour_info_members_count, membersList.size()));

        if (membersAdapter == null) {
            // Initialize the recycler view
            membersView.setLayoutManager(new LinearLayoutManager(getContext()));
            membersAdapter = new MembersAdapter();
            membersView.setAdapter(membersAdapter);
        }

        // add the members
        membersAdapter.addItems(membersList);
    }

    private void switchToPublicSection() {
        detailsSelectorRadioGroup.setVisibility(View.GONE);
        actLayout.setVisibility(View.VISIBLE);
        publicSection.setVisibility(View.VISIBLE);
        privateSection.setVisibility(View.GONE);
        membersLayout.setVisibility(View.GONE);

        updateHeaderButtons();
        updateJoinStatus();

        initializeMap();
    }

    private void switchToPrivateSection() {
        detailsSelectorRadioGroup.setVisibility(View.VISIBLE);
        actLayout.setVisibility(View.GONE);
        membersLayout.setVisibility(View.VISIBLE);
//        publicSection.setVisibility(View.GONE);
//        privateSection.setVisibility(View.VISIBLE);
        if (mapFragment == null) {
            initializeMap();
        }

        if (hiddenMapFragment == null) {
            initializeHiddenMap();
        }

        updateHeaderButtons();
        initializeOptionsView();

        //hide the comment section if the user is not accepted or tour is freezed
        if (!feedItem.getJoinStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED) || feedItem.isFreezed()) {
            commentLayout.setVisibility(View.GONE);
        }

        initializeDiscussionList();
        initializeMembersView();
    }

    private void loadPrivateCards() {
        if (presenter != null) {
            presenter.getFeedItemUsers();
            presenter.getFeedItemMessages();
            presenter.getFeedItemEncounters();
        }
    }

    private void updateJoinStatus() {
        if (feedItem == null) return;
        if (feedItem.isFreezed()) {
            actLayout.setVisibility(View.GONE);
        }
        else {
            actLayout.setVisibility(View.VISIBLE);
            String joinStatus = feedItem.getJoinStatus();
            if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
                joinButton.setEnabled(false);
                joinButton.setText(R.string.tour_cell_button_pending);
                joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_pending), null, null);
            } else if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
                joinButton.setEnabled(false);
                joinButton.setText(R.string.tour_cell_button_accepted);
                joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_accepted), null, null);
            } else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
                joinButton.setEnabled(false);
                joinButton.setText(R.string.tour_cell_button_rejected);
                joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_rejected), null, null);
            } else {
                joinButton.setEnabled(true);
                joinButton.setText(R.string.tour_cell_button_join);
                joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_join), null, null);
            }
        }
    }

    private void updateDiscussionList() {
        updateDiscussionList(true);
    }

    private void updateDiscussionList(boolean scrollToLastCard) {

        List<TimestampedObject> addedCardInfoList = feedItem.getAddedCardInfoList();
        if (addedCardInfoList == null || addedCardInfoList.size() == 0) {
            return;
        }
        for (int i = 0; i < addedCardInfoList.size(); i++) {

            TimestampedObject cardInfo = addedCardInfoList.get(i);
            discussionAdapter.addCardInfoAfterTimestamp(cardInfo);

        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList();

        if (scrollToLastCard) {
            //scroll to last card
            scrollToLastCard();
        }
    }

    private void addDiscussionTourStartCard(Date now) {
        long duration = 0;
        float distance = 0;
        if (feedItem.getStartTime() != null && !feedItem.isClosed()) {
            duration = now.getTime() - feedItem.getStartTime().getTime();
        }
        Date timestamp;
        if (feedItem.getCachedCardInfoList().size() == 0) {
            timestamp = feedItem.getStartTime();
        }
        else {
            timestamp = duration == 0 ? feedItem.getStartTime() : now;
        }
        TourPoint startPoint = feedItem.getStartPoint();
        TourTimestamp tourTimestamp = new TourTimestamp(
                feedItem.getStartTime(),
                timestamp,
                feedItem.getType(),
                FeedItem.STATUS_ON_GOING,
                startPoint,
                duration,
                distance
        );
        feedItem.addCardInfo(tourTimestamp);

        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            tourTimestampList.add(tourTimestamp);
        }
    }

    private void addDiscussionTourEndCard() {
        long duration = 0;
        float distance = 0;
        if (feedItem.getStartTime() != null && feedItem.getEndTime() != null) {
            duration = feedItem.getEndTime().getTime() - feedItem.getStartTime().getTime();
        }
        TourPoint endPoint = feedItem.getEndPoint();
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            Tour tour = (Tour)feedItem;
            List<TourPoint> tourPointsList = tour.getTourPoints();
            if (tourPointsList.size() > 1) {
                TourPoint startPoint = tourPointsList.get(0);
                endPoint = tourPointsList.get(tourPointsList.size() - 1);
                for (int i = 1; i < tourPointsList.size(); i++) {
                    TourPoint p = tourPointsList.get(i);
                    distance += p.distanceTo(startPoint);
                    startPoint = p;
                }
            }
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                feedItem.getEndTime(),
                feedItem.getEndTime(),
                feedItem.getType(),
                FeedItem.STATUS_CLOSED,
                endPoint,
                duration,
                distance
        );
        feedItem.addCardInfo(tourTimestamp);

        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            tourTimestampList.add(tourTimestamp);
        }
    }

    private void scrollToLastCard() {
        discussionView.scrollToPosition(discussionAdapter.getItemCount()-1);
    }

    protected void showProgressBar() {
        apiRequestsCount++;
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        apiRequestsCount--;
        if (apiRequestsCount <= 0) {
            progressBar.setVisibility(View.GONE);
            apiRequestsCount = 0;
        }
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------

    protected void onFeedItemReceived(FeedItem feedItem) {
        hideProgressBar();
        if (feedItem != null) {
            this.feedItem = feedItem;
            initializeView();
            if (feedItem.isPrivate()) {
                loadPrivateCards();
            }
        }
    }

    protected void onFeedItemUsersReceived(List<TourUser> tourUsers) {
        if (tourUsers != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            Iterator<TourUser> iterator = tourUsers.iterator();
            while (iterator.hasNext()) {
                TourUser tourUser =  iterator.next();
                //skip the author
                if (tourUser.getUserId() == feedItem.getAuthor().getUserID()) {
                    TourUser clone = tourUser.clone();
                    clone.setDisplayedAsMember(true);
                    membersList.add(clone);
                    continue;
                }
                //show only the accepted users
                if (!tourUser.getStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED)) {
                    continue;
                }
                timestampedObjectList.add(tourUser);

                TourUser clone = tourUser.clone();
                clone.setDisplayedAsMember(true);
                membersList.add(clone);
            }
            feedItem.addCardInfoList(timestampedObjectList);

            initializeMembersView();
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onFeedItemMessagesReceived(List<ChatMessage> chatMessageList) {
        if (chatMessageList != null) {
            if (chatMessageList.size() > 0) {
                //check who sent the message
                AuthenticationController authenticationController = EntourageApplication.get(getActivity()).getEntourageComponent().getAuthenticationController();
                if (authenticationController.isAuthenticated()) {
                    int me = authenticationController.getUser().getId();
                    Iterator chatMessageIterator = chatMessageList.iterator();
                    while (chatMessageIterator.hasNext()) {
                        ChatMessage chatMessage = (ChatMessage) chatMessageIterator.next();
                        chatMessage.setIsMe(chatMessage.getUserId() == me);
                    }
                }

                List<TimestampedObject> timestampedObjectList = new ArrayList<>();
                timestampedObjectList.addAll(chatMessageList);
                if (feedItem.addCardInfoList(timestampedObjectList) > 0) {
                    //remember the last chat message
                    ChatMessage chatMessage = (ChatMessage)feedItem.getAddedCardInfoList().get(0);
                    oldestChatMessageDate = chatMessage.getCreationDate();
                }
            }
            else {
                //no need to ask for more messages
                needsMoreChatMessaged = false;
            }
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList(scrollToLastCard);
        scrollToLastCard = false;
    }

    protected void onFeedItemMessageSent(ChatMessage chatMessage) {
        hideProgressBar();

        if (chatMessage == null) {
            Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            return;
        }
        commentEditText.setText("");

        //hide the keyboard
        if (commentEditText.hasFocus()) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }

        //add the message to the list
        chatMessage.setIsMe(true);
        feedItem.addCardInfo(chatMessage);

        updateDiscussionList();
    }

    protected void onFeedItemEncountersReceived(List<Encounter> encounterList) {
        if (encounterList != null) {
            User me = EntourageApplication.me(getContext());
            if (me != null) {
                for (int i = 0; i < encounterList.size(); i++) {
                    Encounter encounter = encounterList.get(i);
                    encounter.setIsMyEncounter(encounter.getUserId() == me.getId());
                }
            }
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            timestampedObjectList.addAll(encounterList);
            feedItem.addCardInfoList(timestampedObjectList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourQuited(String status) {
        hideProgressBar();
        if (status == null) {
            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
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
        }
    }

    void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    // ----------------------------------
    // Tour Service listener implementation
    // ----------------------------------

    @Override
    public void onTourCreated(final boolean created, final long tourId) {

    }

    @Override
    public void onTourUpdated(final LatLng newPoint) {

    }

    @Override
    public void onTourResumed(final List<TourPoint> pointsToDraw, final String tourType, final Date startDate) {

    }

    @Override
    public void onLocationUpdated(final LatLng location) {

    }

    @Override
    public void onRetrieveToursNearby(final List<Tour> tours) {
        if (feedItem.getType() != TimestampedObject.TOUR_CARD) return;
        for (Tour receivedTour:tours) {
            if (receivedTour.getId() == this.feedItem.getId()) {
                if(!receivedTour.isSame((Tour)this.feedItem)) {
                    onFeedItemClosed(true, receivedTour);
                }
            }
        }
    }

    @Override
    public void onRetrieveToursByUserId(final List<Tour> tours) {

    }

    @Override
    public void onUserToursFound(final Map<Long, Tour> tours) {

    }

    @Override
    public void onToursFound(final Map<Long, Tour> tours) {

    }

    @Override
    public void onFeedItemClosed(final boolean closed, final FeedItem feedItem) {
        //ignore requests that are not related to our feed item
        if (this.feedItem.getType() != feedItem.getType()) return;
        if (feedItem.getId() != this.feedItem.getId()) return;

        if (closed) {
            this.feedItem.setStatus(feedItem.getStatus());
            this.feedItem.setEndTime(feedItem.getEndTime());
            if (feedItem.getStatus().equals(FeedItem.STATUS_CLOSED) && feedItem.isPrivate()) {
                addDiscussionTourEndCard();
                updateDiscussionList();
            }
            if (feedItem.isFreezed()){
                commentLayout.setVisibility(View.GONE);
            }
            optionsLayout.setVisibility(View.GONE);
            initializeOptionsView();
            updateHeaderButtons();
            updateJoinStatus();
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_close_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGpsStatusChanged(final boolean active) {

    }

    @Override
    public void onUserStatusChanged(final TourUser user, final FeedItem feedItem) {
        //ignore requests that are not related to our feed item
        if (feedItem.getType() != this.feedItem.getType()) return;
        if (feedItem.getId() != this.feedItem.getId()) return;

        hideProgressBar();

        //check for errors
        if (user == null) {
            Toast.makeText(getActivity(), R.string.tour_info_request_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //close the overlay
        onCloseOptionsButton();

        //update the local tour info
        boolean oldPrivateStatus = (privateSection.getVisibility() == View.VISIBLE);
        feedItem.setJoinStatus(user.getStatus());
        boolean currentPrivateStatus = feedItem.isPrivate();
        //update UI
        if (oldPrivateStatus != currentPrivateStatus) {
            if (feedItem.isPrivate()) {
                switchToPrivateSection();
                loadPrivateCards();
            }
            else {
                switchToPublicSection();
            }
        }
        else {
            updateHeaderButtons();
            updateJoinStatus();
        }
    }

    @Override
    public void onRetrieveNewsfeed(final List<Newsfeed> newsfeedList) {}

    // ----------------------------------
    // RecyclerView.OnScrollListener
    // ----------------------------------

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (!needsMoreChatMessaged) return;
            scrollDeltaY += dy;
            //check if user is scrolling up and pass the threshold
            if (dy < 0 && Math.abs(scrollDeltaY) >= SCROLL_DELTA_Y_THRESHOLD) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int adapterPosition = recyclerView.findViewHolderForLayoutPosition(firstVisibleItemPosition).getAdapterPosition();
                TimestampedObject timestampedObject = discussionAdapter.getCardAt(adapterPosition);
                Date timestamp = timestampedObject.getTimestamp();
                if (timestamp != null && oldestChatMessageDate != null && timestamp.before(oldestChatMessageDate)) {
                    presenter.getFeedItemMessages(oldestChatMessageDate);
                }
                scrollDeltaY = 0;
            }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }


    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void showStopTourActivity(Tour tour);
        void closeTourInformationFragment(TourInformationFragment fragment);
    }

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.register(TourInformationFragment.this);
                isBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(TourInformationFragment.this);
            tourService = null;
            isBound = false;
        }
    }
}
