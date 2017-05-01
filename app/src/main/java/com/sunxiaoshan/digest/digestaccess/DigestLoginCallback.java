package com.sunxiaoshan.digest.digestaccess;

/**
 * Created by sunxiaoshan on 25/04/2017.
 */

public interface DigestLoginCallback {

    // TODO : for parking case => company
    DigestLoginCallback DEFAULT = new DigestLoginCallback() {
        public void onSuccess(String loginId, String response) {
        }

        public void onFailure(String loginId, int errorCode) {
        }
    };

    void onSuccess(String var1, String response);

    void onFailure(String var1, int var2);

}
