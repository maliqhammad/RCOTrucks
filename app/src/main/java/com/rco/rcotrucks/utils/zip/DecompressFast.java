package com.rco.rcotrucks.utils.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

public class DecompressFast {
    private static final String TAG = DecompressFast.class.getName();
    private String _zipFile;
    private String _location;

    public DecompressFast(String zipFile, String location) {
        Log.d(TAG, "DecompressFast: ");
        _zipFile = zipFile;
        _location = location;
        _dirChecker("");
    }

    public void unzip() {
        Log.d(TAG, "unzip: ");
        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v(TAG, "Unzipping " + ze.getName());
                if (!ze.getName().contains("MACOSX")) {
                    if (ze.isDirectory()) {
                        _dirChecker(ze.getName());
                    }
                    Log.d(TAG, "unzip: ze: name: " + ze.getName());

//                else {
                    FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zin.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    bufout.close();
                    zin.closeEntry();
                    fout.close();
//                }
                }
            }
            zin.close();
            Log.d(TAG, "Unzipping complete. path :  " + _location);
        } catch (Exception e) {
            Log.e(TAG, "unzip failed: exception: ", e);
            Log.d(TAG, "Unzipping failed");
        }
    }

    private void _dirChecker(String dir) {
        Log.d(TAG, "_dirChecker: ");
//        File f = new File(_location + "/" + dir);
        File f = new File(_location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
