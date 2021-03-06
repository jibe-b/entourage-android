package social.entourage.android.authentication.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.register.OnRegisterUserListener;
import social.entourage.android.authentication.login.register.RegisterNumberFragment;
import social.entourage.android.authentication.login.register.RegisterSMSCodeFragment;
import social.entourage.android.authentication.login.register.RegisterWelcomeFragment;
import social.entourage.android.base.AmazonS3Utils;
import social.entourage.android.map.permissions.NoLocationPermissionFragment;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.tools.Utils;
import social.entourage.android.user.edit.photo.PhotoChooseInterface;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import social.entourage.android.view.CountryCodePicker.CountryCodePicker;
import social.entourage.android.view.HtmlTextView;

/**
 * Activity providing the login steps
 */
public class LoginActivity extends EntourageActivity implements LoginInformationFragment.OnEntourageInformationFragmentFinish, OnRegisterUserListener, PhotoChooseInterface {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    private static final int PERMISSIONS_REQUEST_PHONE_STATE = 1;
    private static final String VERSION = "Version : ";

    public final static String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";
    public final static int LOGIN_ERROR_UNAUTHORIZED = -1;
    public final static int LOGIN_ERROR_INVALID_PHONE_FORMAT = -2;
    public final static int LOGIN_ERROR_UNKNOWN = -9998;
    public final static int LOGIN_ERROR_NETWORK = -9999;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String loggedPhoneNumber;

    LoginInformationFragment informationFragment;

    private View previousView = null;

    @Inject
    LoginPresenter loginPresenter;

    private User onboardingUser;

    /************************
     * Signin View
     ************************/

    @BindView(R.id.login_include_signin)
    View loginSignin;

    @BindView(R.id.login_ccp)
    CountryCodePicker countryCodePicker;

    @BindView(R.id.login_edit_phone)
    EditText phoneEditText;

    @BindView(R.id.login_edit_code)
    EditText passwordEditText;

    @BindView(R.id.login_button_signup)
    Button loginButton;

    @BindView(R.id.login_text_lost_code)
    TextView lostCodeText;

    /************************
     * Lost Code View
     ************************/

    @BindView(R.id.login_include_lost_code)
    View loginLostCode;

    @BindView(R.id.login_lost_code_ccp)
    CountryCodePicker lostCodeCountryCodePicker;

    @BindView(R.id.login_edit_phone_lost_code)
    EditText lostCodePhone;

    @BindView(R.id.login_button_ask_code)
    Button receiveCodeButton;

    @BindView(R.id.login_block_lost_code_start)
    View enterCodeBlock;

    @BindView(R.id.login_block_lost_code_confirmation)
    View confirmationBlock;

    @BindView(R.id.login_text_confirmation)
    HtmlTextView codeConfirmation;

    @BindView(R.id.login_button_home)
    Button homeButton;

    /************************
     * Welcome View
     ************************/

    @BindView(R.id.login_include_email)
    View loginEmail;

    @BindView(R.id.login_edit_email_profile)
    EditText profileEmail;

    //@BindView(R.id.login_edit_name_profile)
    //EditText profileName;

    @BindView(R.id.login_user_photo)
    ImageView profilePhoto;

    @BindView(R.id.login_button_go)
    FloatingActionButton goButton;

    /************************
     * Enter Name View
     ************************/

    @BindView(R.id.login_include_name)
    View loginNameView;

    @BindView(R.id.login_name_firstname)
    EditText firstnameEditText;

    @BindView(R.id.login_name_lastname)
    EditText lastnameEditText;

    @BindView(R.id.login_name_go_button)
    FloatingActionButton nameGoButton;

    /************************
     * Tutorial View
     ************************/

    @BindView(R.id.login_include_tutorial)
    View loginTutorial;

    @BindView(R.id.login_button_finish_tutorial)
    Button finishTutorial;

    /************************
     * Startup View
     ************************/

    @BindView(R.id.login_include_startup)
    View loginStartup;

    /************************
     * Newsletter subscription View
     ************************/

    @BindView(R.id.login_include_newsletter)
    View loginNewsletter;

    @BindView(R.id.login_newsletter_button)
    Button newsletterButton;

    @BindView(R.id.login_newsletter_email)
    TextView newsletterEmail;

    /************************
     * Verify Code view
     ************************/

    @BindView(R.id.login_include_verify_code)
    View loginVerifyCode;

    @BindView(R.id.login_button_verify_code)
    Button verifyCodeButton;

    @BindView(R.id.login_verify_code_code)
    TextView receivedCode;

    /************************
     * Notifications Permissions View
     ************************/

    @BindView(R.id.login_include_notifications)
    View loginNotificationsView;

    /************************
     * Geolocation view
     ************************/

    @BindView(R.id.login_include_geolocation)
    View loginGeolocationView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermissions();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        EntourageEvents.logEvent(Constants.EVENT_SCREEN_01);

        loginSignin.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        loginEmail.setVisibility(View.GONE);
        loginNameView.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.GONE);
        loginNotificationsView.setVisibility(View.GONE);
        loginGeolocationView.setVisibility(View.GONE);

        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        LoginTextWatcher ltw = new LoginTextWatcher();
        firstnameEditText.addTextChangedListener(ltw);
        lastnameEditText.addTextChangedListener(ltw);

        if (loginPresenter != null) {
            AuthenticationController authenticationController = loginPresenter.authenticationController;
            if (authenticationController != null) {
                User user = authenticationController.getUser();
                if (user != null) {
                    launchFillInProfileView(user.getPhone(), user);
                }
            }
        }
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerLoginComponent.builder()
            .entourageComponent(entourageComponent)
            .loginModule(new LoginModule(this))
            .build()
            .inject(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_PHONE_STATE) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (loginSignin.getVisibility() == View.VISIBLE) {
            phoneEditText.setText("");
            passwordEditText.setText("");
            hideKeyboard();
            loginSignin.setVisibility(View.GONE);
            loginStartup.setVisibility(View.VISIBLE);
        } else if (loginLostCode.getVisibility() == View.VISIBLE) {
            lostCodePhone.setText("");
            loginLostCode.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        } else if (loginTutorial.getVisibility() == View.VISIBLE) {
            loginTutorial.setVisibility(View.GONE);
            showEmailView();
        } else if (loginEmail.getVisibility() == View.VISIBLE) {
            loginEmail.setVisibility(View.GONE);
            loginNameView.setVisibility(View.VISIBLE);
        } else if (loginNameView.getVisibility() == View.VISIBLE) {
            hideKeyboard();
            loginNameView.setVisibility(View.GONE);
            loginSignin.setVisibility(View.VISIBLE);
            showKeyboard(phoneEditText);
        } else if (loginNewsletter.getVisibility() == View.VISIBLE && previousView != null) {
            newsletterEmail.setText("");
            loginNewsletter.setVisibility(View.GONE);
            previousView.setVisibility(View.VISIBLE);
            if (previousView == loginSignin) {
                showKeyboard(phoneEditText);
            }
        } else if (loginVerifyCode.getVisibility() == View.VISIBLE) {
            showLostCodeScreen();
        } else {
            super.onBackPressed();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    @TargetApi(23)
    private void checkPermissions() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.login_permission_title)
                    .setMessage(R.string.login_permission_description)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE_STATE);
                        }
                    }).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE_STATE);
            }
        } else {
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String phoneNumber = manager.getLine1Number();
            if (phoneNumber != null) {
                phoneEditText.setText(phoneNumber);
            }
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startMapActivity() {
        stopLoader();
        hideKeyboard();
        EntourageEvents.logEvent(Constants.EVENT_LOGIN_OK);
        startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }

    public void loginFail(int errorCode) {
        stopLoader();
        @StringRes int errorMessage;
        switch (errorCode) {
            case LOGIN_ERROR_INVALID_PHONE_FORMAT:
                errorMessage = R.string.login_error_invalid_phone_format;
                EntourageEvents.logEvent(Constants.EVENT_LOGIN_FAILED);
                break;
            case LOGIN_ERROR_UNAUTHORIZED:
                errorMessage = R.string.login_error_invalid_credentials;
                EntourageEvents.logEvent(Constants.EVENT_LOGIN_FAILED);
                break;
            case LOGIN_ERROR_NETWORK:
                errorMessage = R.string.login_error_network;
                EntourageEvents.logEvent(Constants.EVENT_LOGIN_ERROR);
                break;
            default:
                errorMessage = R.string.login_error;
                EntourageEvents.logEvent(Constants.EVENT_LOGIN_ERROR);
                break;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.login_error_title)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.login_retry_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                }
            })
            .create()
            .show();
    }

    public void displayToast(@StringRes int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void startLoader() {
        loginButton.setText(R.string.button_loading);
        loginButton.setEnabled(false);
        receiveCodeButton.setText(R.string.button_loading);
        receiveCodeButton.setEnabled(false);
        lostCodePhone.setEnabled(false);
        newsletterButton.setText(R.string.button_loading);
        newsletterButton.setEnabled(false);
        verifyCodeButton.setText(R.string.button_loading);
        verifyCodeButton.setEnabled(false);
        nameGoButton.setEnabled(false);
    }

    public void stopLoader() {
        loginButton.setText(R.string.login_button_signup);
        loginButton.setEnabled(true);
        receiveCodeButton.setText(R.string.login_button_ask_code);
        receiveCodeButton.setEnabled(true);
        lostCodePhone.setEnabled(true);
        newsletterButton.setText(R.string.login_button_newsletter);
        newsletterButton.setEnabled(true);
        verifyCodeButton.setText(R.string.login_button_verify_code);
        verifyCodeButton.setEnabled(true);
        nameGoButton.setEnabled(true);
    }

    public void launchFillInProfileView(String phoneNumber, User user) {

        //TODO Need a better approach
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterSMSCodeFragment.TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
        fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterNumberFragment.TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
        fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(RegisterWelcomeFragment.TAG);
        if (fragment != null) {
            fragment.dismiss();
        }

        loggedPhoneNumber = phoneNumber;

        if (this.onboardingUser != null) {
            user.setOnboardingUser(true);
        }
        hideKeyboard();

        loginStartup.setVisibility(View.GONE);
        loginSignin.setVisibility(View.GONE);
        loginVerifyCode.setVisibility(View.GONE);
        if (user.getFirstName() == null || user.getFirstName().length() == 0 || user.getLastName() == null || user.getLastName().length() == 0) {
            showNameView();
        } else if (user.getEmail() == null || user.getEmail().length() == 0) {
            showEmailView();
        } else if (user.getAvatarURL() == null || user.getAvatarURL().length() == 0) {
            showPhotoChooseSource();
        } else {
            showGeolocationView();
        }
    }

    // ----------------------------------
    // INTERFACES CALLBACKS
    // ----------------------------------

    @Override
    public void closeEntourageInformationFragment() {
        if (informationFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(informationFragment).commit();
        }
    }

    @Override
    public void onPhotoBack() {
        EntourageEvents.logEvent(Constants.EVENT_PHOTO_BACK);
        showGeolocationView();
    }

    @Override
    public void onPhotoIgnore() {
        EntourageEvents.logEvent(Constants.EVENT_PHOTO_IGNORE);
        showGeolocationView();
    }

    @Override
    public void onPhotoChosen(final Uri photoUri, int photoSource) {

        if (photoSource == PhotoChooseSourceFragment.TAKE_PHOTO_REQUEST) {
            EntourageEvents.logEvent(Constants.EVENT_PHOTO_SUBMIT);
        }

        if (loginPresenter == null || loginPresenter.authenticationController == null || loginPresenter.authenticationController.getUser() == null) {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
            PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
            if (photoEditFragment != null) {
                photoEditFragment.onPhotoSent(false);
            }
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading);

        final String objectKey = "user_" + loginPresenter.authenticationController.getUser().getId() + ".jpg";
        TransferUtility transferUtility = AmazonS3Utils.getTransferUtility(this);
        TransferObserver transferObserver = transferUtility.upload(
            BuildConfig.AWS_BUCKET,
            "300x300/" + objectKey,
            new File(photoUri.getPath()),
            CannedAccessControlList.PublicRead
        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(final int id, final TransferState state) {
                if (state == TransferState.COMPLETED) {
                    if (loginPresenter != null) {
                        loginPresenter.updateUserPhoto(objectKey);
                        // Delete the temporary file
                        File tmpImageFile = new File(photoUri.getPath());
                        if (!tmpImageFile.delete()) {
                            // Failed to delete the file
                            Log.d("EntouragePhoto", "Failed to delete the temporary photo file");
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                        dismissProgressDialog();
                        PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                        if (photoEditFragment != null) {
                            photoEditFragment.onPhotoSent(false);
                        }
                    }
                }
            }

            @Override
            public void onProgressChanged(final int id, final long bytesCurrent, final long bytesTotal) {

            }

            @Override
            public void onError(final int id, final Exception ex) {
                Toast.makeText(LoginActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
                PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                if (photoEditFragment != null) {
                    photoEditFragment.onPhotoSent(false);
                }
            }
        });
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    /************************
     * Signin View
     ************************/

    /*
    @OnClick(R.id.login_text_more)
    void onAskMore() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        informationFragment = new LoginInformationFragment();
        informationFragment.show(fragmentManager, "fragment_login_information");
    }
    */
    @OnClick(R.id.login_back_button)
    void onLoginBackClick() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_signup)
    void onLoginClick() {
        if (loginPresenter != null) {
            loginPresenter.login(
                countryCodePicker.getSelectedCountryCodeWithPlus(),
                phoneEditText.getText().toString(),
                passwordEditText.getText().toString());
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login_text_lost_code)
    void onLostCodeClick() {
        EntourageEvents.logEvent(Constants.EVENT_SMS_CODE_REQUEST);
        hideKeyboard();
        loginSignin.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.VISIBLE);
        confirmationBlock.setVisibility(View.GONE);

        String phoneNumber = phoneEditText.getText().toString();
        lostCodePhone.setText(phoneNumber);

        showKeyboard(lostCodePhone);
    }

    /*
    @OnClick(R.id.login_welcome_more)
    void onMoreClick() {
        loginSignin.setVisibility(View.GONE);
        loginNewsletter.setVisibility(View.VISIBLE);
        previousView = loginSignin;
        showKeyboard(newsletterEmail);
    }
    */

    /************************
     * Lost Code View
     ************************/

    @OnClick(R.id.login_lost_code_close)
    void lostCodeClose() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_ask_code)
    void sendNewCode() {
        String phoneNumber = Utils.checkPhoneNumberFormat(lostCodeCountryCodePicker.getSelectedCountryCodeWithPlus(), lostCodePhone.getText().toString());
        if (phoneNumber == null) {
            displayToast(getString(R.string.login_text_invalid_format));
            return;
        }
        if (loginPresenter != null) {
            startLoader();
            loginPresenter.sendNewCode(phoneNumber);
            EntourageEvents.logEvent(Constants.EVENT_LOGIN_SEND_NEW_CODE);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login_button_home)
    void returnHome() {
        lostCodePhone.setText("");
        confirmationBlock.setVisibility(View.GONE);
        enterCodeBlock.setVisibility(View.VISIBLE);
        loginLostCode.setVisibility(View.GONE);
        loginSignin.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
    }

    void newCodeAsked(User user, boolean isOnboarding) {
        stopLoader();
        if (user != null) {
            if (isOnboarding) {
                //registerPhoneNumberSent(onboardingUser.getPhone(), true);
                displayToast(R.string.registration_smscode_sent);
            } else {
                if (loginLostCode.getVisibility() == View.VISIBLE) {
                    loginLostCode.setVisibility(View.GONE);
                    loginVerifyCode.setVisibility(View.VISIBLE);
                } else {
                    displayToast(getString(R.string.login_text_lost_code_ok));
                }
            }
        } else {
            if (isOnboarding) {
                EntourageEvents.logEvent(Constants.EVENT_SCREEN_03_2);
                displayToast(getString(R.string.login_text_lost_code_ko));
            } else {
                if (loginLostCode.getVisibility() == View.VISIBLE) {
                    //codeConfirmation.setText(R.string.login_text_lost_code_ko);
                    codeConfirmation.setHtmlString(R.string.login_text_lost_code_ko_html);
                    enterCodeBlock.setVisibility(View.GONE);
                    confirmationBlock.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /************************
     * Email View
     ************************/

    private void showEmailView() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_30_4);
        loginEmail.setVisibility(View.VISIBLE);
        profileEmail.requestFocus();
    }

    @OnClick(R.id.login_email_back_button)
    void onEmailBackClicked() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_go)
    void saveEmail() {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_EMAIL_SUBMIT);
            loginPresenter.updateUserEmail(profileEmail.getText().toString());

            loginPresenter.updateUserToServer();
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login_email_ignore_button)
    void ignoreEmail() {
        EntourageEvents.logEvent(Constants.EVENT_EMAIL_IGNORE);
        if (loginPresenter != null) {
            loginPresenter.updateUserToServer();
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    /************************
     * Enter Name View
     ************************/

    void showNameView() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_30_5);
        loginNameView.setVisibility(View.VISIBLE);

        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            User user = loginPresenter.authenticationController.getUser();
            if (user != null) {
                if (user.getFirstName() != null) {
                    firstnameEditText.setText(user.getFirstName());
                }
                if (user.getLastName() != null) {
                    lastnameEditText.setText(user.getLastName());
                }
            }
        }

        firstnameEditText.requestFocus();
    }

    @OnClick(R.id.login_name_back_button)
    void onNameBackClicked() {
        onBackPressed();
    }

    @OnClick(R.id.login_name_go_button)
    void onNameGoClicked() {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_NAME_SUBMIT);
            String firstname = firstnameEditText.getText().toString().trim();
            String lastname = lastnameEditText.getText().toString().trim();
            if (firstname.length() == 0) {
                Toast.makeText(this, R.string.login_firstname_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (lastname.length() == 0) {
                Toast.makeText(this, R.string.login_lastname_error, Toast.LENGTH_SHORT).show();
                return;
            }
            hideKeyboard();
            loginPresenter.updateUserName(firstname, lastname);
            User user = loginPresenter.authenticationController.getUser();
            if (user != null) {
                if (user.getEmail() == null || user.getEmail().length() == 0) {
                    loginNameView.setVisibility(View.GONE);
                    showEmailView();
                    return;
                }
            }
            loginPresenter.updateUserToServer();
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void showPhotoChooseSource() {
        if (isFinishing()) return;
        if (loginPresenter != null && loginPresenter.authenticationController != null) {
            hideKeyboard();
            loginEmail.setVisibility(View.GONE);
            loginNameView.setVisibility(View.GONE);

            User user = loginPresenter.authenticationController.getUser();
            if (user == null || user.getAvatarURL() == null || user.getAvatarURL().length() == 0) {
                PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
                fragment.show(getSupportFragmentManager(), PhotoChooseSourceFragment.TAG);
            } else {
                showGeolocationView();
            }
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void onUserPhotoUpdated(boolean updated) {
        if (isFinishing()) return;
        dismissProgressDialog();
        PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
        if (photoEditFragment != null) {
            photoEditFragment.onPhotoSent(updated);
        }
        if (updated) {
            PhotoChooseSourceFragment fragment = (PhotoChooseSourceFragment) getSupportFragmentManager().findFragmentByTag(PhotoChooseSourceFragment.TAG);
            if (fragment != null && !fragment.isStopped()) {
                fragment.dismiss();
            }
            showGeolocationView();
        } else {
            Toast.makeText(this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
        }
    }

    /************************
     * Private Methods
     ************************/

    private void saveNotifications(boolean enabled) {
        //remember the choice
        final SharedPreferences notificationsPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = notificationsPreferences.edit();
        editor.putBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.commit();
    }

    private void finishTutorial() {
        //set the tutorial as done
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(KEY_TUTORIAL_DONE, new HashSet<String>());
        loggedNumbers.add(loggedPhoneNumber);
        sharedPreferences.edit().clear().putStringSet(KEY_TUTORIAL_DONE, loggedNumbers).commit();

        startMapActivity();
    }

    /*
    TODO: put this back when the tutorial content is ready
    @OnClick(R.id.login_button_go)
    void startTutorial() {
        EntourageEvents.logEvent(Constants.EVENT_TUTORIAL_START);
        loginPresenter.updateUserEmail(profileEmail.getText().toString());
        loginEmail.setVisibility(View.GONE);
        loginTutorial.setVisibility(View.VISIBLE);
    }
    */

    /************************
     * Tutorial View
     ************************/

    /*
    TODO: put this back when the tutorial content is ready
    @OnClick(R.id.login_button_finish_tutorial)
    void finishTutorial() {
        EntourageEvents.logEvent(Constants.EVENT_TUTORIAL_END);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply();
        startActivity(new Intent(this, DrawerActivity.class));
    }
    */

    /************************
     * Startup View
     ************************/

    @OnClick(R.id.login_button_login)
    void onStartupLoginClicked() {
        EntourageEvents.logEvent(Constants.EVENT_SPLASH_LOGIN);
        showLoginScreen();
    }

    void showLoginScreen() {
        EntourageEvents.logEvent(Constants.EVENT_LOGIN_START);
        loginStartup.setVisibility(View.GONE);
        loginSignin.setVisibility(View.VISIBLE);
        showKeyboard(phoneEditText);
        this.onboardingUser = null;
    }

    @OnClick(R.id.login_button_register)
    void showRegisterScreen() {
        EntourageEvents.logEvent(Constants.EVENT_SPLASH_SIGNUP);
        this.onboardingUser = new User();
        RegisterWelcomeFragment registerWelcomeFragment = new RegisterWelcomeFragment();
        registerWelcomeFragment.show(getSupportFragmentManager(), RegisterWelcomeFragment.TAG);
    }

    /************************
     * Newsletter View
     ************************/

    @OnClick(R.id.login_newsletter_close)
    void newsletterClose() {
        hideKeyboard();
        onBackPressed();
    }

    @OnClick(R.id.login_newsletter_button)
    void newsletterSubscribe() {
        if (loginPresenter != null) {
            hideKeyboard();
            startLoader();
            loginPresenter.subscribeToNewsletter(newsletterEmail.getText().toString());
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    void newsletterResult(boolean success) {
        stopLoader();
        if (success) {
            displayToast(getString(R.string.login_text_newsletter_success));
            onBackPressed();
        } else {
            displayToast(getString(R.string.login_text_newsletter_fail));
        }
    }

    /************************
     * Verify Code View
     ************************/

    @OnClick(R.id.login_code_sent_close)
    void verifyCodeClose() {
        onBackPressed();
    }

    @OnClick(R.id.login_button_verify_code)
    void verifyCode() {
        if (loginPresenter != null) {
            loginPresenter.login(
                lostCodeCountryCodePicker.getSelectedCountryCodeWithPlus(),
                lostCodePhone.getText().toString(),
                receivedCode.getText().toString()
            );
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login_verify_code_resend)
    void resendCode() {
        sendNewCode();
    }

    @OnClick(R.id.login_verify_code_back)
    void showLostCodeScreen() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_03_1);
        hideKeyboard();
        receivedCode.setText("");
        loginVerifyCode.setVisibility(View.GONE);
        loginLostCode.setVisibility(View.VISIBLE);
        showKeyboard(lostCodePhone);
    }

    /************************
     * Register
     ************************/

    // OnRegisterUserListener
    @Override
    public void registerShowSignIn() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_02_1);
        showLoginScreen();
    }

    @Override
    public void registerSavePhoneNumber(String phoneNumber) {
        if (loginPresenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_PHONE_SUBMIT);
            loginPresenter.registerUserPhone(phoneNumber);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void registerCheckCode(final String smsCode) {
        if (loginPresenter != null && onboardingUser != null) {
            loginPresenter. login(null, onboardingUser.getPhone(), smsCode);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void registerResendCode() {
        if (loginPresenter != null && onboardingUser != null) {
            EntourageEvents.logEvent(Constants.EVENT_SCREEN_03_1);
            loginPresenter.sendNewCode(onboardingUser.getPhone(), true);
        } else {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    }

    protected void registerPhoneNumberSent(String phoneNumber, boolean smsSent) {
        if (isFinishing()) return;
        if (smsSent) {
            displayToast(R.string.registration_smscode_sent);
        }
        if (onboardingUser != null) {
            onboardingUser.setPhone(phoneNumber);
        }
        RegisterSMSCodeFragment fragment = new RegisterSMSCodeFragment();
        fragment.show(getSupportFragmentManager(), RegisterSMSCodeFragment.TAG);
    }

    /************************
     * Notifications View
     ************************/

    public void showNotificationPermissionView() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_04_3);
        loginNotificationsView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_notifications_ignore_button)
    protected void onNotificationsIgnore() {
        saveNotifications(false);
        //loginNotificationsView.setVisibility(View.GONE);
        finishTutorial();
    }

    @OnClick(R.id.login_notifications_accept)
    protected void onNotificationsAccept() {
        EntourageEvents.logEvent(Constants.EVENT_NOTIFICATIONS_ACCEPT);
        saveNotifications(true);
        //loginNotificationsView.setVisibility(View.GONE);
        finishTutorial();
    }

    /************************
     * Geolocation View
     ************************/

    private void showGeolocationView() {
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_04_2);
        loginGeolocationView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.login_geolocation_ignore_button)
    protected void onGeolocationIgnore() {
        NoLocationPermissionFragment noLocationPermissionFragment = new NoLocationPermissionFragment();
        noLocationPermissionFragment.show(getSupportFragmentManager(), NoLocationPermissionFragment.TAG);
    }

    @OnClick(R.id.login_geolocation_accept_button)
    protected void onGeolocationAccepted() {
        EntourageEvents.logEvent(Constants.EVENT_GEOLOCATION_ACCEPT);
        loginGeolocationView.setVisibility(View.GONE);
        showNotificationPermissionView();
    }

    @OnClick(R.id.login_startup_logo)
    void onEntourageLogoClick() {
        Toast.makeText(this, VERSION + BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show();
    }

    class LoginTextWatcher implements TextWatcher {
        private boolean firstEvent = true;
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            if(firstEvent) {
                EntourageEvents.logEvent(Constants.EVENT_NAME_TYPE);
                firstEvent = false;
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {

        }
    }
}
