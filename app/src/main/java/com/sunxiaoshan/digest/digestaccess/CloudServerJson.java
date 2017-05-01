package com.sunxiaoshan.digest.digestaccess;

import java.util.List;
import java.util.Map;

public class CloudServerJson {

    private Object mObj;
    private int mStatusCode;
    private Map<String, List<String>> mHeader;
    private Exception mException;

    public Object getJsonObj() {
        return mObj;
    }

    public void setJsonObj(Object mObj) {
        this.mObj = mObj;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public void setStatusCode(int mStatusCode) {
        this.mStatusCode = mStatusCode;
    }

    public Map<String, List<String>> getHeader() {
        return mHeader;
    }

    public void setHeader(Map<String, List<String>> mHeader) {
        this.mHeader = mHeader;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception ex) {
        this.mException = ex;
    }
}
