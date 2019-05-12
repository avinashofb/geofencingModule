package module.ofbusiness.com.geofencing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import static module.ofbusiness.com.geofencing.GeoTaggingActivity.ARG_FILE_NAME;
import static module.ofbusiness.com.geofencing.GeoTaggingActivity.ARG_FILE_PATH;
import static module.ofbusiness.com.geofencing.GeoTaggingActivity.ARG_IMG_INFO_DTO;

public class ClickedImageFragment extends Fragment {

    public String filePath, fileName;
    public GeoTaggedImageMeta geoTaggedImageMeta;

    public ClickedImageFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.image_item_view, container, false);
        ImageView imageView = view.findViewById(R.id.image_view);
        TextView imageNameTextView = view.findViewById(R.id.file_name);

        if (getArguments() != null) {
            filePath = getArguments().getString(ARG_FILE_PATH);
            fileName = getArguments().getString(ARG_FILE_NAME);
            geoTaggedImageMeta = (GeoTaggedImageMeta) getArguments().getSerializable(ARG_IMG_INFO_DTO);

            imageNameTextView.setText(fileName);

            if (filePath != null) {
                File imgFile = new File(filePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(getResizedBitmap(bitmap, 800));
                }
            }
        }
        return view;
    }

    public GeoTaggedImageMeta getGeoTaggedImageMeta(){
        return geoTaggedImageMeta;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
