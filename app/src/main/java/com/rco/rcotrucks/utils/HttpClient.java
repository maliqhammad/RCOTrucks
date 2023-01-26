package com.rco.rcotrucks.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpClient {
    public static String TAG = HttpClient.class.getName();
    private static int NUMBER_OF_RETRIES = 2;
    private static String CODEPAGE = "UTF-8";
    private final static int timeoutMillis = 1 * 15 * 1000;

    public static String get2(String url) throws IOException {
        String strThis = "get2(), ";

        Log.d(TAG, strThis = ", url=" + url);

        Date startDate = new Date();
        String result;

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutMillis);        // Set the timeout in milliseconds until a connection is established. The default value is zero, that means the timeout is not used.
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutMillis);                // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity != null) {
            System.gc();
            InputStream in = httpEntity.getContent();
            result = convertStreamToString(in); // this call closes inputstream. -RAN
            in.close();

            Date endDate = new Date();
            long elapsedSecs = (endDate.getTime() - startDate.getTime()) / 1000;
            Log.d(TAG, strThis + "(" + elapsedSecs + "secs) := " + result);

            return result;
        }

        return null;
    }

    public static String post(String url, String postBody) throws IOException {
        return post(url, postBody, null);
    }

    public static String post(String url, String postBody, HashMap<String, String> headers) throws IOException {
        Log.d(TAG, url + "\n" + postBody);

        Date startDate = new Date();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(postBody, CODEPAGE));

        httpPost.setHeader("Content-Type", "application/json");

        if (headers != null)
            for (Map.Entry<String, String> header : headers.entrySet())
                httpPost.setHeader(header.getKey(), header.getValue());

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutMillis);        // Set the timeout in milliseconds until a connection is established. The default value is zero, that means the timeout is not used.
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutMillis);                // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.

        org.apache.http.client.HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity httpEntity = httpResponse.getEntity();
        String result = null;

        if (httpEntity != null) {
            System.gc();
            InputStream in = httpEntity.getContent();
            result = convertStreamToString(in);
            in.close();
        }

        Date endDate = new Date();
        long elapsedSecs = (endDate.getTime() - startDate.getTime()) / 1000;
        Log.d(TAG, "(" + elapsedSecs + "secs) := " + result);

        return result;
    }

    public static String postFile(String url, String contentType, String filename, byte[] content) throws Exception {
        return postFile(url, contentType, filename, content, true);
    }

    public static String postFile(String url, String contentType, String filename, byte[] content, boolean forceHttps) throws Exception {
        String result = null;

        Log.d(TAG, "save: postFile: url: "+url);
        Log.d(TAG, "save: postFile: content: " + content);
        for (int i = 0; i < NUMBER_OF_RETRIES; i++)
            try {
                HttpReturnInfo ret = postFileBytes(url, contentType, filename, content, forceHttps, -1);
                Log.d(TAG, "save: postFile: ret: " + ret);
                result = ret.result;
                break;
            } catch (Exception ex) {
                Log.d(TAG, "save: postFile: exception: " + ex.getMessage());
                ex.printStackTrace();
                result = null;

                if (i == NUMBER_OF_RETRIES - 1)
                    throw ex;
            }

        return result;
    }

    private static String postFileBytes(String urlStr, String fileContentType, String filename, byte[] data) throws Exception {
        return postFileBytes(urlStr, fileContentType, filename, data, true, -1).result;
    }

    public static HttpReturnInfo postFileBytes(String urlStr, String fileContentType, String filename, byte[] data, boolean isForceHttps, int timeoutMillis) throws Exception {
        String strThis = "save: postFileBytes(), ";
        String result = null;
        Log.d(TAG, strThis + "postFileBytes: STARTS ");

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        final String end = "\r\n";
        final String twoHyphens = "--";
        final String boundary = "*****";
        HttpReturnInfo returnInfo = new HttpReturnInfo();
        URL url = null;

        try {
            Log.d(TAG, strThis + "URL: " + urlStr + ",  fileContentType=" + fileContentType
                    + ", data.length=" + (data != null ? data.length : "(NULL)"));

            byte[] buffer = data;

            if (isForceHttps && urlStr.indexOf("https") < 0) {
                urlStr = urlStr.replace("http", "https");
                Log.d(TAG, strThis + "After convert to https: URL: " + urlStr);
            }

            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            Log.d(TAG, strThis + "postFileBytes: url: " + url);
            if (timeoutMillis >= 0) {
                connection.setConnectTimeout(timeoutMillis);
                connection.setReadTimeout(timeoutMillis);
            }

            Log.d(TAG, strThis + "postFileBytes: timeoutMillis: " + timeoutMillis);

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            Log.d(TAG, strThis + "postFileBytes: connection: " + connection);

            outputStream = new DataOutputStream(connection.getOutputStream());
            Log.d(TAG, strThis + "postFileBytes: outputStream: " + outputStream);

            outputStream.writeBytes(twoHyphens + boundary + end);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename + "\"" + end);
            outputStream.writeBytes(end);
            Log.d(TAG, strThis + "postFileBytes: buffer: " + buffer);
            if (buffer != null)
                outputStream.write(buffer, 0, buffer.length);
            outputStream.writeBytes(end);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + end);
            outputStream.flush();
            outputStream.close();
            Log.d(TAG, strThis + "postFileBytes: outputStream: " + outputStream);

            int serverResponseCode = connection.getResponseCode();
            returnInfo.responseCode = serverResponseCode;
            String strMessage = connection.getResponseMessage();
            returnInfo.responseMessage = strMessage;
            Log.d(TAG, strThis + "postFileBytes: strMessage: " + strMessage + " serverResponseCode: " + serverResponseCode);
            Log.d(TAG, strThis + "url:=" + url + ", before getting response inputstream, returnInfo:=" + returnInfo);

            // Get the inputstream no matter what in case the caller wants it
            String strResponseInputStream = null;
            InputStream inputStream = null;

            try {
                inputStream = connection.getInputStream();
                strResponseInputStream = convertStreamToStringNoClose(inputStream).trim();
                Log.d(TAG, strThis + "postFileBytes: strResponseInputStream: " + strResponseInputStream);
            } catch (FileNotFoundException e) {
                Log.d(TAG, strThis + "**** FileNotFoundException Error trying to get response stream. " + e);
                strResponseInputStream = e.toString();
                InputStream errorStream = null;
                try {
                    errorStream = connection.getErrorStream();
                    String str = convertStreamToStringNoClose(errorStream);
                    if (strResponseInputStream == null) strResponseInputStream = str;
                    else strResponseInputStream += "\n" + str;
                } finally {
                    if (errorStream != null) errorStream.close();
                }
            } finally {
                if (inputStream != null) inputStream.close();
            }

            Log.d(TAG, strThis + "postFileBytes: inputStream: " + inputStream);
            returnInfo.responseInputStream = strResponseInputStream;

//            result  = serverResponseCode >= 200 && serverResponseCode <= 299 ?
//                    convertInputStreamToString(connection.getInputStream()) : connection.getResponseMessage();
            result = serverResponseCode >= 200 && serverResponseCode <= 299 ?
                    strResponseInputStream : strMessage;
            Log.d(TAG, strThis + "postFileBytes: result: " + result);

            returnInfo.result = result;

        } catch (Exception ex) {
            Log.d(TAG, strThis + "postFileBytes: ex: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        } finally {
            Log.d(TAG, strThis + "postFileBytes: ");
            if (outputStream != null) outputStream.close();
            if (connection != null)
                connection.disconnect();
            Log.d(TAG, strThis + "End. url:=" + url + ", returnInfo:=" + returnInfo);
        }

        Log.d(TAG, strThis + "postFileBytes: ENDS returnInfo: " + returnInfo);
        return returnInfo;
    }

    /**
     * Todo: don't like methods that close streams they didn't open. -RAN
     *
     * @param inputStream
     * @return
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();

        String line = null;

        while ((line = bufferedReader.readLine()) != null)
            sb.append(line);

        inputStream.close();

        if (sb.length() > 0)
            return sb.toString();

        return null;
    }

    /**
     * Todo: don't like methods that close streams they didn't open. -RAN
     *
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String result = sb.toString().trim(); // why are we trimming here? expensive.

        // Heavy handed way to remove trailing line end. expensive.
        return result != null && result.length() > 0 && result.charAt(result.length() - 1) == '\n' ?
                result.substring(0, result.length() - 1) : result;
    }

    public static String convertStreamToStringNoClose(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            line = reader.readLine();

            if (line != null) {
                sb.append(line);

                while ((line = reader.readLine()) != null)
                    sb.append('\n').append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    //region Unsafe HTTPS call

    public static String get(String urlStr) throws IOException {
        return getWithInfo(urlStr).result;
    }

    /**
     * Todo: would like to see this reorganized a little for clarity and closing resources.
     *
     * @param urlStr
     * @return
     * @throws IOException
     */
    @SuppressLint("NewApi")
    public static HttpReturnInfo getWithInfo(String urlStr) throws IOException {
        String strThis = "getWithInfo(), ";
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//			StrictMode.setThreadPolicy(policy);
//			BusinessRules.logVerbose("StrictMode for network access check off on HONEYCOMB (3.0+) or later");
//        }

        HttpURLConnection cnn = null;
        InputStream inputStream = null;
        HttpReturnInfo returnInfo = new HttpReturnInfo();

        try {
            Log.d(TAG, strThis + "Start. urlStr=" + urlStr);

            cnn = getUrlConnection(urlStr);
            cnn.setDoInput(true);
            cnn.setDoOutput(false);
            cnn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)");

            returnInfo.responseCode = cnn.getResponseCode();
            returnInfo.responseMessage = cnn.getResponseMessage();

//            Log.d(TAG, strThis + "Before reading response inputstream, returnInfo= " + returnInfo);

            inputStream = cnn.getInputStream();
//            DataInputStream input = new DataInputStream(inputStream);

            System.gc(); // Todo: necessary? do we want it? -RAN 1/26/2021

//            int maxChunkSize = 2048;
//            byte[] data = new byte[maxChunkSize];
//            ByteArrayBuffer buffer = new ByteArrayBuffer(0);
//            int bytesRead = -1;
//
//            do {
//                bytesRead = input.read(data, 0, maxChunkSize);
//
//                if (bytesRead > 0)
//                    buffer.append(data, 0, bytesRead);
//            } while (bytesRead > 0);
            returnInfo.responseInputStream = convertStreamToStringNoClose(inputStream);
//            input.close();
            inputStream.close();
//            String result = new String(buffer.toByteArray());
            returnInfo.result = returnInfo.responseInputStream;
//            buffer.clear();
            System.gc(); // Todo: necessary? do we want it? -RAN 1/26/2021

            if (returnInfo.responseCode != 200) {
//                Log.d(TAG, "Response code: " + ResponseCode + " " + ResponseMessage);
//                Log.d(TAG, "Response body: " + result);
                Log.d(TAG, strThis + "**** Error on server. returnInfo=" + returnInfo);
            }

//            return result;
        } catch (Throwable e) {
            if (cnn != null) {
                Log.d(TAG, strThis + "**** Error with request urlStr:" + urlStr + " .", e);
                InputStream errorstream = null;
                try {
                    errorstream = cnn.getErrorStream();
                    String errorResponse = convertStreamToStringNoClose(errorstream);
                    if (returnInfo.responseInputStream == null)
                        returnInfo.responseInputStream = errorResponse;
                    else returnInfo.responseInputStream += "\n" + errorResponse;
                    Log.d(TAG, strThis + "**** Error with request urlStr:" + urlStr + " . errorResponse=" + errorResponse);

                } catch (Throwable e2) {
                    Log.d(TAG, strThis + "**** Error with request urlStr:" + urlStr + " . Error reading error stream.", e2);
                } finally {
                    try {
                        errorstream.close();
                    } catch (Exception e3) {
                    }
                    ;
                }
            }
        } finally {
            if (inputStream != null) inputStream.close();  // In case of Exception. -RAN 1/21/2021

            if (cnn != null)
                cnn.disconnect();
        }

        Log.d(TAG, strThis + "End. urlStr:" + urlStr + ", responseCode:" + returnInfo.responseCode
                + ", responseMessage:" + returnInfo.responseMessage
                + ", responseInputStream.length()=" + (returnInfo.responseInputStream != null ? returnInfo.responseInputStream.length() : "(NULL)"));

        return returnInfo;
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static HttpURLConnection getUrlConnection(String urlStr) throws IOException {
        HttpURLConnection http = null;
        URL url = new URL(urlStr);

        if (url.getProtocol().toLowerCase().compareToIgnoreCase("https") == 0) {
            trustAllHosts();

            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            https.setHostnameVerifier(DO_NOT_VERIFY);
            http = https;
        } else {
            http = (HttpURLConnection) url.openConnection();
        }

        return http;
    }

    private static void trustAllHosts() {
        X509TrustManager t = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        };

        TrustManager[] trustAllCerts = new TrustManager[]{t};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    // region Encoding

    public static String uencode(String urlParameter) throws UnsupportedEncodingException {
        return URLEncoder.encode(urlParameter, "UTF-8");
    }

    public static String blankEncode(String value) {
        if (value == null || value.length() == 0) return "+";
        else return value.replace(" ", "+");
    }

    // endregion

    //region HelperClasses

    public static class HttpReturnInfo {
        public int responseCode;
        public String responseMessage;
        public String responseInputStream;
        public String result;

        public String toString() {
            return "result=" + result + ", responseCode=" + responseCode + ", responseMessage=" + responseMessage
                    + ", responseInputStream=" + responseInputStream;
        }
    }

    //endregion
}
