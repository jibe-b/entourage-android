package social.entourage.android;

public class Constants {

    //TODO: should be changed after each release
    // v4.4 API Key
    public static final String API_KEY = "1307d4a101dc2e01855960b1";

    // Announcements version
    public static final String ANNOUNCEMENTS_VERSION = "v1";

    // Filenames
    public static final String SHARED_PREFERENCES_FILE = "entourage_shared_preferences";
    public static final String FILENAME_TAPE_QUEUE = "encounters_queue";

    // Update Dialog
    public static final String UPDATE_DIALOG_DISPLAYED = "update_dialog_displayed";

    // Request and result codes
    public static final int REQUEST_CREATE_ENCOUNTER = 1;
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;

    // Link IDs
    public static final String SCB_LINK_ID = "pedagogic-content";
    public static final String GOAL_LINK_ID= "action-examples";
    public static final String DONATE_LINK_ID = "donation";
    public static final String ATD_LINK_ID = "atd-partnership";
    public static final String CHARTE_LINK_ID = "ethics-charter";
    public static final String FAQ_LINK_ID = "faq";
    public static final String FEEDBACK_ID = "feedback";
    public static final String AMBASSADOR_ID = "volunteering";
    // Links
    public static final String POI_PROPOSE_URL = "https://goo.gl/jD5uIQ";
    public static final String HELP_URL = "https://blog.entourage.social/franchir-le-pas/";


    // Email Addresses
    public static final String EMAIL_CONTACT = "contact@entourage.social";

    // Analytics events
    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";
    public static final String EVENT_OPEN_POI_FROM_MAP = "Open_POI_From_Map";

    //LOG IN Events
    public static final String EVENT_LOGOUT = "Logout";
    public static final String EVENT_LOGIN_START = "Login_Start";
    public static final String EVENT_LOGIN_OK = "Login_Success";
    public static final String EVENT_LOGIN_FAILED = "Login_Failed";
    public static final String EVENT_LOGIN_ERROR = "Login_Error";
    public static final String EVENT_TUTORIAL_START = "Log_Tutorial_Start";
    public static final String EVENT_TUTORIAL_END = "Log_Tutorial_End";
    public static final String EVENT_LOGIN_SEND_NEW_CODE = "Login_Send_New_Code";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_OK = "Newsletter_Inscription_OK";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_FAILED = "Newsletter_Inscription_Failed";
    public static final String EVENT_SPLASH_SIGNUP = "SplashSignUp";
    public static final String EVENT_SPLASH_LOGIN = "SplashLogIn";
    public static final String EVENT_PHONE_SUBMIT = "TelephoneSubmit";
    public static final String EVENT_PHONE_SUBMIT_FAIL = "TelephoneSubmitFail";
    public static final String EVENT_PHONE_SUBMIT_ERROR = "TelephoneSubmitError";
    public static final String EVENT_SMS_CODE_REQUEST = "SMSCodeRequest";
    public static final String EVENT_EMAIL_SUBMIT = "EmailSubmit";
    public static final String EVENT_EMAIL_SUBMIT_ERROR = "EmailSubmitError";
    public static final String EVENT_EMAIL_IGNORE = "IgnoreEmail";
    public static final String EVENT_NAME_SUBMIT = "NameSubmit";
    public static final String EVENT_NAME_SUBMIT_ERROR = "NameSubmitError";
    public static final String EVENT_NAME_TYPE = "NameType";
    public static final String EVENT_PHOTO_UPLOAD_SUBMIT = "PhotoUploadSubmit";
    public static final String EVENT_PHOTO_TAKE_SUBMIT = "PhotoTakeSubmit";
    public static final String EVENT_PHOTO_IGNORE = "IgnorePhoto";
    public static final String EVENT_PHOTO_BACK = "BackFromPhoto1";
    public static final String EVENT_PHOTO_SUBMIT = "SubmitInstantPhoto";
    public static final String EVENT_NOTIFICATIONS_ACCEPT = "AcceptNotifications";
    public static final String EVENT_NOTIFICATIONS_POPUP_ACCEPT = "AcceptNotificationsFromPopup";
    public static final String EVENT_NOTIFICATIONS_POPUP_REFUSE = "RefuseNotificationsFromPopup";
    public static final String EVENT_GEOLOCATION_ACCEPT = "AcceptGeoloc";
    public static final String EVENT_GEOLOCATION_POPUP_ACCEPT = "AcceptGeolocFromPopup";
    public static final String EVENT_GEOLOCATION_POPUP_REFUSE = "RefuseGeolocFromPopup";
    public static final String EVENT_GEOLOCATION_ACTIVATE_04_4A = "ActivateGeolocFromScreen04_4UserBlocked";
    public static final String EVENT_WELCOME_CONTINUE = "WelcomeScreenContinue";

    // SCREEN Events
    public static final String EVENT_SCREEN_01 = "Screen01SplashView";
    public static final String EVENT_SCREEN_02_1 = "Screen02OnboardingLoginView";
    public static final String EVENT_SCREEN_03_1 = "Screen03_1OnboardingCodeResendView";
    public static final String EVENT_SCREEN_03_2 = "Screen03_2OnboardingPhoneNotFoundView";
    public static final String EVENT_SCREEN_04 = "Screen04_GoEnableGeolocView"; //Not implemented on Android
    public static final String EVENT_SCREEN_04_2 = "Screen04_2OnboardingGeolocView";
    public static final String EVENT_SCREEN_04_3 = "Screen04_3OnboardingNotificationsView";
    public static final String EVENT_SCREEN_30_1 = "Screen30_1WelcomeView";
    public static final String EVENT_SCREEN_30_2 = "Screen30_2InputPhoneView";
    public static final String EVENT_SCREEN_30_2_E = "Screen30_2PhoneAlreadyExistsError";
    public static final String EVENT_SCREEN_30_3 = "Screen30_3InputPasscodeView";
    public static final String EVENT_SCREEN_30_4 = "Screen30_4InputEmailView";
    public static final String EVENT_SCREEN_30_5 = "Screen30_5InputNameView";
    public static final String EVENT_SCREEN_09_1_ME = "Screen09_1MyProfileViewAsPublicView";
    public static final String EVENT_SCREEN_09_1_OTHER = "Screen09_1OtherUserProfileView";
    public static final String EVENT_SCREEN_09_2 = "Screen09_2EditMyProfileView";
    public static final String EVENT_SCREEN_09_4 = "Screen09_4EditPasswordView";
    public static final String EVENT_SCREEN_09_4_SUBMIT = "Screen09_4ChangePasswordSubmit";
    public static final String EVENT_SCREEN_09_5 = "Screen09_5EditNameView";
    public static final String EVENT_SCREEN_09_6 = "Screen09_6ChoosePhotoView";
    public static final String EVENT_SCREEN_09_7 = "Screen09_7TakePhotoView";
    public static final String EVENT_SCREEN_09_8 = "Screen09_8ConfirmPhotoView";
    public static final String EVENT_SCREEN_09_9 = "Screen09_9MovePhotoView";
    public static final String EVENT_SCREEN_06_1 = "Screen06_1FeedView";
    public static final String EVENT_SCREEN_06_2 = "Screen06_2MapView";
    public static final String EVENT_SCREEN_17_2 = "Screen17_2MyMessagesView";

    //MENU Events
    public static final String EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_PLUS = "Open_Guide_From_Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_SIDEMENU = "SolidarityGuideFrom07Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_MAP = "SolidarityGuideFrom06Map";
    public static final String EVENT_OPEN_TOURS_FROM_MENU = "Open_Tours_From_Menu";
    public static final String EVENT_MENU_TAP_MY_PROFILE = "TapMyProfilePhoto";
    public static final String EVENT_MENU_LOGOUT = "LogOut";
    public static final String EVENT_MENU_ABOUT = "AboutClick";
    public static final String EVENT_MENU_GOAL = "WhatActionsClick";
    public static final String EVENT_MENU_FAQ = "AppFAQClick";
    public static final String EVENT_MENU_BLOG = "SimpleCommeBonjourClick";
    public static final String EVENT_MENU_CHART = "ViewEthicsChartClick";
    public static final String EVENT_MENU_ATD = "ATDPartnershipView";
    public static final String EVENT_MENU_DONATION = "DonationView";

    //FEED Events
    public static final String EVENT_FEED_MESSAGES = "GoToMessages"; // No longer used
    public static final String EVENT_FEED_MENU = "OpenMenu";
    public static final String EVENT_FEED_USERPROFILE = "UserProfileClick";
    public static final String EVENT_FEED_ACTIVE_CLOSE_OVERLAY = "OpenActiveCloseOverlay";
    public static final String EVENT_FEED_MAPCLICK = "MapClick";
    public static final String EVENT_FEED_HEATZONECLICK = "HeatzoneMapClick";
    public static final String EVENT_FEED_TOURLINECLICK = "TourMapClick"; //Not possible
    public static final String EVENT_FEED_OPEN_ENTOURAGE = "OpenEntouragePublicPage";
    public static final String EVENT_FEED_OPEN_CONTACT = "OpenEnterInContact";
    public static final String EVENT_FEED_RECENTERCLICK = "RecenterMapClick";
    public static final String EVENT_FEED_FILTERSCLICK = "FeedFiltersPress";
    public static final String EVENT_FEED_REFRESH_LIST = "RefreshListPage";
    public static final String EVENT_FEED_SCROLL_LIST = "ScrollListPage";
    public static final String EVENT_FEED_PLUS_CLICK = "PlusFromFeedClick";
    public static final String EVENT_FEED_TOUR_CREATE_CLICK = "TourCreateClick";
    public static final String EVENT_FEED_ACTION_CREATE_CLICK = "CreateActionClick";
    public static final String EVENT_FEED_GUIDE_SHOW_CLICK = "GDSViewClick";
    public static final String EVENT_FEED_PENDING_OVERLAY = "PendingRequestOverlay";
    public static final String EVENT_FEED_CANCEL_JOIN_REQUEST = "CancelJoinRequest";
    public static final String EVENT_FEED_OPEN_ACTIVE_OVERLAY = "OpenActiveOverlay";
    public static final String EVENT_FEED_QUIT_ENTOURAGE = "QuitFromFeed";
    public static final String EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR = "ActivateGeolocFromCreateTourPopup";
    public static final String EVENT_FEED_ACTIVATE_GEOLOC_RECENTER = "ActivateGeolocFromRecenterPopup";

    //MAP Events
    public static final String EVENT_MAP_MAPVIEW_CLICK = "MapViewClick";
    public static final String EVENT_MAP_LISTVIEW_CLICK = "ListViewClick";
    public static final String EVENT_MAP_PLUS_CLICK = "PlusFromMapClick";
    public static final String EVENT_MAP_LONGPRESS = "HiddenButtonsOverlayPress";
    public static final String EVENT_MAP_ZOOM_IN = "ZoomIn";
    public static final String EVENT_MAP_ZOOM_OUT = "ZoomOut";
    public static final String EVENT_MAP_SHIFT_CENTER = "MapShiftCenter"; //Not able to detect if it's an automatic or manual shift

    //GUIDE Events
    public static final String EVENT_GUIDE_POI_VIEW = "POIView";
    public static final String EVENT_GUIDE_PLUS_CLICK = "PlusFromGDSClick";
    public static final String EVENT_GUIDE_MASK_CLICK = "MaskGDSClick";
    public static final String EVENT_GUIDE_X_CLICK = "MaskGDSXClick";
    public static final String EVENT_GUIDE_LIST_VIEW = "GDSListViewClick";
    public static final String EVENT_GUIDE_MAP_VIEW = "GDSMapViewClick";

    //SEND JOIN REQUEST Events
    public static final String EVENT_JOIN_REQUEST_START = "StartJoinMessage";
    public static final String EVENT_JOIN_REQUEST_SUBMIT = "SubmitJoinMessage";
    public static final String EVENT_JOIN_REQUEST_ACCEPT = "AcceptJoinRequest";
    public static final String EVENT_JOIN_REQUEST_REJECT = "RejectJoinRequest";

    //ENTOURAGE VIEW Events
    public static final String EVENT_ENTOURAGE_DISCUSSION_VIEW = "Screen14_1DiscussionView";
    public static final String EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER = "Screen14_2PublicPageViewAsMemberOrCreator";
    public static final String EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER = "Screen14_2PublicPageViewAsNonMember";
    public static final String EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE = "WriteMessage";
    public static final String EVENT_ENTOURAGE_VIEW_SPEECH = "SpeechRecognitionMessage";
    public static final String EVENT_ENTOURAGE_VIEW_ADD_MESSAGE = "AddContentToMessage";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY = "OpenEntourageOptionsOverlay";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE = "CloseEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT = "ExitEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT = "EditEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS = "InviteFriendsClick";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS = "InviteContacts";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_PHONE = "InviteByPhoneNumber";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_CLOSE = "InviteFriendsClose";
    public static final String EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC = "EntouragePublicPageFromMessages";
    public static final String EVENT_ENTOURAGE_VIEW_ASK_JOIN = "AskJoinFromPublicPage";
    public static final String EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION = "ChangeLocationClick";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_SUCCESS = "SuccessfulClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_FAILURE = "BlockedClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_HELP = "HelpRequestOnClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL = "CancelClosePopup";
    public static final String EVENT_ENTOURAGE_SHARE_MEMBER = "ShareLinkAsMemberOrCreator";
    public static final String EVENT_ENTOURAGE_SHARE_NONMEMBER = "ShareLinkAsNonMember";

    //MY ENTOURAGES Events
    public static final String EVENT_MYENTOURAGES_BANNER_CLICK = "BannerMessageClick";
    public static final String EVENT_MYENTOURAGES_MESSAGE_OPEN = "MessageOpen";
    public static final String EVENT_MYENTOURAGES_PLUS_CLICK = "PlusOnMessagesPageClick";
    public static final String EVENT_MYENTOURAGES_FILTER_CLICK = "MessagesFilterClick";
    public static final String EVENT_MYENTOURAGES_BANNER_MOVE = "MoveBannerClick"; //A lot of code needs to be written to detect this
    public static final String EVENT_MYENTOURAGES_BACK_CLICK = "BackToFeedClick";

    //MY ENTOURAGES FILTER Events
    public static final String EVENT_MYENTOURAGES_FILTER_EXIT = "ExitMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_SAVE = "SaveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ACTIVE = "ActiveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_INVITATIONS = "InvitationsFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ORGANIZER = "OrganizerFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_UNREAD = "UnreadMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_PAST = "ExcludeClosedEntouragesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ASK = "AskMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_OFFER = "OfferMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_TOUR = "TourMessagesFilter";

    //ENTOURAGE DISCLAIMER Events
    public static final String EVENT_ENTOURAGE_DISCLAIMER_CLOSE = "CloseEthicsPopupClick";
    public static final String EVENT_ENTOURAGE_DISCLAIMER_ACCEPT = "AcceptEthicsChartClick";
    public static final String EVENT_ENTOURAGE_DISCLAIMER_LINK = "LinkToEthicsChartClick";

    //TOUR Events
    public static final String EVENT_START_TOUR = "StartTourClick";
    public static final String EVENT_STOP_TOUR = "TourStop";
    public static final String EVENT_RESTART_TOUR = "TourRestart";
    public static final String EVENT_OPEN_TOUR_LAUNCHER_FROM_MAP = "Open_Tour_Launcher_From_Map";
    public static final String EVENT_TOUR_MEDICAL = "MedicalTourChoose";
    public static final String EVENT_TOUR_SOCIAL = "SocialTourChoose";
    public static final String EVENT_TOUR_DISTRIBUTION = "DistributionTourChoose";
    public static final String EVENT_TOUR_SUSPEND = "SuspendTourClick";
    public static final String EVENT_TOUR_PLUS_CLICK = "PlusOnTourClick";

    //TOUR ENCOUNTER Events
    public static final String EVENT_CREATE_ENCOUNTER_CLICK = "CreateEncounterClick";
    public static final String EVENT_CREATE_ENCOUNTER_START = "Open_Create_Encounter_From_Tour";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK = "Encounter_Voice_Message_Recorded_OK";
    public static final String EVENT_CREATE_ENCOUNTER_OK = "Encounter_Created";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED = "Encounter_Voice_Message_Recording_Started";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED = "Encounter_Voice_Message_Recording_Not_Supported";
    public static final String EVENT_CREATE_ENCOUNTER_FAILED = "Encounter_Create_Failed";

    //MAP Filter Events
    public static final String EVENT_MAP_FILTER_FILTER1 = "ClickFilter1Value";
    public static final String EVENT_MAP_FILTER_FILTER2 = "ClickFilter2Value";
    public static final String EVENT_MAP_FILTER_FILTER3 = "ClickFilter3Value";
    public static final String EVENT_MAP_FILTER_ONLY_MINE = "ShowOnlyMineFilter";
    public static final String EVENT_MAP_FILTER_ONLY_TOURS = "ShowToursOnlyFilterClick";
    public static final String EVENT_MAP_FILTER_ONLY_OFFERS = "ShowOnlyOffersClick";
    public static final String EVENT_MAP_FILTER_ONLY_ASK = "ShowOnlyAsksClick";
    public static final String EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS = "ShowOnlyMedicalToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS = "ShowOnlySocialToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS = "ShowOnlyDistributionToursClick";
    public static final String EVENT_MAP_FILTER_ACTION_CATEGORY = "FilterActionSubtypeClick";
    public static final String EVENT_MAP_FILTER_SUBMIT = "SubmitFilterPreferences";
    public static final String EVENT_MAP_FILTER_CLOSE = "CloseFilter";

    // USER Events
    public static final String EVENT_USER_EDIT_PROFILE = "EditMyProfile";
    public static final String EVENT_USER_EDIT_PHOTO = "EditPhoto";
    public static final String EVENT_USER_SAVE = "SaveProfileEdits";
    public static final String EVENT_USER_TOBADGE = "ToBadgePageFromProfile";
    public static final String EVENT_USER_TONOTIFICATIONS = "ToNotifications";

    // ABOUT Events
    public static final String EVENT_ABOUT_RATING = "RatingClick";
    public static final String EVENT_ABOUT_FACEBOOK = "FacebookPageClick";
    public static final String EVENT_ABOUT_WEBSITE = "WebsiteVisitClick";
    public static final String EVENT_ABOUT_CGU = "CGUClick";
    public static final String EVENT_ABOUT_TUTORIAL = "OpenTutorialFromMenu";

    // Encounter Popup While Tour Events
    public static final String EVENT_ENCOUNTER_POPUP_SHOW = "SwitchToEncounterPopupView";
    public static final String EVENT_ENCOUNTER_POPUP_ENCOUNTER = "SwitchToCreateEncounter";
    public static final String EVENT_ENCOUNTER_POPUP_ENTOURAGE = "ContinueCreatePublicEntourage";

    // Geolocation
    public static final long UPDATE_TIMER_MILLIS_OFF_TOUR = 20000;
    public static final long UPDATE_TIMER_MILLIS_ON_TOUR = 5000;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR = 0;//30;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR = 0;//5;

    //Time constants
    public static final long MILLIS_HOUR = 3600000; //1000 * 60 * 60

    // Items per pagination
    public static final int ITEMS_PER_PAGE = 10;

    //Invite success automatic hide delay
    public static final long INVITE_SUCCESS_HIDE_DELAY = 5000; //1000 * 5

    // Don't show the popup again within this distance
    public static final int EMPTY_POPUP_DISPLAY_LIMIT = 300; //meters

    // Carousel delay time
    public static final long CAROUSEL_DELAY_MILLIS = 15000; // 15 seconds
}
