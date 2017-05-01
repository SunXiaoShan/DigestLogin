package com.sunxiaoshan.digest.digestaccess;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sunxiaoshan on 28/04/2017.
 */

public class Digest {
    private Context context = null;
    private String server = null;
    private String loginMethod = null;

    private final String TAG = "Digest";
    private static final String APP_PACKAGE_NAME = "com.Digest";
    private final NetworkManager mNetworkManager;

    public Digest(Context context, String server, String loginMethod) {
        this.context = context;
        this.server = server;
        this.loginMethod = loginMethod;
        this.mNetworkManager = new NetworkManager(context);
    }

    private String getPhoneDeviceID() {
        String deviceID = "";

        deviceID = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceID == null || deviceID.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                deviceID = Build.SERIAL;
            }
            if (deviceID == null || deviceID.equals("")) {
                deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (deviceID == null || deviceID.equals("")) {
                    deviceID = UUID.randomUUID().toString();
                }
            }
        }
        return deviceID;
    }

    private String hash(String alg, String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance(alg).digest(string.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private String genHeader(Map<String, List<String>> headerArray,
                             String userName,
                             String password,
                             String uri,
                             String udid ) {
        String header = "";
        String value = "";
        String realm = "";
        String nonce = "";
        String qop = "";

        Log.d(TAG, "header.length:" + headerArray.size());
        List<String> headerValue = headerArray.get("WWW-Authenticate");
        for (int i = 0; i < headerValue.size(); i++) {
            value = headerValue.get(i);
            realm = value.substring(value.indexOf("realm=") + 7, value.indexOf("\"", value.indexOf("realm=") + 7));
            nonce = value.substring(value.indexOf("nonce=") + 7, value.indexOf("\"", value.indexOf("nonce=") + 7));
            qop = value.substring(value.indexOf("qop=") + 5, value.indexOf("\"", value.indexOf("qop=") + 5));
        }

        String ha1 = hash("MD5", userName + ":" + realm + ":" + password);
        String ha2 = hash("MD5", "GET:" + uri);
        String ha3 = hash("MD5", ha1 + ":" + nonce + ":00000001:" + "0a4f113b:" + qop + ":" + ha2);

        header = "Digest username=" + "\"" + userName + "\"" + ", realm=" + "\"" + realm + "\"" + ", nonce=" + "\""
                + nonce + "\"" + ", uri=" + "\"" + uri + "\"" + ", qop=" + qop + ", nc=" + "00000001" + ", cnonce="
                + "\"" + "0a4f113b" + "\"" + ", response=" + "\"" + ha3 + "\"";
        return header;
    }

    public void digestLogin(String accoundId,
                            final String password,
                            final DigestLoginCallback callback) throws Exception {

        final String udid = getPhoneDeviceID();
        final String uri = this.server + this.loginMethod + "?device_id=" + udid + "&app_identifier=" +
                APP_PACKAGE_NAME;
        final String userName = accoundId.toLowerCase();

        CloudServerJson cloudServerJson = mNetworkManager.getJsonDataForHeader(uri, 10000);
        if (cloudServerJson.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            String header = genHeader(cloudServerJson.getHeader(),
                    userName.toLowerCase(Locale.ENGLISH),
                    password,
                    uri.replace(this.server, ""),
                    udid);

            CloudServerJson cloudServerJsonAgain = mNetworkManager.getJsonDataForLogin(uri, header);
            if (cloudServerJsonAgain.getStatusCode() == HttpStatus.SC_OK) {
                callback.onSuccess(accoundId, cloudServerJsonAgain.getJsonObj().toString());
            } else {
                callback.onFailure(userName, -1);
            }
        }
    }
}
