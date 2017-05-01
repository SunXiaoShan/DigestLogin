package com.digest.unittest;

import android.content.Context;

import com.sunxiaoshan.digest.digestaccess.Digest;
import com.sunxiaoshan.digest.digestaccess.DigestLoginCallback;

import org.junit.Before;
import org.junit.Test;
import android.test.mock.MockContext;
import android.test.InstrumentationTestCase;

/**
 * Created by sunxiaoshan on 01/05/2017.
 */

public class DigestTest extends InstrumentationTestCase {

    private Context instrumentationCtx;

    @Before
    public void setup() {
        instrumentationCtx = new MockContext();
        assertNotNull(instrumentationCtx);
    }

    @Test
    public void testDigestLogin() {

        Digest digest = new Digest(instrumentationCtx, "server", "login_method");
        try {
            digest.digestLogin("account_id", "password", new DigestLoginCallback() {

                @Override
                public void onSuccess(String var1, String response) {

                }

                @Override
                public void onFailure(String var1, int var2) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
