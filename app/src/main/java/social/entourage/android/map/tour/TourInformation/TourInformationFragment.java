package social.entourage.android.map.tour.TourInformation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.tour.TourInformation.discussion.DiscussionAdapter;

public class TourInformationFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    @Bind(R.id.tour_info_organization)
    TextView tourOrganization;

    @Bind(R.id.tour_info_author_photo)
    ImageView tourAuthorPhoto;

    @Bind(R.id.tour_info_type)
    TextView tourType;

    @Bind(R.id.tour_info_author_name)
    TextView tourAuthorName;

    @Bind(R.id.tour_info_discussion_view)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

    @Bind(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.tour_info_comment_layout)
    LinearLayout commentLayout;

    @Bind(R.id.tour_info_comment)
    EditText commentEditText;

    int apiRequestsCount;

    Tour tour;

    Date lastChatMessageDate = null;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(Tour tour) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
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
        initializeView();
        return toReturn;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        presenter.getTourUsers();
        presenter.getTourMessages();
        presenter.getTourEncounters();
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
                    if (commentEditText.getText().equals("")) {
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        this.dismiss();
    }

    @OnClick(R.id.tour_info_comment_add_button)
    protected void onAddCommentButton() {
        presenter.sendTourMessage(commentEditText.getText().toString());
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

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);

        apiRequestsCount = 0;

        tourOrganization.setText(tour.getOrganizationName());

        String type = tour.getTourType();
        if (type != null) {
            if (type.equals(TourType.MEDICAL.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_medical)));
            } else if (type.equals(TourType.ALIMENTARY.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_alimentary)));
            } else if (type.equals(TourType.BARE_HANDS.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_bare_hands)));
            }
        } else {
            tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_info_unknown)));
        }

        tourAuthorName.setText(tour.getAuthor().getUserName());

        String avatarURLAsString = tour.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            Picasso.with(getContext()).load(Uri.parse(avatarURLAsString))
                    .transform(new CropCircleTransformation())
                    .into(tourAuthorPhoto);
        }

        //hide the comment section if the user is not accepted
        if (!tour.getJoinStatus().equals(Tour.JOIN_STATUS_ACCEPTED)) {
            commentLayout.setVisibility(View.GONE);
        }

        initialiseDiscussionList();
    }

    private void initialiseDiscussionList() {

        Date now = new Date();

        //add the start time
        addDiscussionTourStartCard(now);

        //check if we need to add the Tour closed card
        if (tour.isClosed()) {
            addDiscussionTourEndCard();
        }

        //init the recycler view
        discussionView.setLayoutManager(new LinearLayoutManager(getContext()));
        discussionAdapter = new DiscussionAdapter();
        discussionView.setAdapter(discussionAdapter);

        //add the cards
        List<TimestampedObject> cachedCardInfoList = tour.getCachedCardInfoList();
        if (cachedCardInfoList != null) {
            discussionAdapter.addItems(cachedCardInfoList);
        }

        //clear the added cards info
        tour.clearAddedCardInfoList();
    }

    private void updateDiscussionList() {

        List<TimestampedObject> addedCardInfoList = tour.getAddedCardInfoList();
        if (addedCardInfoList == null || addedCardInfoList.size() == 0) {
            return;
        }
        for (int i = 0; i < addedCardInfoList.size(); i++) {

            TimestampedObject cardInfo = addedCardInfoList.get(i);
            discussionAdapter.addCardInfoBeforeTimestamp(cardInfo);

        }

        //clear the added cards info
        tour.clearAddedCardInfoList();
    }

    private void addDiscussionTourStartCard(Date now) {
        long duration = 0;
        float distance = 0;
        if (tour.getStartTime() != null && !tour.isClosed()) {
            duration = now.getTime() - tour.getStartTime().getTime();
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                tour.getStartTime(),
                Tour.TOUR_ON_GOING,
                duration,
                distance
        );
        tour.addCardInfo(tourTimestamp);
    }

    private void addDiscussionTourEndCard() {
        long duration = 0;
        float distance = 0;
        if (tour.getStartTime() != null && tour.getEndTime() != null) {
            duration = tour.getEndTime().getTime() - tour.getStartTime().getTime();
        }
        List<TourPoint> tourPointsList = tour.getTourPoints();
        TourPoint startPoint = tourPointsList.get(0);
        for (int i=1; i < tourPointsList.size(); i++) {
            TourPoint p = tourPointsList.get(i);
            distance += p.distanceTo(startPoint);
            startPoint = p;
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                tour.getEndTime(),
                Tour.TOUR_CLOSED,
                duration,
                distance
        );
        tour.addCardInfo(tourTimestamp);
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
        }
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // Server callbacks
    // ----------------------------------

    protected void onTourUsersReceived(List<TourUser> tourUsers) {
        if (tourUsers != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            Iterator<TourUser> iterator = tourUsers.iterator();
            while (iterator.hasNext()) {
                TourUser tourUser =  iterator.next();
                //skip the author
                if (tourUser.getUserId() == tour.getAuthor().getUserID()) {
                    continue;
                }
                //skip the rejected user
                if (tourUser.getStatus().equals(Tour.JOIN_STATUS_REJECTED)) {
                    continue;
                }
                timestampedObjectList.add(tourUser);
            }
            tour.addCardInfoList(timestampedObjectList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessagesReceived(List<ChatMessage> chatMessageList) {
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
                //remember the last chat message
                ChatMessage chatMessage = chatMessageList.get(chatMessageList.size() - 1);
                lastChatMessageDate = chatMessage.getCreationDate();

                List<TimestampedObject> timestampedObjectList = new ArrayList<>();
                timestampedObjectList.addAll(chatMessageList);
                tour.addCardInfoList(timestampedObjectList);
            }
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessageSent(ChatMessage chatMessage) {
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
        tour.addCardInfo(chatMessage);

        updateDiscussionList();

        //make the chat message visible
        scrollToLastCard();
    }

    protected void onTourEncountersReceived(List<Encounter> encounterList) {
        if (encounterList != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            timestampedObjectList.addAll(encounterList);
            tour.addCardInfoList(timestampedObjectList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void closeTourInformationFragment(TourInformationFragment fragment);
    }
}
