package social.entourage.android.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.R;

public class AboutActivity extends AppCompatActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final String RATE_URL = "market://details?id=";
    private static final String FACEBOOK_URL = "https://www.facebook.com/EntourageReseauCivique";
    private static final String TERMS_URL = "http://www.entourage.social/cgu/index.html";
    private static final String WEBSITE_URL = "http://www.entourage.social";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.about_version)
    TextView versionTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        populate();
    }

    private void populate() {
        versionTextView.setText("v"+BuildConfig.VERSION_NAME);
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.about_close_button)
    protected void onCloseButton() {
        finish();
    }

    @OnClick(R.id.about_rate_us_layout)
    protected void onRateUsClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ABOUT_RATING);

        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    @OnClick(R.id.about_facebook_layout)
    protected void onFacebookClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ABOUT_FACEBOOK);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_conditions_layout)
    protected void onTermsClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ABOUT_CGU);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_website_layout)
    protected void onWebsiteClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ABOUT_WEBSITE);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
        startActivity(browserIntent);
    }

    @OnClick(R.id.about_email_layout)
    protected void onEmailClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        String[] addresses = {Constants.EMAIL_CONTACT};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
