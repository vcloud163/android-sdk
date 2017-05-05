package com.netease.nosupload;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.cloud.nos.android.core.CallRet;
import com.netease.cloud.nos.android.exception.InvalidChunkSizeException;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.vcloudnosupload.AcceleratorConfig;
import com.netease.vcloudnosupload.NOSUpload;
import com.netease.vcloudnosupload.NOSUploadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

    private static final String TAG = "MainActivity";

    private EditText editTimeout, editPieceSize, editRetryTimes, editRefreshTime;
    private Button btnInit, btnHttpUpload, btnHttpsUpload, btnCancel, btnQueryID;
    private Button btnSelectFile;
    private EditText editSelectedFile;
    private TextView txtNetUrl;
    private ProgressBar progressBar;

    private AcceleratorConfig acceleratorConf = new AcceleratorConfig();

    private String mNosToken, mBucket, mObject;

    private File mFile;

    private static class HandleMsg {
        public static final int MSG_INIT_SUCCESS = 0;
        public static final int MSG_INIT_FAIL = 1;
        public static final int MSG_QUERYVIDEO_SUCCESS = 2;
        public static final int MSG_QUERYVIDEO_FAIL = 3;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HandleMsg.MSG_INIT_SUCCESS: {
                    Toast.makeText(MainActivity.this, "init success", Toast.LENGTH_SHORT).show();
                    txtNetUrl.setText("http://nos.netease.com/" + mBucket + "/" + mObject);
                    break;
                }
                case HandleMsg.MSG_INIT_FAIL: {
                    int code = msg.arg1;
                    String m = (String) msg.obj;
                    Toast.makeText(MainActivity.this, "init fail, code: " + code + ", msg: " + m, Toast.LENGTH_SHORT).show();
                    break;
                }
                case HandleMsg.MSG_QUERYVIDEO_SUCCESS: {
                    List<NOSUploadHandler.VideoQueryCallback.QueryResItem> list = (List< NOSUploadHandler.VideoQueryCallback.QueryResItem> ) msg.obj;
                    Toast.makeText(MainActivity.this, "query video success: " + list.toString(), Toast.LENGTH_SHORT).show();
                    break;
                }
                case HandleMsg.MSG_QUERYVIDEO_FAIL: {
                    int code = msg.arg1;
                    String m = (String) msg.obj;
                    Toast.makeText(MainActivity.this, "query video fail, code: " + code + ", msg: " + m, Toast.LENGTH_SHORT).show();
                    break;
                }

                default:
                    break;
            }
            return false;
        }
    });

    private NOSUpload nosUpload = NOSUpload.getInstace(MainActivity.this);
    private NOSUpload.UploadExecutor executor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButtons();
        loadDefaultAcceleratorConf();
        if (nosUpload != null) {
            /** 这里的accid,token需要用户根据文档 http://dev.netease.im/docs/product/%E9%80%9A%E7%94%A8/%E7%82%B9%E6%92%AD%E9%80%9A%E7%94%A8/%E7%A7%BB%E5%8A%A8%E7%AB%AF%E4%B8%8A%E4%BC%A0%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E
            中的/app/vod/thirdpart/user/create 接口创建 **/
            NOSUpload.Config config = new NOSUpload.Config();
            config.appKey = "55f3fcee14db4682a11e1c633739d314";
            config.accid = "test_accid_0505";
            config.token = "b99d5baf7afd461e8b1ca747f112bee80854adf2";
            nosUpload.setConfig(config);
        }
    }

    private void initButtons() {
        editTimeout = (EditText) findViewById(R.id.edit_timeout);
        editPieceSize = (EditText) findViewById(R.id.edit_piecesize);
        editRetryTimes = (EditText) findViewById(R.id.edit_retry_times);
        editRefreshTime = (EditText) findViewById(R.id.edit_refresh_time);
        (btnInit = (Button) findViewById(R.id.btn_init)).setOnClickListener(this);
        (btnHttpUpload = (Button) findViewById(R.id.btn_http_upload)).setOnClickListener(this);
        (btnHttpsUpload = (Button) findViewById(R.id.btn_https_upload)).setOnClickListener(this);
        (btnCancel = (Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);
        (btnQueryID = (Button) findViewById(R.id.btn_query_id)).setOnClickListener(this);
        (btnSelectFile = (Button) findViewById(R.id.btn_sel_file)).setOnClickListener(this);
        editSelectedFile = (EditText) findViewById(R.id.txt_selected_file);
        txtNetUrl = (TextView) findViewById(R.id.txtNetUrl);
        progressBar = (ProgressBar) findViewById(R.id.progress_upload);
        progressBar.setMax(1000);
    }

    @Override
    public void onClick(View v) {
        String connectTimeout = editTimeout.getText().toString();
        String fileSize = editPieceSize.getText().toString();
        String retry = editRetryTimes.getText().toString();
        String refresh = editRefreshTime.getText().toString();

        try {
            if (connectTimeout != null && !connectTimeout.equals(""))
                acceleratorConf.setConnectionTimeout(Integer.parseInt(connectTimeout) * 1000);
            if (refresh != null && !refresh.equals(""))
                acceleratorConf.setRefreshInterval(Integer.parseInt(refresh) * 1000);
            if (fileSize != null && !fileSize.equals(""))
                acceleratorConf.setChunkSize(Integer.parseInt(fileSize) * 1024);
            if (retry != null && !retry.equals("")) {
                acceleratorConf.setChunkRetryCount(Integer.parseInt(retry));
                acceleratorConf.setQueryRetryCount(Integer.parseInt(retry));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (InvalidChunkSizeException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        nosUpload.setAcceConfig(acceleratorConf);
        switch (v.getId()) {
            case R.id.btn_sel_file: {
                Intent intent = new Intent();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                }else {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.btn_init: {
                uploadInit();
                break;
            }
            case R.id.btn_http_upload: {
                httpUpload();
                break;
            }
            case R.id.btn_cancel: {
                cancelUpload();
                break;
            }
            case R.id.btn_query_id: {
                queryVideo();
                break;
            }
            case R.id.btn_https_upload: {
                httpsUpload();
                break;
            }
        }
    }


    private void uploadInit() {
        if(mFile == null){
            Toast.makeText(MainActivity.this, "please select file first!", Toast.LENGTH_SHORT).show();
        }
        String name = mFile.getName();

        nosUpload.fileUploadInit(name, null, -1, -1, null, null, -1, null, new NOSUploadHandler.UploadInitCallback() {
            @Override
            public void onSuccess(String nosToken, String bucket, String object) {
                mNosToken = nosToken;
                mBucket = bucket;
                mObject = object;
                Message msg = Message.obtain(mHandler, HandleMsg.MSG_INIT_SUCCESS);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFail(int code, String msg) {
                Message m = Message.obtain(mHandler, HandleMsg.MSG_INIT_FAIL);
                m.arg1 = code;
                m.obj = msg;
                mHandler.sendMessage(m);
            }
        });
    }

    private void httpUpload() {
        if(mFile == null){
            Toast.makeText(MainActivity.this, "please select file first!", Toast.LENGTH_SHORT).show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    String uploadContext = null;
                    /**
                     *  查看一个这个文件是否已经上传过，如果上传过就取出它的uploadcontext, 传给NosUpload.putFileByHttp进行断点续传
                     *  当uploadContext是null时, 就从头开始往上传
                     */
                    String tmp = nosUpload.getUploadContext(mFile);
                    if (tmp != null && !tmp.equals("")) {
                        uploadContext = tmp;
                    }
                    try {

                        executor = nosUpload.putFileByHttp(mFile,
                                    uploadContext, mBucket, mObject, mNosToken, new NOSUploadHandler.UploadCallback() {
                                    @Override
                                    public void onUploadContextCreate(
                                            String oldUploadContext,
                                            String newUploadContext) {
                                        /**
                                         *  将新的uploadcontext保存起来
                                         */
                                        nosUpload.setUploadContext(mFile, newUploadContext);
                                    }

                                    @Override
                                    public void onProcess(long current, long total) {
                                        int pro = (int)((1.0f* current / total) * progressBar.getMax());
                                        progressBar.setProgress(pro);
                                    }

                                    @Override
                                    public void onSuccess(CallRet ret) {
                                        executor = null;
                                        /**
                                         *  清除该文件对应的uploadcontext
                                         */
                                        nosUpload.setUploadContext(mFile, "");
                                        Toast.makeText(MainActivity.this, "upload success", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(CallRet ret) {
                                        executor = null;
                                        Toast.makeText(MainActivity.this, "upload fail", Toast.LENGTH_SHORT).show();
                                        progressBar.setProgress(0);
                                    }

                                    @Override
                                    public void onCanceled(CallRet ret) {
                                        executor = null;
                                        Toast.makeText(MainActivity.this, "upload cancel", Toast.LENGTH_SHORT).show();
                                        progressBar.setProgress(0);
                                    }
                                });
                        executor.join();
                    } catch (Exception ex) {
                    }

                }
            }
        }).start();
    }

    private void cancelUpload() {
        if (executor != null) {
            executor.cancel();
        }
    }

    private void queryVideo() {
        List<String> list = new ArrayList<>();
        list.add(mObject);
        nosUpload.videoQuery(list, new NOSUploadHandler.VideoQueryCallback() {
            @Override
            public void onSuccess(List<QueryResItem> list) {
                Log.e(TAG, "list: " + list.toString());
                Message msg = Message.obtain(mHandler, HandleMsg.MSG_QUERYVIDEO_SUCCESS);
                msg.obj = list;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFail(int code, String msg) {
                Log.e(TAG, "videoQuery fail: " + msg);
                Message m = Message.obtain(mHandler, HandleMsg.MSG_QUERYVIDEO_FAIL);
                m.arg1 = code;
                m.obj = msg;
                mHandler.sendMessage(m);
            }
        });
    }

    private void httpsUpload() {
        if(mFile == null){
            Toast.makeText(MainActivity.this, "please select file first!", Toast.LENGTH_SHORT).show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    String uploadContext = null;
                    /**
                     *  查看一个这个文件是否已经上传过，如果上传过就取出它的uploadcontext, 传给NosUpload.putFileByHttp进行断点续传
                     *  当uploadContext是null时, 就从头开始往上传
                     */
                    String tmp = nosUpload.getUploadContext(mFile);
                    if (tmp != null && !tmp.equals("")) {
                        uploadContext = tmp;
                    }
                    try {
                        executor = nosUpload.putFileByHttps(  mFile,
                                    uploadContext, mBucket, mObject, mNosToken, new NOSUploadHandler.UploadCallback() {
                                    @Override
                                    public void onUploadContextCreate(
                                            String oldUploadContext,
                                            String newUploadContext) {
                                        /**
                                         *  将新的uploadcontext保存起来
                                         */
                                        nosUpload.setUploadContext(mFile, newUploadContext);
                                    }

                                    @Override
                                    public void onProcess(long current, long total) {
//                                        Toast.makeText(MainActivity.this, "onProcess " + current + "/" + total, Toast.LENGTH_SHORT).show();
                                        int pro = (int)((1.0f* current / total) * progressBar.getMax());
                                        progressBar.setProgress(pro);
                                    }


                                    @Override
                                    public void onSuccess(CallRet ret) {
                                        executor = null;
                                        /**
                                         *  上传已经完成, 清除该文件对应的uploadcontext
                                         */
                                        nosUpload.setUploadContext(mFile, "");
                                        Toast.makeText(MainActivity.this, "upload success", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(CallRet ret) {
                                        executor = null;
                                        Toast.makeText(MainActivity.this, "upload fail", Toast.LENGTH_SHORT).show();
                                        progressBar.setProgress(0);
                                    }

                                    @Override
                                    public void onCanceled(CallRet ret) {
                                        executor = null;
                                        Toast.makeText(MainActivity.this, "upload cancel", Toast.LENGTH_SHORT).show();
                                        progressBar.setProgress(0);
                                    }
                                });
                        executor.join();
                    } catch (Exception ex) {
                    }

                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        String path = FileUtil.getPath(MainActivity.this, data.getData());
        mFile = new File(path);
        editSelectedFile.setText(path);
    }

    private void loadDefaultAcceleratorConf() {
        try {
            acceleratorConf.setChunkSize(1024 * 32);
            acceleratorConf.setChunkRetryCount(2);
            acceleratorConf.setConnectionTimeout(10 * 1000);
            acceleratorConf.setSoTimeout(30 * 1000);
            acceleratorConf.setLbsConnectionTimeout(10 * 1000);
            acceleratorConf.setLbsSoTimeout(10 * 1000);
            acceleratorConf.setRefreshInterval(2 * 3600);
            acceleratorConf.setMonitorInterval(120 * 1000);
            acceleratorConf.setMonitorThread(true);
        } catch (InvalidChunkSizeException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
    }
}
