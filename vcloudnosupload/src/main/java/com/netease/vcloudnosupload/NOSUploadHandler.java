package com.netease.vcloudnosupload;

import com.netease.cloud.nos.android.core.CallRet;

import java.util.List;

/**
 * Created by hzwangxiaoming on 2016/12/14.
 */

public class NOSUploadHandler {
    /**
    *  上传初始化的回调接口
    * */
    public interface UploadInitCallback{
        /**
         * 初始化成功的回调函数
         *
         * @param nosToken          上传凭证
         * @param bucket            存储对象的桶名
         * @param object            生成的唯一对象名
         */
        void onSuccess(String nosToken, String bucket, String object);
        /**
         * 初始化失败的回调函数
         *
         * @param code          错误码
         * @param msg           错误字符串
         */
        void onFail(int code, String msg);
    }

    /**
     *  查询id的回调接口
     * */
    public interface VideoQueryCallback{
        public static class QueryResItem{
            //  object name
            public String objectName;
            //  video id
            public String vid;
            //  image id
            public String imgId;
            public String toString(){
                return "objectName: " + objectName + ", vid: " + vid + ", imgId: " + imgId;
            }
        }
        /**
         * 请求成功的回调函数
         *
         * @param list          请求返回的QueryResItem列表
         */
        void onSuccess(List<QueryResItem> list);
        /**
         * 请求失败的回调函数
         *
         * @param code          错误码
         * @param msg           错误字符串
         */
        void onFail(int code, String msg);
    }

    /**
     *  上传文件的回调接口
     */
    public interface UploadCallback {
        /**
         *  上传上下文创建后的回调函数
         *
         * @param oldUploadContext      旧的上传上下文
         * @param newUploadContext      新的上传上下文
         */
        void onUploadContextCreate(String oldUploadContext, String newUploadContext);

        /**
         *  上传过程的回调函数
         *
         * @param current       已经上传的字节数
         * @param total         总的字节数
         */
        void onProcess(long current, long total);

        /**
         *  上传成功后的回调函数
         *
         * @param ret
         */
        void onSuccess(CallRet ret);

        /**
         *  上传失败后的回调函数
         *
         * @param ret
         */
        void onFailure(CallRet ret);

        /**
         *  上传取消后的回调函数
         *
         * @param ret
         */
        void onCanceled(CallRet ret);
    }
}
