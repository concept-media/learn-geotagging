package lab.aikibo.learngeotagging;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.icu.text.UnicodeSet;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lab.aikibo.learngeotagging.util.PhotoHandler;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements Camera.PictureCallback, SurfaceHolder.Callback {

    private Camera camera;
    private int cameraId = 0;
    private SurfaceView cameraPreview;
    private Camera.Parameters param;
    private Location loc;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPreview = (SurfaceView) findViewById(R.id.surfaceView);
        final SurfaceHolder surfaceHolder = cameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camera = Camera.open();
        param = camera.getParameters();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permission kamera belum siap", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        try {
            camera.setPreviewDisplay(cameraPreview.getHolder());
            camera.setDisplayOrientation(90);
            param.setPictureFormat(ImageFormat.JPEG);
            param.setJpegQuality(90);
            Camera.Size prefSize = getCameraPreviewSize(camera);
            param.setPictureSize(prefSize.width, prefSize.height);
            camera.setParameters(param);
            camera.startPreview();
        } catch(Exception e) {
            Toast.makeText(this, "Kamera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        Button btnAmbilGambar = (Button) findViewById(R.id.btn_take_picture);
        btnAmbilGambar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "loc : " + MainActivity.this.loc.getLatitude() + "," +
                                MainActivity.this.loc.getLongitude(), Toast.LENGTH_LONG).show();
                    takePic();
                    }
                }
        );
    }

    private void setGpsParameters(Location loc) {
        //param.removeGpsData();
        param.setGpsTimestamp(System.currentTimeMillis() / 1000);

        if(loc != null) {
            this.loc = new Location(loc.getProvider());
            this.loc.setLatitude(loc.getLatitude());
            this.loc.setLongitude(loc.getLongitude());
            boolean hasLatLon = (this.loc.getLatitude() != 0.0d) || (this.loc.getLongitude() != 0.0d);

            if(hasLatLon) {
                param.setGpsLatitude(this.loc.getLatitude());
                param.setGpsLongitude(this.loc.getLongitude());
                param.setGpsProcessingMethod(loc.getProvider().toUpperCase());
                camera.setParameters(param);
                Toast.makeText(this, "Lat Lon sudah tersimpan", Toast.LENGTH_LONG).show();
            } //else loc = null;
        }
    }

    private void takePic() {
        camera.takePicture(null, null, this);

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        camera.release();
        camera = null;
        camera = Camera.open();

        try {
            camera.setPreviewDisplay(cameraPreview.getHolder());
            camera.setDisplayOrientation(90);
            param.setPictureFormat(ImageFormat.JPEG);
            param.setJpegQuality(90);
            Camera.Size prefSize = getCameraPreviewSize(camera);
            param.setPictureSize(prefSize.width, prefSize.height);
            camera.setParameters(param);
            camera.startPreview();
        } catch(Exception e) {
            Toast.makeText(this, "Kamera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Camera.Size getCameraPreviewSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();

        Rect frame = cameraPreview.getHolder().getSurfaceFrame();
        int width = frame.width();
        int height = frame.height();

        for(Camera.Size size : supportedSizes) {
            if(size.width >= width || size.height >= height) {
                return size;
            }
        }
        return supportedSizes.get(0);
    }

    private boolean checkCameraHardware(Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else return false;

    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch(Exception e) {}
        return c;
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFileDir = getDir();

        if(!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d(TAG, "Tidak dapat membuat direktori");
            Toast.makeText(this, "Tidak dapat membuat direktori", Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Gambar_" + date + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(this, "Foto tersimpan: " + filename, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Log.d(TAG, "File " + filename + " tidak tersimpan: " + e.getMessage());
            Toast.makeText(this, "Gambar tidak tersimpan", Toast.LENGTH_LONG).show();
        }

        ExifInterface exif;
        try {
            exif = new ExifInterface(filename);
            int num1Lat = (int) Math.floor(this.loc.getLatitude());
            int num2Lat = (int) Math.floor((this.loc.getLatitude() - num1Lat) * 60);
            double num3Lat = (this.loc.getLatitude() - ((double)num1Lat + ((double)num2Lat/60))) * 3600000;

            int num1Lon = (int) Math.floor(this.loc.getLongitude());
            int num2Lon = (int) Math.floor((this.loc.getLongitude() - num1Lon) * 60);
            double num3Lon = (this.loc.getLongitude() - ((double)num1Lon - ((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");

            if(this.loc.getLatitude() > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if(this.loc.getLongitude() > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }
            exif.saveAttributes();
        } catch(IOException e) {
            Log.e("SavePicMainActivity", e.getLocalizedMessage());
        }
    }

    private File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "KameraGis");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(camera != null) {
            try {

                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch(IOException e) {
                Toast.makeText(this, "Tidak dapat menggunakan kamera", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
        camera = null;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MainActivity.this, "Lokasi : " +
                    location.getLatitude() + " - " + location.getLongitude(), Toast.LENGTH_LONG).show();
            setGpsParameters(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
