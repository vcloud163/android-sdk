package com.netease.vcloudnosupload.util;

import android.util.Base64;

import com.netease.cloud.nos.android.core.CallRet;

/**
 * Created by hzwangxiaoming on 2016/12/14.
 */

public class CallResult {

    private CallRet realRet;


    public CallResult(Object var1, String var2, int var3, String var4, String var5, String var6, Exception var7) {
        realRet = new CallRet(var1, var2, var3, var4, var5, var6, var7);
    }

    public Object getFileParam() {
        return realRet.getFileParam();
    }

    public void setFileParam(Object var1) {
        realRet.setFileParam(var1);
    }

    public String getUploadContext() {
        return realRet.getUploadContext();
    }

    public void setUploadContext(String var1) {
        realRet.setUploadContext(var1);
    }

    public int getHttpCode() {
        return realRet.getHttpCode();
    }

    public void setHttpCode(int var1) {
        realRet.setHttpCode(var1);
    }

    public String getRequestId() {
        return realRet.getRequestId();
    }

    public void setRequestId(String var1) {
        realRet.setRequestId(var1);
    }

    public String getCallbackRetMsg() {
        return realRet.getCallbackRetMsg();
    }

    public void setCallbackRetMsg(String var1) {
        realRet.setCallbackRetMsg(var1);
    }

    public String getResponse() {
        return realRet.getResponse();
    }

    public void setResponse(String var1) {
        realRet.setResponse(var1);
    }

    public Exception getException() {
        return realRet.getException();
    }

    public void setException(Exception var1) {
        realRet.setException(var1);
    }

    public boolean isOK() {
        return realRet.isOK();
    }
}
