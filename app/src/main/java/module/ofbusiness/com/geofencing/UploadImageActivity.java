package module.ofbusiness.com.geofencing;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import app.ofbusiness.com.geofencing.UploadImageFragment;


public class UploadImageActivity extends AppCompatActivity implements UploadImageFragment.UploadImageFragmentCallbacks {

    UploadImageFragment uploadImageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_image_activity);

        uploadImageFragment = (UploadImageFragment) getSupportFragmentManager().findFragmentById(R.id.upload_image_frag);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void addTab() {
        new MockApiCall().execute();
    }

//    @Override
//    public int setPrimaryColor() {
//        return Color.parseColor("#000000");
//    }

    //--------------------------

    private class MockApiCall extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(UploadImageActivity.this, "InActivity",Toast.LENGTH_SHORT).show();
            uploadImageFragment.addClickedImageFragment();
        }
    }
}

