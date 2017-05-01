package com.sunxiaoshan.digest.digestaccess;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class NetworkManager {

    private static String TAG = "NetworkManager";
    private static Context mContext;

    public class HTTP {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String UTF_8 = "UTF-8";
    }

    public NetworkManager(Context context) {
        mContext = context;
        // Add for ignore NetworkOnMainThreadException, just for test
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public CloudServerJson postJsonData(String uriStr, JSONObject obj) throws Exception {
        disableConnectionReuseIfNecessary();
        Object jResult = null;
        CloudServerJson gResponse = null;
        URL url = new URL(uriStr);

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (uriStr.startsWith("https://")) {
            urlConnection.setSSLSocketFactory(createTrustAllsslSocketFactory());
        }

        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty(HTTP.CONTENT_TYPE, "application/json");
        OutputStream os = urlConnection.getOutputStream();
        os.write(obj.toString().getBytes(HTTP.UTF_8));
        os.flush();
        os.close();
        urlConnection.connect();

        int statusCode = urlConnection.getResponseCode();
        Log.d(TAG, "statusCode  :" + statusCode);

        InputStream is = urlConnection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, length);
        }

        String data = new String(baos.toByteArray(), HTTP.UTF_8);
        is.close();
        baos.flush();
        baos.close();
        Log.d(TAG, "result: " + data);

        if (data.length() > 0) {
            Object jsonObj = new JSONTokener(data).nextValue();
            if (jsonObj instanceof JSONObject) {
                jResult = jsonObj;
            } else if (jsonObj instanceof JSONArray) {
                jResult = jsonObj;
            }
        }
        gResponse = new CloudServerJson();
        gResponse.setStatusCode(statusCode);
        gResponse.setJsonObj(jResult);
        urlConnection.disconnect();
        return gResponse;
    }

    public CloudServerJson getJsonData(String uriStr, int timeout) throws Exception {
        disableConnectionReuseIfNecessary();
        Object jResult = null;
        CloudServerJson gResponse = null;
        if (uriStr == null)
            return gResponse;

        URL url = new URL(uriStr);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (uriStr.startsWith("https://")) {
            urlConnection.setHostnameVerifier(new NullHostNameVerifier());
            urlConnection.setSSLSocketFactory(createTrustAllsslSocketFactory());
        }
        urlConnection.setReadTimeout(timeout);
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty(HTTP.CONTENT_TYPE, "application/json");
        urlConnection.connect();

        int statusCode = urlConnection.getResponseCode();
        Log.d(TAG, "statusCode  :" + statusCode);

        InputStream is = urlConnection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, length);
        }

        String data = new String(baos.toByteArray(), HTTP.UTF_8);
        is.close();
        baos.flush();
        baos.close();
        Log.d(TAG, "result: " + data);

        if (data.length() > 0) {
            Object jsonObj = new JSONTokener(data).nextValue();
            if (jsonObj instanceof JSONObject) {
                jResult = jsonObj;
            } else if (jsonObj instanceof JSONArray) {
                jResult = jsonObj;
            }
        }

        gResponse = new CloudServerJson();
        gResponse.setStatusCode(statusCode);
        gResponse.setJsonObj(jResult);
        urlConnection.disconnect();
        return gResponse;
    }

    public CloudServerJson getJsonDataForHeader(String uriStr, int timeout) throws Exception {
        disableConnectionReuseIfNecessary();
        Object jResult = null;
        CloudServerJson gResponse = null;

        if (uriStr == null)
            return gResponse;

        URL url = new URL(uriStr);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (uriStr.startsWith("https://")) {
            urlConnection.setSSLSocketFactory(createTrustAllsslSocketFactory());
        }
        urlConnection.setReadTimeout(timeout);
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        int statusCode = urlConnection.getResponseCode();
        Log.d(TAG, "statusCode  :" + statusCode);

        gResponse = new CloudServerJson();
        gResponse.setStatusCode(statusCode);
        gResponse.setJsonObj(jResult);
        gResponse.setHeader(urlConnection.getHeaderFields());
        urlConnection.disconnect();
        return gResponse;
    }

    public CloudServerJson getJsonDataForLogin(String uriStr, String header) {
        disableConnectionReuseIfNecessary();
        Object jResult = null;
        CloudServerJson gResponse = null;

        if (uriStr == null)
            return gResponse;

        Log.d(TAG, "getJsonDataForLogin() again uri=" + uriStr);
        try {
            URL aURL = new URL(uriStr);
            String devServer = aURL.getHost();
            if (devServer.contains("https://")) {
                devServer = devServer.replace("https://", "");
            } else if (devServer.contains("http://")) {
                devServer = devServer.replace("http://", "");
            }

            URL url = new URL(uriStr);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            if (uriStr.startsWith("https://")) {
                urlConnection.setSSLSocketFactory(createTrustAllsslSocketFactory());
            }
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("HOST", devServer);
            urlConnection.setRequestProperty("Authorization", header);
            urlConnection.connect();

            // Get response code
            int statusCode_again = urlConnection.getResponseCode();
            Log.d(TAG, "statusCode_again  :" + statusCode_again);
            gResponse = new CloudServerJson();
            gResponse.setStatusCode(statusCode_again);

            // Get JsonObject or JsonArray
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, length);
            }

            String data = new String(baos.toByteArray(), HTTP.UTF_8);
            is.close();
            baos.flush();
            baos.close();
            Log.d(TAG, "result: " + data);

            if (data != null && data.length() > 0) {
                Object jsonObj = new JSONTokener(data).nextValue();
                if (jsonObj instanceof JSONObject) {
                    jResult = (JSONObject) jsonObj;
                } else if (jsonObj instanceof JSONArray) {
                    jResult = (JSONArray) jsonObj;
                }
            }

            gResponse.setJsonObj(jResult);
            if (jResult != null) {
                if (((JSONObject) jResult).has("global_session")) {
                    // TODO : login data parse
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            gResponse.setException(e);
        } catch (IOException e) {
            e.printStackTrace();
            gResponse.setException(e);
        } catch (Exception e) {
            e.printStackTrace();
            gResponse.setException(e);
        }

        return gResponse;
    }

    private static SSLSocketFactory createTrustAllsslSocketFactory() throws Exception {
        TrustManager mTrustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext mSSLContext = SSLContext.getInstance("TLS");
        mSSLContext.init(null, new TrustManager[]{mTrustManager}, new SecureRandom());

        return mSSLContext.getSocketFactory();

    }

    private void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");

        }
    }

    public class NullHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i(TAG, "Approving certificate for " + hostname);
            return true;
        }
    }

}