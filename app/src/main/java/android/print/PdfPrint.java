package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;

public class PdfPrint {

    private static final String TAG = PdfPrint.class.getSimpleName();
    private final PrintAttributes printAttributes;

    public PdfPrint(PrintAttributes printAttributes) {
        this.printAttributes = printAttributes;
    }

    public File print(final PrintDocumentAdapter printAdapter, final File fileDestFolder, final String fileName) {

        if (!fileDestFolder.exists()) {
            boolean isSuccess = fileDestFolder.mkdirs();
            Log.d(TAG, "PdfPrint#print() after fileDestFolder.mkdirs(), isSuccess=" + isSuccess);
        }

        final File file = new File(fileDestFolder, fileName);

        PrintDocumentAdapter.WriteResultCallback writeResultCallback = new PrintDocumentAdapter.WriteResultCallback() {
            @Override
            public void onWriteFinished(PageRange[] pages) {
                super.onWriteFinished(pages); // probably does nothing.
                Log.d(TAG, "printAdapter.onWrite() onWriteFinished() ---------------------- file.length()="
                        + file.length() + ", pages.length=" + pages.length
                );
            }
        };

        return print(printAdapter, printAttributes, file, writeResultCallback);
    }

    public static File print(final PrintDocumentAdapter printAdapter, PrintAttributes printAttributes, final File file,
                             final PrintDocumentAdapter.WriteResultCallback writeResultCallback) {

        try {
            file.createNewFile();

            printAdapter.onLayout(null, printAttributes, null, new PrintDocumentAdapter.LayoutResultCallback() {
                @Override
                public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                    final ParcelFileDescriptor pfd = getOutputFileParcelFileDescriptor(file); // -RAN 2/10/2021
                    printAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, pfd, new CancellationSignal(), writeResultCallback);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e);
        }

        return file;
    }

    private static ParcelFileDescriptor getOutputFileParcelFileDescriptor(File fileOutputFile) {
        try {
            Log.d(TAG, "getOutputFileParcelFileDescriptor() called with: fileOutputFile = [" + fileOutputFile.getCanonicalPath() + "]");
            return ParcelFileDescriptor.open(fileOutputFile, ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e);
        }
        return null;
    }

    public static PrintDocumentAdapter.WriteResultCallback getWriteResultCallbackDelegate(final IWriteResultCallbackWrapper writeResultCallback) {
        PrintDocumentAdapter.WriteResultCallback callback = new PrintDocumentAdapter.WriteResultCallback() {
            @Override
            public void onWriteFinished(PageRange[] pages) {
                super.onWriteFinished(pages);
                writeResultCallback.onWriteFinished(pages);
            }

            @Override
            public void onWriteFailed(CharSequence error) {
                super.onWriteFailed(error);
                writeResultCallback.onWriteFailed(error);
            }

            @Override
            public void onWriteCancelled() {
                super.onWriteCancelled();
                writeResultCallback.onWriteCancelled();
            }
        };

        return callback;
    }

    public static PrintDocumentAdapter.WriteResultCallback getWriteResultCallbackDoNothing() {
       return new PrintDocumentAdapter.WriteResultCallback() {
        };
    }

    /**
     *  Get some standard print attributes for printing on standard US 8 1/2 x 11 paper.
     * @param dpi dots per inch - not sure if actually used by pdf printing.
     * @return
     */
    public static PrintAttributes getStandardUsPdfPrintAttributes(int dpi) {
        PrintAttributes attributes = new PrintAttributes.Builder()
//                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", dpi, dpi))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
        return attributes;
    }
}