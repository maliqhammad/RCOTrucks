package com.rco.rcotrucks.utils.zip;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UnZip extends AsyncTask<Void, Integer, Integer> {
    private static final String TAG = "zip: "+UnZip.class.getName();

    private String _zipFile;
    private String _location;
    private int per = 0;

    public UnZip(String zipFile, String location) {
        Log.d(TAG, "UnZip: zipFile: "+zipFile+" destinationLocation: "+location);
        _zipFile = zipFile;
        _location = location;
        _dirChecker("");
    }

    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: ");
        try {
//            ZipFile zip = new ZipFile(_zipFile);
//            bar.setMax(zip.size());
//            Log.d(TAG, "doInBackground: size: "+zip.size());
            FileInputStream fin = new FileInputStream(_zipFile);

            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                Log.d(TAG, "Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    _dirChecker(ze.getName());
                } else {
                    // Here I am doing the update of my progress bar
                    Log.d(TAG, "more " + ze.getName());

                    per++;
                    publishProgress(per);

                    FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {

                        fout.write(c);
                    }
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
        } catch (Exception e) {
            Log.e(TAG, "unzip", e);
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        Log.d(TAG, "onProgressUpdate: progress: "+progress);
//        bar.setProgress(per); //Since it's an inner class, Bar should be able to be called directly
    }

    protected void onPostExecute(Integer... result) {
        Log.i(TAG, "onPostExecute: Completed. Total size: " + result);
    }

    private void _dirChecker(String dir) {
        Log.d(TAG, "_dirChecker: ");
        File f = new File(_location + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }


    public static void streamCopy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[32 * 1024]; // play with sizes..
        int readCount;
        while ((readCount = in.read(buffer)) != -1) {
            out.write(buffer, 0, readCount);
        }
    }

//    else {
//        // Here I am doing the update of my progress bar
//        Log.d("Decompress", "more " + ze.getName());
//
//        per++;
//        publishProgress(per);
//
//        FileOutputStream fout = new FileOutputStream(_location + ze.getName());
//
//        streamCopy(zin, fout);
//
//        zin.closeEntry();
//        fout.close();
//    }
}
