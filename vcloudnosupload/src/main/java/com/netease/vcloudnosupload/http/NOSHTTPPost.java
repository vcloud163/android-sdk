package com.netease.vcloudnosupload.http;
import com.netease.vcloudnosupload.http.HTTPResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by hzwangxiaoming on 2016/12/9.
 */

public class NOSHTTPPost implements Runnable {

    public interface PostCallback{
        void onResponse(HTTPResult result);
    }
    private Map<String, String> mHeader;
    private byte[] mPostEntity;
    private PostCallback mCallback;
    private String mUrl;

    public void setHeader(Map<String, String> header){
        mHeader = header;
    }

    public void setPostEntity(byte[] entity){
        mPostEntity = entity;
    }

    public void setCallback(PostCallback callback){
    	synchronized(NOSHTTPPost.class){
    		mCallback = callback;
    	}
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public void run() {
        try {
            URL url = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");

            for(Map.Entry<String, String> entry : mHeader.entrySet()){
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

            DataOutputStream ds =
                    new DataOutputStream(con.getOutputStream());

            ds.write(mPostEntity);

            ds.flush();
            ds.close();
            int code = con.getResponseCode();
            StringBuffer sb=new StringBuffer();
            String readLine;
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
            while((readLine = responseReader.readLine()) != null){
                sb.append(readLine).append("\n");
            }
            responseReader.close();

            HTTPResult result = new HTTPResult(code, sb.toString(), null);
            synchronized (NOSHTTPPost.class) {
            	if(mCallback != null){
                    mCallback.onResponse(result);
                }	
			}
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
