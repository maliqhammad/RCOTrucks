package com.rco.rcotrucks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.print.IWriteResultCallbackWrapper;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.rco.rcotrucks.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DocumentUtils {
    private static final String TAG = "DocumentUtils";

    public static File createPdfFileFromWebView(Context ctx, String uniqueFileName, WebView webView, String pdfFileName) {

        Bitmap bm = BitmapHelper.getBitmapFromView(webView);

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bm.getWidth(), bm.getHeight(), 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);



        Bitmap scaleBitmap = getResizedBitmap(bm, canvas.getWidth(), canvas.getHeight());

        int center = (canvas.getWidth() - bm.getWidth()) / 2;

        canvas.drawBitmap(scaleBitmap, center, 0, paint);



        document.finishPage(page);

//        String pdfName = createBillOfLadingFileName();
//        File outputFile = new File(ctx.getExternalCacheDir(), pdfName);
        File outputFile = new File(ctx.getExternalCacheDir(), pdfFileName);



        if (outputFile.exists())

            outputFile.delete();



        try {

            outputFile.createNewFile();

            OutputStream out = new FileOutputStream(outputFile);

            document.writeTo(out);

            document.close();

            out.close();

        } catch (IOException e) {

            e.printStackTrace();

        }



        return outputFile;

    }

    public static File createWebPrintJob(
            WebView webView, String destFolderPathRelative, String pdfFileNameWithExt,
            IWriteResultCallbackWrapper writeResultCallbackOptional) throws IOException {

        String jobName = webView.getContext().getString(R.string.app_name) + " Document";
        int dpi = 200; // This is probably ignored, or it may be metadata for pdf file.

        Log.d(TAG, "createWebPrintJob() called with: jobName = [" + jobName
                + "], webView = [" + webView
                + "], destFolderPathRelative = [" + destFolderPathRelative + "], pdfFileNameWithExt = ["
                + pdfFileNameWithExt + "], dpi = [" + dpi + "]");

        File file = getLocalFileHandle(webView.getContext(), destFolderPathRelative, pdfFileNameWithExt);

        PrintDocumentAdapter.WriteResultCallback callback = null;

        if (writeResultCallbackOptional != null)
            callback = PdfPrint.getWriteResultCallbackDelegate(writeResultCallbackOptional);
        else
            callback = PdfPrint.getWriteResultCallbackDoNothing();

        File filePrinted = createWebPrintJob(jobName, webView,
                file, dpi, callback);

        return filePrinted;
    }

// Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.
//    private ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // Permission is granted. Continue the action or workflow in your
//                    // app.
//                } else {
//                    // Explain to the user that the feature is unavailable because the
//                    // features requires a permission that the user has denied. At the
//                    // same time, respect the user's decision. Don't link to system
//                    // settings in an effort to convince the user to change their
//                    // decision.
//                }
//            });

//    public static void checkPermission(Context ctx) {
//        if (ContextCompat.checkSelfPermission(
//                ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
//                PackageManager.PERMISSION_GRANTED) {
//            // You can use the API that requires the permission.
//            performAction(...);
//        } else if (shouldShowRequestPermissionRationale(...)) {
//            // In an educational UI, explain to the user why your app requires this
//            // permission for a specific feature to behave as expected. In this UI,
//            // include a "cancel" or "no thanks" button that allows the user to
//            // continue using your app without granting the permission.
//            showInContextUI(...);
//        } else {
//            // You can directly ask for the permission.
//            // The registered ActivityResultCallback gets the result of this request.
//            requestPermissionLauncher.launch(
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//    }

    /**
     *
     * @param jobName a name for the print job to identify it in a print queue (I guess).  Example:  ctx.getString(R.string.app_name) + " Document"
     * @param webView the WebView to be printed to pdf file.
//     * @param externalStorageType Android external storage "type" for designating a built-in folder (e.g. Environment.DIRECTORY_..) orig ref: (relative to external storage path) for created print file, e.g. Environment.DIRECTORY_DCIM + "/PDFTest/"
     * @param fileDest File object specifying the pathname of the destination file to write the PDF file to.  If the file exists, it will be deleted and a new one created.
     * @param dpi dots per inch, e.g. 300 or 600
     * @return File handle to printed file in the Android storage.
     */
    public static File createWebPrintJob(String jobName, WebView webView, //String externalStorageType,
                                         File fileDest, int dpi, PrintDocumentAdapter.WriteResultCallback writeResultCallback) {
        Log.d(TAG, "createWebPrintJob() called with: jobName = [" + jobName + "], webView = [" + webView + "], fileDest = [" + fileDest + "], dpi = [" + dpi + "]");

        PrintAttributes attributes = PdfPrint.getStandardUsPdfPrintAttributes(dpi);

        if (fileDest.exists()) {
            try {
                boolean isSuccess = fileDest.delete();
                if (!isSuccess)
                    Log.d(TAG, "createWebPrintJob() ***** Error - fileDest: " + fileDest.getCanonicalPath()
                            + " already exists.  Attempt to delete it failed for unknown reasons.");
                else
                    Log.d(TAG, "createWebPrintJob() deleted existing fileDest: " + fileDest.getCanonicalPath()
                        + ", fileDest.getAbsolutePath()=" + fileDest.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File filePdfPrinted = PdfPrint.print(webView.createPrintDocumentAdapter(jobName), attributes, fileDest, writeResultCallback);

        return filePdfPrinted;
    }

    /**
     *
     * @param view a view to be drawn to a new bitmap.
     * @param reductionFactor amount to divide the view dimensions for the bitmap dimensions, e.g. 8 to reduce dimensions by factor of 8.  This is a crude form of compression if less resolution is acceptable.
     * @param bitmapConfiguration e.g. Bitmap.Config.RGB_565
     * @return the bitmap that is an optionally scaled bitmap of the view appearance.
     */
    public static Bitmap getBitmapFromView(View view, int reductionFactor, Bitmap.Config bitmapConfiguration)
    {
        int iscale = 8;
        float fscale = 1.0f/iscale;
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() / iscale, view.getHeight() / iscale, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(fscale, fscale);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int width, int height) {
        // Todo: code for resized bitmap;
        return null;
    }

    public static File getLocalFileHandle(Context ctx, String destFolderPathRelative, String pdfFileNameWithExt) throws IOException {
        File fileExtStorageTypePath = ctx.getFilesDir();

        Log.d(TAG, "getLocalFileHandle() Start. fileExtStorageTypePath=" + fileExtStorageTypePath.getCanonicalPath());

        File fileDestinationFolder = null;

        if (destFolderPathRelative != null) {
            fileDestinationFolder = new File(fileExtStorageTypePath, destFolderPathRelative);
            if (!fileDestinationFolder.exists()) fileDestinationFolder.mkdirs();
        } else fileDestinationFolder = fileExtStorageTypePath;

        File fileRet = new File(fileDestinationFolder, pdfFileNameWithExt);
        Log.d(TAG, "getLocalFileHandle() End. fileRet.getCanonicalPath()=" + fileRet.getCanonicalPath());
        return fileRet;
    }


    // ------------------------------------ Nested Classes -----------------------------------
    public static class BitmapHelper {
        public static Bitmap getBitmapFromView(WebView webView) {
            // Todo: code to get bitmap from WebView.

            return null;
        }
    }
}
