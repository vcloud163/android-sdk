package com.netease.vcloudnosupload;

import com.netease.cloud.nos.android.core.AcceleratorConf;
import com.netease.cloud.nos.android.exception.InvalidChunkSizeException;
import com.netease.cloud.nos.android.exception.InvalidParameterException;

/**
 * Created by hzwangxiaoming on 2016/12/14.
 */

public class AcceleratorConfig {

    private AcceleratorConf mConf = new AcceleratorConf();

    public String getLbsHost() {
        return mConf.getLbsHost();
    }

    public void setLbsHost(String var1) {
        mConf.setLbsHost(var1);
    }

    public String getLbsIP() {
        return mConf.getLbsIP();
    }

    public void setLbsIP(String var1) throws InvalidParameterException { mConf.setLbsIP(var1);
    }

    public String getMonitorHost() {
        return mConf.getMonitorHost();
    }

    public void setMontiroHost(String var1) {
        mConf.setMontiroHost(var1);
    }

    public String getCharset() {
        return mConf.getCharset();
    }

    public int getConnectionTimeout() {
        return mConf.getConnectionTimeout();
    }

    /**
     * 设置文件上传socket连接超时，默认为10s
     * 如果设置的值小于或等于0，将抛异常InvalidParameterException
     *
     * @param timeout
     * @throws InvalidParameterException
     */
    public void setConnectionTimeout(int timeout) throws InvalidParameterException {
        mConf.setConnectionTimeout(timeout);
    }

    public int getSoTimeout() {
        return mConf.getSoTimeout();
    }

    /**
     * 设置文件上传socket读写超时，默认30s
     * 如果设置的值小于或等于0，将抛异常InvalidParameterException
     * @param timeout
     * @throws InvalidParameterException
     */
    public void setSoTimeout(int timeout) throws InvalidParameterException {
        mConf.setSoTimeout(timeout);
    }

    public int getLbsConnectionTimeout() {
        return mConf.getLbsConnectionTimeout();
    }


    /**
     * 设置LBS查询socket连接超时，默认为10s
     * 如果设置的值小于或等于0，将抛异常InvalidParameterException
     *
     * @param timeout
     * @throws InvalidParameterException
     */
    public void setLbsConnectionTimeout(int timeout) throws InvalidParameterException {
        mConf.setLbsConnectionTimeout(timeout);
    }

    public int getLbsSoTimeout() {
        return mConf.getLbsSoTimeout();
    }

    /**
     * 设置LBS查询socket读写超时，默认10s
     * 如果设置的值小于或等于0，将抛异常InvalidParameterException
     *
     * @param timeout
     * @throws InvalidParameterException
     */
    public void setLbsSoTimeout(int timeout) throws InvalidParameterException {
        mConf.setLbsSoTimeout(timeout);
    }

    public int getChunkSize() {
        return mConf.getChunkSize();
    }

    /**
     * SDK会根据网络类型自动调整上传分块大小，如果网络类型无法识别，将采用设置的上传分块大小
     * 默认32K，如果网络环境较差，可以设置更小的分块
     * chunkSize的取值范围为：[4K, 4M]，不在范围内将抛异常InvalidChunkSizeException
     *
     * @param chunkSize
     * @throws InvalidChunkSizeException
     */
    public void setChunkSize(int chunkSize) throws InvalidChunkSizeException {
        mConf.setChunkSize(chunkSize);
    }

    public int getChunkRetryCount() {
        return mConf.getChunkRetryCount();
    }

    /**
     * 设置分块上传失败时的重试次数，默认2次
     * 如果设置的值小于或等于0，将抛异常InvalidParameterException
     *
     * @param count
     * @throws InvalidParameterException
     */
    public void setChunkRetryCount(int count) throws InvalidParameterException {
        mConf.setChunkRetryCount(count);
    }

    public int getQueryRetryCount() {
        return mConf.getQueryRetryCount();
    }

    public void setQueryRetryCount(int var1) throws InvalidParameterException {
        mConf.setQueryRetryCount(var1);
    }

    public long getRefreshInterval() {
        return mConf.getRefreshInterval();
    }

    /**
     * 设置刷新上传边缘节点的时间间隔，默认2小时
     * 合法值为大于或等于60s，设置非法将采用默认值
     * 注：当发生网络切换，Android-SDK会在下次上传文件时做一次接入点刷新
     *
     * @param interval
     */
    public void setRefreshInterval(long interval) {
        mConf.setRefreshInterval(interval);
    }

    public long getMonitorInterval() {
        return mConf.getMonitorInterval();
    }

    /**
     * 设置统计监控程序统计发送间隔，默认120s
     * 合法值为大于或等于60s，设置非法将采用默认值
     *
     * @param interval
     */
    public void setMonitorInterval(long interval) {
        mConf.setMonitorInterval(interval);
    }

    public int getMd5FileMaxSize() {
        return mConf.getMd5FileMaxSize();
    }

    public void setMd5FileMaxSize(int var1) throws InvalidParameterException {
        mConf.setMd5FileMaxSize(var1);
    }

    /**
     *.设置是否用线程进行统计信息上传，默认值为false
     * true：创建线程进行统计信息上传
     * false：使用service进行统计信息上传
     *
     * @param b
     */
    public void setMonitorThread(boolean b) {
        mConf.setMonitorThread(b);
    }

    public boolean isMonitorThreadEnabled() {
        return mConf.isMonitorThreadEnabled();
    }
}
