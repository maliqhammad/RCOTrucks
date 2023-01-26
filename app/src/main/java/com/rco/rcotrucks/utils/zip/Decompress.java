package com.rco.rcotrucks.utils.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class Decompress {
    private static final String TAG = Decompress.class.getName();
    private static final int BUFFER_SIZE = 1024 * 10;

    public static void unzipFromAssets(Context context, String zipFile, String destination) {
        try {
            if (destination == null || destination.length() == 0)
                destination = context.getFilesDir().getAbsolutePath();

            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(zipFile);

            String path = Environment.getExternalStorageDirectory() + "/" + "RCOTrucks/";
// Create the parent path
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

//            unzip(inputStream, destination);
            unzip(inputStream, dir);
        } catch (IOException e) {
            Log.d(TAG, "unzipFromAssets: iosException: " + e);
            e.printStackTrace();
        }
    }

    public static void unzip(String zipFile, String location) {
        try {
            FileInputStream fin = new FileInputStream(zipFile);
//            unzip(fin, location);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "unzipFromAssets: FileNotFoundException: " + e);
            e.printStackTrace();
        }

    }

//    public static void unzip(InputStream stream, String destination) {
    public static void unzip(InputStream stream, File dir) {
//        dirChecker(destination, "tile");
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            ZipInputStream zin = new ZipInputStream(stream);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {
                Log.d(TAG, "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
//                    dirChecker(destination, ze.getName());
                    dirChecker(dir.getAbsolutePath(), ze.getName());
                } else {
//                    File f = new File(destination, ze.getName());
//                    File f = new File("rcoTrucks/offline");
//                    Log.d(TAG, "unzip: f: " + f);

//                    if (!f.getParentFile().exists()) {
//                        f.getParentFile().mkdirs();
//                        Log.d(TAG, "unzip: parentFile: " + f.getParentFile().getAbsolutePath());
//                    }
//                    Log.d(TAG, "unzip: parentFile: file: " + f);
//                    if (!f.exists()) {
//                        f.createNewFile();
//                        Log.d(TAG, "unzip: parentFile: file: " + f);
//                    }


//                    if (!f.exists()) {
////                        boolean success = f.mkdirs();
//                        boolean success = f.createNewFile();
//                        if (!success) {
//                            Log.d(TAG, "Failed to create file " + f.getName());
////                            continue;
//                            break;
//                        }
//                        FileOutputStream fout = new FileOutputStream(f);
//                        int count;
//                        while ((count = zin.read(buffer)) != -1) {
//                            fout.write(buffer, 0, count);
//                        }
//                        zin.closeEntry();
//                        fout.close();
//                    }

                    if (!dir.exists()) {
//                        boolean success = f.mkdirs();
//                        boolean success = dir.createNewFile();
//                        if (!success) {
//                            Log.d(TAG, "Failed to create file " + dir.getName());
////                            continue;
//                            break;
//                        }
                        FileOutputStream fout = new FileOutputStream(dir);
                        int count;
                        while ((count = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                        zin.closeEntry();
                        fout.close();
                    }
                }

            }
            zin.close();
        } catch (Exception e) {
            Log.e(TAG, "unzip", e);
        }

    }

    private static void dirChecker(String destination, String dir) {
        File f = new File(destination, dir);

        if (!f.isDirectory()) {
            boolean success = f.mkdirs();
            Log.d(TAG, "dirChecker: folder created");
            if (!success) {
                Log.d(TAG, "Failed to create folder " + f.getName());
            }
        }
    }
}