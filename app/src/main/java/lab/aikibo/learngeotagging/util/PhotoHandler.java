package lab.aikibo.learngeotagging.util;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import lab.aikibo.learngeotagging.MainActivity;

import static android.content.ContentValues.TAG;

/**
 * Created by tamami on 11/12/17.
 */

public class PhotoHandler implements Camera.PictureCallback {

    private final Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFileDir = getDir();

        if(!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d(TAG, "Tidak dapat membuat direktori untuk simpan gambar");
            Toast.makeText(context, "Tidak dapat membuat direktori untuk simpan gambar",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Gambar__" + date + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "Gambar baru tersimpan: " + photoFile, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Log.d(TAG, "File " + filename + " tidak tersimpan: " + e.getMessage());
            Toast.makeText(context, "Gambar tidak dapat disimpan", Toast.LENGTH_LONG).show();
        }
    }

    private File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "GISInfo");
    }
}
