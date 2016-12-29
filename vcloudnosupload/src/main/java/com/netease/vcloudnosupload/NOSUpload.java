package com.netease.vcloudnosupload;

import android.content.Context;

import com.netease.cloud.nos.android.core.UploadTaskExecutor;
import com.netease.cloud.nos.android.exception.InvalidParameterException;

import java.io.File;
import java.util.List;

/**
 * Created by hzwangxiaoming on 2016/12/12.
 */

public abstract class NOSUpload {

    public static class Config{
        //  开发者平台分配的AppKey
        public String appKey;
        //  服务器认证需要，视频云用户创建的其子用户id
        public String accid;
        //  视频云用户子用户的token
        public String token;
    }
    /**
     * 执行器
     */
    public static class UploadExecutor{
        private UploadTaskExecutor realExecutor;

        /**
        *  这个接口不对外
        * */
        public void setRealExecutor(UploadTaskExecutor executor){
            realExecutor = executor;
        }
        /**
        *  等待上传结束
        * */
        public void join(){
            if(realExecutor != null){
                realExecutor.get();
            }
        }
        /**
         *  取消上传
         * */
        public void cancel(){
            if(realExecutor != null){
                realExecutor.cancel();
            }
        }
    }

    /**
     *  视频云请求单例
     *
     *  @return
     */
    public static NOSUpload getInstace(Context ctx){
        if(nosUpload == null){
            synchronized (NOSUpload.class){
                if(nosUpload == null){
                    nosUpload = new NOSUploadImpl(ctx);
                }
            }
        }
        return nosUpload;
    }
    /**
     *  设置config参数
     *
     *  @return
     */
    public abstract void setConfig(Config conf);

    /**
     *  设置文件的上传上下文(用于断点续传)
     *
     * @param file              文件
     * @param uploadContext     上传上下文
     */
    public abstract void setUploadContext(File file, String uploadContext);

    /**
     *  获取文件的上传上下文
     *
     * @param file      文件
     * @return
     */
    public abstract String getUploadContext(File file);

    /**
     * 设置加速相关的config
     */
    public abstract void setAcceConfig(AcceleratorConfig config);

    /**
     *  文件上传初始化接口:用于获取xNosToken（上传凭证）、bucket（存储对象的桶名）、object（生成的唯一对象名）
     *
     *  @param originFileName    上传文件的原始名称（包含后缀名）
     *  @param userFileName      用户命名的上传文件名称
     *  @param typeId            视频所属的类别Id（不填写为默认分类）
     *  @param presetId          视频所需转码模板Id（不填写为默认模板，默认模板不进行转码）
     *  @param uploadCallbackUrl 上传成功后回调客户端的URL地址（需标准http格式）
     *  @param description       上传视频的描述信息
     *  @param watermarkId       视频水印Id（不填写为不添加水印，如果选择，
                                     请务必在水印管理中提前完成水印图片的上传和参数的配置；
                                     且必需设置prestId字段，且presetId字段不为默认模板）
     *  @param userDefInfo       用户自定义信息，回调会返回此信息
     *  @param cb                初始化完成后的回调接口
     */
    public abstract void fileUploadInit(String originFileName,
                               String userFileName,
                               int typeId,
                               int presetId,
                               String uploadCallbackUrl,
                               String description,
                               int watermarkId,
                               String userDefInfo, NOSUploadHandler.UploadInitCallback cb);

    /**
     *    用http上传文件
     *
     *    @param file              文件
     *    @param uploadContext     用于断点续传的上传上下文
     *    @param bucket            上传到云存储的bucket
     *    @param object            上传到云存储的object name
     *    @param token             上传需要的token, 由服务器生成
     *    @param callback          上传过程的回调接口
     *
     *    @return 执行器实例
     */
    public abstract UploadExecutor putFileByHttp( File file,
                  String uploadContext, String bucket, String object, String token,
                  NOSUploadHandler.UploadCallback callback) throws InvalidParameterException;
    /**
     *    用https上传文件
     *
     *    @param file              文件
     *    @param uploadContext     用于断点续传的上传上下文
     *    @param bucket            上传到云存储的bucket
     *    @param object            上传到云存储的object name
     *    @param token             上传需要的token, 由服务器生成
     *    @param callback          上传过程的回调接口
     *
     *    @return 执行器实例
     */
    public abstract UploadExecutor putFileByHttps(  File file,
                                                    String uploadContext, String bucket, String object, String token,
                                                    NOSUploadHandler.UploadCallback callback) throws InvalidParameterException;
    /**
     *  上传完成根据对象名查询视频或水印图片主Id
     *
     *  @param list 上传文件的对象名列表
     *  @param cb   查询完成后的回调接口
     */
    public abstract void videoQuery(List<String> list, NOSUploadHandler.VideoQueryCallback cb);

    private volatile static NOSUpload nosUpload = null;
}
