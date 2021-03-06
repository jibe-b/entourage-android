package social.entourage.android.user.edit.photo;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class TakePhotoActivity extends AppCompatActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int REQUEST_TAKE_PHOTO = 2;

    public static final String KEY_PHOTO_PATH = "social.entourage.android.photo_path";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    String mCurrentPhotoPath;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the take photo intent
        Intent intent = getIntent();
        if (intent != null) {
            Uri photoFileUri = null;
            if (intent.getData() != null) {
                photoFileUri = intent.getData();
            }
            mCurrentPhotoPath = getIntent().getStringExtra(KEY_PHOTO_PATH);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = ClipData.newUri(getContentResolver(), "A photo", photoFileUri);

                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            else {
                List<ResolveInfo> resInfoList= getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, photoFileUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            if (photoFileUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
            }
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.getData() != null) {
                    BusProvider.getInstance().post(new Events.OnPhotoChosen(intent.getData()));
                    return;
                }
                if (mCurrentPhotoPath != null) {
                    BusProvider.getInstance().post(new Events.OnPhotoChosen(Uri.fromFile(new File(mCurrentPhotoPath))));
                }
            }
            finish();
        }
    }
}
