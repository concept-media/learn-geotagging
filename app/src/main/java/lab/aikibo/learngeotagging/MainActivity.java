package lab.aikibo.learngeotagging;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            for(String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics camChar = manager.getCameraCharacteristics(cameraId);
            }
        } catch(CameraAccessException e) {}
    }
}
