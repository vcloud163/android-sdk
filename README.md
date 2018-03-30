# Android-SDK 说明

## 1 简介

Android-SDK 是用于服务器端点播上传的软件开发工具包，提供简单、便捷的方法，方便用户开发上传视频或图片文件的功能。

## 2 功能特性

- 文件上传
- 获取进度
- 断点续传
- 查询视频

## 3 开发准备

### 3.1 下载地址

[android sdk 的源码地址](https://g.hz.netease.com/vcloud-sdk/VCloudUploadSDK_Android.git "android sdk 的源码地址")


### 3.2 环境准备

- 通过管理控制台->账户信息获取 AppKey ，Accid ，Token。
- 如果安装了 git 命令行，执行 `git clone ssh://git@g.hz.netease.com:22222/vcloud-sdk/VCloudUploadSDK_Android.git` 或者直接在 github 下载 zip 包
- 参照 API 说明和 sdk 中提供的 demo，开发代码。


### 3.3 https 支持

支持http和https的上传，分别对应putFileByHttp和putFileByHttps；
如果使用https上传失败，内部会退化为http上传，无需外部干涉，对用户透明；

## 4 使用说明

### 4.1 初始化

接入视频云点播，需要拥有一对有效的 AppKey 和 Accid ，Token 进行签名认证，可通过如下步骤获得：
开通视频云点播服务；
登陆视频云开发者平台，通过管理控制台->账户信息获取 AppKey ，Accid ，Token。
在获取到 AppKey 和 Accid ，Token 之后，可按照如下方式进行初始化：

    /** 这里的accid,token需要用户根据文档 http://dev.netease.im/docs/product/%E7%82%B9%E6%92%AD/%E7%A7%BB%E5%8A%A8%E7%AB%AF%E4%B8%8A%E4%BC%A0%E6%94%AF%E6%8C%81%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E
    中的/app/vod/thirdpart/user/create 接口创建 **/

	NosUpload nosUpload = NosUpload.getInstance(context);
	NOSUpload.Config config = new NOSUpload.Config();
	config.appKey = "...";
	config.accid = "...";
	config.token = "...";
	nosUpload.setConfig(config);

### 4.2 文件上传

视频云点播在全国各地覆盖大量上传节点，会选择适合用户的最优节点进行文件上传，并根据用户传入的参数做不同处理，具体详见点播服务端 API 文档。

以下是使用示例：

		String uploadContext = nosUpload.getUploadContext(mFile);
		NOSUpload.UploadExecutor executor = nosUpload.putFileByHttp(
				mFile,				//	文件
				uploadContext, 		//	用于断点续传的上传上下文
				mBucket,			//	上传到云存储的bucket
				mObject, 			//	上传到云存储的object name
				mNosToken, 			//	上传需要的token, 由uploadInit返回
				
				new NOSUploadHandler.UploadCallback() {
					@Override
					public void onUploadContextCreate(
							String oldUploadContext,
							String newUploadContext) {
						/**
						 *  将新的uploadcontext保存起来
						 */
						nosUpload.setUploadContext(mFile, newUploadContext);
					}

					//	上传进度的回调函数
					@Override
					public void onProcess(long current, long total) { 
					}

					@Override
					public void onSuccess(CallResult ret) {
						executor = null;
						/**
						 *  上传成功后，清除该文件对应的uploadcontext
						 */
						nosUpload.setUploadContext(mFile, ""); 
					}

					@Override
					public void onFailure(CallResult ret) { 
					}

					@Override
					public void onCanceled(CallResult ret) {
					
					}
			});

**注：具体使用示例详见 sdk 包中 MainActivity.java 文件。**

### 4.3 查询进度

视频云点播文件上传采用分片处理，可通过以下方法查询上传完成的文件进度。SDK 提供回调函数接收文件上传进度。

以下是使用示例：
	
	nosUpload.putFileByHttp(
			mFile,				//	文件
			uploadContext, 		//	用于断点续传的上传上下文
			mBucket,			//	上传到云存储的bucket
			mObject, 			//	上传到云存储的object name
			mNosToken, 			//	上传需要的token
			
			new NOSUploadHandler.UploadCallback() { 
				//	上传进度的回调函数
				@Override
				public void onProcess(long current, long total) { 
				} 
			}

**注：具体使用示例详见 sdk 包中 MainActivity.java 文件。**

### 4.4 断点续传

在上传文件中，视频云点播通过唯一标识 uploadContext 标识正在上传的文件，可通过此标识获取到已经上传视频云的文件字节数。通过此方法可实现文件的断点续传。

为防止服务中止造成文件上传信息丢失，可通过在本地存储文件信息来记录断点信息，当服务重新启动，可根据文件继续上传文件。临时文件会在上传完成后删除记录。

使用示例如 4.2 所示。

### 4.5 查询视频

视频上传成功后，可通过主动查询的方式获取到视频唯一标识，支持批量查询。

以下是使用示例：

		nosUpload.videoQuery(list, new NOSUploadHandler.VideoQueryCallback() {
		
				//	请求返回的QueryResItem列表
				@Override
				public void onSuccess(List<QueryResItem> list) {
				}
				//	请求失败的回调函数, 返回错误码和错误字符串
				@Override
				public void onFail(int code, String msg) {
				
				}
			});

**注：具体使用示例详见 sdk 包中 MainActivity.java 文件。**



## 5 版本更新记录

**v1.0.0**

Android SDK 的初始版本，提供点播上传的基本功能。包括：文件上传、获取进度、断点续传、查询视频。
