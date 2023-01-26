package com.rco.rcotrucks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_PICTURES;

public class ImageUtils {
    public static final String TAG = "ImageUtils";

    public static Bitmap getBitmapFromBytes(byte[] data) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bmp;
    }


    /**
     * Mutable bitmap is useful for initializing Canvas for drawing.
     *
     * @param data
     * @return
     */
    public static Bitmap getMutableBitmapFromBytes(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return bmp;
    }

    // convert from bitmap to byte array
    public static byte[] getPngBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.d(TAG, "scanGallery() MediaScannerConnection.OnScanCompletedListener() Saved Image Successfully.., path=" + path + ", uri=" + uri);
//                    Toast.makeText(SignatureActivity.this, "Save Image Successfully..", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue scanning gallery. path=" + path);
        }
    }

    /**
     * Probably should call this from a non-UI thread.  Mostly used for debugging at the moment,
     * so needs cleanup, particularly picture directory locating.
     *
     * @param context
     * @param bitmap
     * @param folderName
     * @param filename
     * @return
     */
    public static File saveBitmapToPngFile(Context context, Bitmap bitmap, String folderName, String filename) {
        File pictureFile = null;
        String strThis = "saveBitmapToPngFile(), ";

        try {

            File pictureFileDir = null;
            pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), folderName);
            Log.d(TAG, strThis + "Let's try to store bitmap to pictureFileDir: " + pictureFileDir.getCanonicalPath());

            if (!pictureFileDir.exists()) {
                Log.d(TAG, strThis + "Case: directory: " + pictureFileDir.getCanonicalPath() + " does not exist.  Will try to create.");
                boolean isDirectoryCreated = pictureFileDir.mkdirs();

                if (!isDirectoryCreated) {
                    Log.d("TAG", strThis + "Case: Failed to create directory " + pictureFileDir + " to save the image.  Will try context.getExternalFilesDir(DIRECTORY_PICTURES)");

//                    pictureFileDir = context.getFilesDir();
                    pictureFileDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
                    Log.d(TAG, strThis + "Checking Directory: " + pictureFileDir);
                    pictureFileDir = new File(pictureFileDir, folderName);

                    if (!pictureFileDir.exists()) {
                        Log.d(TAG, strThis + "Case: Directory: " + pictureFileDir + " does not exist.  Will try to create.");
                        isDirectoryCreated = pictureFileDir.mkdirs();
                        if (!isDirectoryCreated) {
                            Log.d("TAG", "Can't create directory " + pictureFileDir + " to save the image");
                            throw new Exception("Can't create directory " + pictureFileDir + " to save the image");
                        } else
                            Log.d("TAG", "Using newly created directory " + pictureFileDir + " to save the image instead");
                    } else
                        Log.d("TAG", "Case: Directory " + pictureFileDir + " already exists, will use to save the image instead");
                }
//                return null;
                // Try local storage
            }


//            String filename = pictureFileDir.getPath() + File.separator + System.currentTimeMillis() + ".png";
            if (!filename.endsWith(".png")) filename += ".png";

            pictureFile = new File(pictureFileDir, filename);
            Log.d(TAG, "saveBitmapToPngFile() pictureFileDir.getCanonicalPath()=" + pictureFileDir.getCanonicalPath()
                    + ", pictureFile.getCanonicalPath()=" + pictureFile.getCanonicalPath());
            if (pictureFile.exists()) pictureFile.delete();
            pictureFile.createNewFile();  // is this needed?
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
            Log.d(TAG, "saveBitmapToPngFile() Saved image to: " + pictureFile.getCanonicalPath());
//            Toast.makeText(context, "Saved Image Successfully to: " + pictureFile.getCanonicalPath(), Toast.LENGTH_SHORT).show();
//            scanGallery(context, pictureFile.getAbsolutePath());
        } catch (IOException e) {
            Log.i("TAG", "There was an issue saving the image. " + e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.i("TAG", "There was an issue saving the image. " + e);
            e.printStackTrace();
        }

        return pictureFile;
    }

    public static String FetchDirectionImage(String directionType) {

//        Aug 04, 2022  -   Added the custom round about maneuvers
//        (these three - direction_rotary_right, direction_rotary_straight, direction_rotary_left)
        if (directionType == null)
            return "direction_depart";
        else if (directionType.equals("fork-left")) {
            return "direction_fork_left";
        } else if (directionType.equals("keep-left")) {
            return "direction_turn_slight_left";
        } else if (directionType.equals("direction_roundabout_right")) {
            return "direction_roundabout_right";
        } else if (directionType.equals("direction_rotary_slight_right")) {
            return "direction_rotary_slight_right";
        } else if (directionType.equals("direction_rotary_straight")) {
            return "direction_rotary_straight";
        } else if (directionType.equals("direction_rotary_slight_left")) {
            return "direction_rotary_slight_left";
        } else if (directionType.equals("direction_rotary_left")) {
            return "direction_rotary_left";
        } else if (directionType.equals("direction_rotary_sharp_left")) {
            return "direction_rotary_sharp_left";
        } else if (directionType.equals("direction_rotary_right"))
            return "direction_rotary_right";
        else if (directionType.equals("direction_rotary_slight_right"))
            return "direction_rotary_slight_right";
        else if (directionType.equals("direction_rotary_straight"))
            return "direction_rotary_straight";
        else if (directionType.equals("direction_rotary_slight_left"))
            return "direction_rotary_slight_left";
        else if (directionType.equals("direction_rotary_left"))
            return "direction_rotary_left";
        else if (directionType.equals("direction_on_ramp_straight"))
            return "direction_on_ramp_straight";
        else if (directionType.equals("turn-sharp-left"))
            return "direction_turn_sharp_left";
        else if (directionType.equals("uturn-right"))
            return "direction_uturn";
        else if (directionType.equals("turn-slight-right"))
            return "direction_turn_slight_right";
        else /* 2022.08.18 this is an empty image!!!!
            if (directionType.equals("merge"))
            return "direction_merge_straight";
        else */
            if (directionType.equals("roundabout-left") || directionType.equals("direction_roundabout_left"))
            return "direction_roundabout_left";
        else if (directionType.equals("roundabout-right") || directionType.equals("direction_roundabout_right") )
            return "direction_roundabout_right";
        else if (directionType.equals("direction_on_ramp_sharp_left"))
            return "direction_on_ramp_sharp_left";
        else if (directionType.equals("uturn-left"))
            return "direction_uturn";
        else if (directionType.equals("turn-slight-left"))
            return "direction_turn_slight_left";
        else if (directionType.equals("turn-left"))
            return "direction_turn_left";
        else if (directionType.equals("ramp-right"))
            return "direction_on_ramp_right";
        else if (directionType.equals("turn-right"))
            return "direction_turn_right";
        else if (directionType.equals("fork-right"))
            return "direction_fork_right";
        else if (directionType.equals("ferry-train"))
            return "direction_ferry_train";
        else if (directionType.equals("turn-sharp-right"))
            return "direction_turn_sharp_right";
        else if (directionType.equals("ramp-left"))
            return "direction_on_ramp_left";
        else if (directionType.equals("ferry"))
            return "direction_ferry";
        else if (directionType.equals("straight"))
            return "direction_new_name_straight";
        else if (directionType.equals("fork-left"))
            return "direction_fork_left";
        else if (directionType.contains("merge"))
            return "merge";
        else if (directionType.contains("direction_arrive_left"))
            return "direction_arrive_left";
        else if (directionType.contains("direction_arrive_right"))
            return "direction_arrive_right";
        else {
//            Aug 09 2022   -   Updated direction icon
            return "direction_arrive";
//            return "destination_icon_two";
        }



    }

    public static int calculateExit(int turnAngle) {
//        Log.d(TAG, "setInstructionView: calculateExit: turnAngle: "+turnAngle);
        int count = 0;
        int rest = 0;
        if (turnAngle < 0) {
            turnAngle = 360 + turnAngle;
        }

//        Log.d(TAG, "setInstructionView: calculateExit: before calculations: ");
        count = turnAngle / 45;
        rest = turnAngle % 45;
//        Log.d("setInstructionView: stepSize", "count: " + count + "rest:" + rest);

        if (rest > 25) {
            count++;
        }
        return count;
    }


    //    Aug 05, 2022  -
    public static String FetchDirectionImageForRoundAbout(int turnAngle) {

        if (turnAngle==1000) {
            return null;
        }
        int exitNumber = calculateExit(turnAngle);
        Log.d(TAG, "drawSignRoute: setInstructionView: FetchDirectionImageForRoundAbout: exitNumber: "+exitNumber);

        String directionImage = "";
        if (exitNumber == 1) {
            directionImage = "direction_rotary_sharp_right";
        } else if (exitNumber == 2) {
            directionImage = "direction_roundabout_right";
        } else if (exitNumber == 3) {
            directionImage = "direction_rotary_slight_right";
        } else if (exitNumber == 4) {
            directionImage = "direction_rotary_straight";
        } else if (exitNumber == 5) {
            directionImage = "direction_rotary_slight_left";
        } else if (exitNumber == 6) {
            directionImage = "direction_rotary_left";
        } else if (exitNumber >= 7) {
            directionImage = "direction_rotary_sharp_left";
        }
        return directionImage;
    }



}
