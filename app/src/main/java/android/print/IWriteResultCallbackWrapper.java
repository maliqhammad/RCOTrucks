package android.print;

/**
 * Added this interface for flexibility because class android.print.PrintDocumentAdapter.WriteResultCallback
 *  is abstract and not based on an interface.  This will allow any class to serve as a PrintDocumentAdapter.WriteResultCallback
 *  through delegation. See PdfPrint.getWriteResultCallbackDelegate() .
 *  **** Warning: I first named this class IWriteResultCallback, but the runtime found another class by that
 *  name somewhere in the system jars and caused a puzzling error.
 *  -RAN 2/11/2021
 */
public interface IWriteResultCallbackWrapper {
        /**
         * Notifies that all the data was written.
         *
         * @param pages The pages that were written. Cannot be <code>null</code>
         *        or empty. <br />
         *        Returning {@link PageRange#ALL_PAGES} indicates that all pages that were
         *        requested as the {@code pages} parameter in {@link PrintDocumentAdapter#onWrite} were written.
         */
        public void onWriteFinished(PageRange[] pages);

        /**
         * Notifies that an error occurred while writing the data.
         *
         * @param error The <strong>localized</strong> error message.
         * shown to the user. May be <code>null</code> if error is unknown.
         */
        public void onWriteFailed(CharSequence error);

        /**
         * Notifies that write was cancelled as a result of a cancellation request.
         */
        public void onWriteCancelled();
}
