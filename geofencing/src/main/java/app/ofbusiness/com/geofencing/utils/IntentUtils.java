package app.ofbusiness.com.geofencing.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IntentUtils {

    public static final String PROVIDER = "com.ofbusiness.io.debug.fileprovider";

    public static String captureImage(Activity baseActivity, int requestCode) {
        String path = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(baseActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Pair<File, String> imageFile = createImageFile(baseActivity);
                photoFile = imageFile.first;
                path = imageFile.second;
            } catch (IOException ex) {
                // Error occurred while creating the File
                //AppLog.w(TAG, "Failed to create iamge file.", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(baseActivity, PROVIDER, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                baseActivity.startActivityForResult(takePictureIntent, requestCode);
            }
        }
        return path;
    }

    private static Pair<File, String> createImageFile(Activity baseActivity)
            throws IOException {
        // Create an image file name
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "OFB_" + timeStamp + "_";
        File storageDir = baseActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);

        // Save a file: path for use with ACTION_VIEW intents
        //String path = "file:" + image.getAbsolutePath();
        String path = image.getAbsolutePath();
        return new Pair<>(image, path);
    }
}
