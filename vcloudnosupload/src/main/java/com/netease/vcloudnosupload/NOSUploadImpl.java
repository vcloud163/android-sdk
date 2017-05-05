package com.netease.vcloudnosupload;
/**
 * Created by hzwangxiaoming on 2016/12/12.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.core.AcceleratorConf;
import com.netease.cloud.nos.android.core.CallRet;
import com.netease.cloud.nos.android.core.Callback;
import com.netease.cloud.nos.android.core.UploadTaskExecutor;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.core.WanNOSObject;
import com.netease.cloud.nos.android.exception.InvalidChunkSizeException;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.vcloudnosupload.http.HTTPResult;
import com.netease.vcloudnosupload.http.NOSHTTPPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by hzwangxiaoming on 2016/12/12.
 */
public class NOSUploadImpl extends NOSUpload {
    private static final String TAG = "NOSUploadImpl";
    private String APPKEY, ACCID, TOKEN;
    private Context mCtx;

    private final String hostName = "http://vcloud.163.com";
    private final String uploadInit = "/app/vod/upload/init";
    private final String videoQuery = "/app/vod/video/query";

    public NOSUploadImpl(Context ctx){
        mCtx = ctx;
    }

    public void setConfig(Config conf) {
        APPKEY = conf.appKey;
        ACCID = conf.accid;
        TOKEN = conf.token;
    }

    public void setUploadContext(File file, String uploadContext){
        if(mCtx != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString(file.getAbsolutePath(), uploadContext);
            edit.commit();
        }
    }

    public String getUploadContext(File file){
        if(mCtx != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
            return preferences.getString(file.getAbsolutePath(), (String)null);
        }
        return null;
    }

    public void setAcceConfig(AcceleratorConfig config){
        AcceleratorConf conf = new AcceleratorConf();

        try {
            conf.setMonitorThread(config.isMonitorThreadEnabled());
            conf.setMonitorInterval(config.getMonitorInterval());
            conf.setMd5FileMaxSize(config.getMd5FileMaxSize());
            conf.setRefreshInterval(config.getRefreshInterval());
            conf.setChunkRetryCount(config.getChunkRetryCount());
            conf.setChunkSize(config.getChunkSize());
            conf.setConnectionTimeout(config.getConnectionTimeout());
            conf.setLbsConnectionTimeout(config.getLbsConnectionTimeout());
            conf.setLbsHost(config.getLbsHost());
            conf.setLbsIP(config.getLbsIP());
            conf.setLbsSoTimeout(config.getLbsSoTimeout());
            conf.setMontiroHost(config.getMonitorHost());
            conf.setQueryRetryCount(config.getQueryRetryCount());
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (InvalidChunkSizeException e) {
            e.printStackTrace();
        }

        WanAccelerator.setConf(conf);
    }

    @Override
    public void fileUploadInit(String originFileName, String userFileName, int typeId, int presetId,
                               String uploadCallbackUrl, String description, int watermarkId,
                               String userDefInfo, final NOSUploadHandler.UploadInitCallback cb) {
        NOSHTTPPost noshttpPost = new NOSHTTPPost();
        noshttpPost.setUrl(hostName + uploadInit);
        Map<String, String> header = new HashMap<String, String>();
        header.put("AppKey", APPKEY);
        header.put("Accid", ACCID);
        header.put("Token", TOKEN);
        header.put("Content-Type", "application/json");

        noshttpPost.setHeader(header);

        noshttpPost.setCallback(new NOSHTTPPost.PostCallback() {
            @Override
            public void onResponse(HTTPResult result) {
                Log.d(TAG, "msg: " + result.getMsg());
                if(result.getStatusCode() == HttpsURLConnection.HTTP_OK){
                    JSONObject resObj = null;
                    try {
                        resObj = new JSONObject(result.getMsg());
                        int code = resObj.getInt("code");
                        JSONObject retObj = null;
                        if(resObj.has("ret")){
                            retObj = resObj.getJSONObject("ret");
                        }
                        if(code == Code.HTTP_SUCCESS){
                            if(retObj != null){
                                String nosToken = retObj.getString("xNosToken");
                                String bucket = retObj.getString("bucket");
                                String object = retObj.getString("object");
                                if(cb != null){
                                    cb.onSuccess(nosToken, bucket, object);
                                }
                            }
                        }else{
                            if(resObj != null){
                                String msg = resObj.getString("msg");
                                if(cb != null){
                                    cb.onFail(code, msg);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    if(cb != null){
                        cb.onFail(result.getStatusCode(), null);
                    }
                }
            }
        });

        JSONObject jsonObject = new JSONObject();
        try {
            if(originFileName != null){
                jsonObject.put("originFileName", originFileName);
            }
            if(userFileName != null){
                jsonObject.put("userFileName", userFileName);
            }
            if(typeId != -1){
                jsonObject.put("typeId", typeId);
            }
            if(presetId != -1){
                jsonObject.put("presetId", presetId);
            }
            if(uploadCallbackUrl != null){
                jsonObject.put("uploadCallbackUrl", uploadCallbackUrl);
            }
            if(description != null){
                jsonObject.put("description", description);
            }
            if(watermarkId != -1){
                jsonObject.put("watermarkId", watermarkId);
            }
            if(userDefInfo != null){
                jsonObject.put("userDefInfo", userDefInfo);
            }
            noshttpPost.setPostEntity(jsonObject.toString().getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCachedThreadPool.execute(noshttpPost);
    }

    @Override
    public UploadExecutor putFileByHttp(    File file,
                                            String uploadContext, String bucket, String object, String token,
                                            final NOSUploadHandler.UploadCallback callback) throws InvalidParameterException {
        WanNOSObject obj = new WanNOSObject();
        obj.setNosBucketName(bucket);
        obj.setUploadToken(token);
        obj.setNosObjectName(object);
        UploadTaskExecutor realExecutor = WanAccelerator.putFileByHttp(mCtx, file, file.getAbsoluteFile(), uploadContext, obj, new Callback() {
            @Override
            public void onUploadContextCreate(Object o, String s, String s1) {
                if(callback != null)
                    callback.onUploadContextCreate(s, s1);
            }

            @Override
            public void onProcess(Object o, long l, long l1) {
                if(callback != null)
                    callback.onProcess(l, l1);
            }

            @Override
            public void onSuccess(CallRet callRet) {
                if(callback != null){
                    callback.onSuccess(callRet);
                }
            }

            @Override
            public void onFailure(CallRet callRet) {
                if(callback != null){
                    callback.onFailure(callRet);
                }
            }

            @Override
            public void onCanceled(CallRet callRet) {
                if(callback != null){
                    callback.onCanceled(callRet);
                }
            }
        });
        NOSUpload.UploadExecutor executor = new NOSUpload.UploadExecutor();
        executor.setRealExecutor(realExecutor);
        return executor;
    }

    @Override
    public UploadExecutor putFileByHttps(    File file,
                                             String uploadContext, String bucket, String object, String token,
                                             final NOSUploadHandler.UploadCallback callback) throws InvalidParameterException {
        WanNOSObject obj = new WanNOSObject();
        obj.setNosBucketName(bucket);
        obj.setUploadToken(token);
        obj.setNosObjectName(object);
        UploadTaskExecutor realExecutor = WanAccelerator.putFileByHttps(mCtx, file, file.getAbsoluteFile(), uploadContext, obj, new Callback() {
            @Override
            public void onUploadContextCreate(Object o, String s, String s1) {
                if(callback != null)
                    callback.onUploadContextCreate(s, s1);
            }

            @Override
            public void onProcess(Object o, long l, long l1) {
                if(callback != null)
                    callback.onProcess(l, l1);
            }

            @Override
            public void onSuccess(CallRet callRet) {
                if(callback != null){
                    callback.onSuccess(callRet);
                }
            }

            @Override
            public void onFailure(CallRet callRet) {
                if(callback != null){
                    callback.onFailure(callRet);
                }
            }

            @Override
            public void onCanceled(CallRet callRet) {
                if(callback != null){
                    callback.onCanceled(callRet);
                }
            }
        });
        NOSUpload.UploadExecutor executor = new NOSUpload.UploadExecutor();
        executor.setRealExecutor(realExecutor);
        return executor;
    }

    @Override
    public void videoQuery(List<String> list, final NOSUploadHandler.VideoQueryCallback cb) {
        NOSHTTPPost noshttpPost = new NOSHTTPPost();
        noshttpPost.setUrl(hostName + videoQuery);
        Map<String, String> header = new HashMap<String, String>();
        header.put("AppKey", APPKEY);
        header.put("Accid", ACCID);
        header.put("Token", TOKEN);
        header.put("Content-Type", "application/json");
        noshttpPost.setHeader(header);
        JSONArray jsonArray = new JSONArray(list);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("objectNames", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        noshttpPost.setPostEntity(jsonObject.toString().getBytes());
        noshttpPost.setCallback(new NOSHTTPPost.PostCallback() {
            @Override
            public void onResponse(HTTPResult result) {
                System.out.println("msg: " + result.getMsg());
                JSONObject resObj = null;
                if(result.getStatusCode() == HttpsURLConnection.HTTP_OK){
                    try {
                        resObj = new JSONObject(result.getMsg());
                        int code = resObj.getInt("code");
                        JSONObject retObj = null;
                        if(resObj.has("ret")){
                            retObj = resObj.getJSONObject("ret");
                        }
                        if(code == Code.HTTP_SUCCESS){
                            if(retObj != null){
                                if(cb != null){
                                    List<NOSUploadHandler.VideoQueryCallback.QueryResItem> list = new ArrayList<NOSUploadHandler.VideoQueryCallback.QueryResItem>();
                                    JSONArray listArray = retObj.getJSONArray("list");

                                    for(int i = 0; i< listArray.length(); i++){
                                        NOSUploadHandler.VideoQueryCallback.QueryResItem item = new NOSUploadHandler.VideoQueryCallback.QueryResItem();
                                        JSONObject obj = (JSONObject)listArray.opt(i);
                                        if(obj != null){
                                            if(obj.has("objectName")) item.objectName = obj.optString("objectName");
                                            if(obj.has("imgId")) item.imgId = obj.optString("imgId");
                                            if(obj.has("vid")) item.vid = obj.optString("vid");
                                            list.add(item);
                                        }
                                    }
                                    cb.onSuccess(list);
                                }
                            }
                        }else{
                            if(resObj != null){
                                String msg = resObj.getString("msg");
                                if(cb != null){
                                    cb.onFail(code, msg);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    if(resObj != null){
                        if(cb != null){
                            cb.onFail(result.getStatusCode(), null);
                        }
                    }
                }
            }
        });
        mCachedThreadPool.execute(noshttpPost);
    }

    ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();
}
