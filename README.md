## Android直播带货演示项目

### 1 环境要求

<table class="relative-table wrapped" style="width: 60%;"><colgroup></colgroup>
<tbody>
<tr>
<th>名称</th>
<th>要求</th></tr>
<tr>
<td>minSdkVersion</td>
<td>>=21 (Android 5.0)</td></tr>
<tr>
<td>targetSdkVersion</td>
<td>27 (Android 8.1)</td></tr>
<tr>
<td>abiFilters</td>
<td>arm64-v8a、armeabi-v7a、x86</td></tr>
<tr>
<td>jdk version</td>
<td>1.7.0</td></tr>
<tr>
<td>集成工具</td>
<td>Android Studio</td></tr></tbody></table>

**注1：开发者如需兼容minSdkVersion >= 16 (Android 4.1设备)，请按[OkHttp4.x版本降级到3.x兼容方案](https://github.com/polyv/polyv-android-cloudClass-sdk-demo/wiki/OkHttp4.x%E7%89%88%E6%9C%AC%E9%99%8D%E7%BA%A7%E5%88%B03.x%E5%85%BC%E5%AE%B9%E6%96%B9%E6%A1%88)处理。**

**注2：如果要兼容targetSdkVersion至28，需要在AndroidManifest.xml文件中添加android:usesCleartextTraffic="true"。**

**注3：so库的配置只能使用[arm64-v8a、armeabi-v7a、x86]的一种或几种，否则可能在部分机型上出现崩溃或者音画不同步、跳秒播放等情况。**

### 2 目录结构

<table class="relative-table wrapped" style="width: 60%;"><colgroup></colgroup>
<tbody>
<tr>
<th>模块名</th>
<th>作用</th></tr>
<tr>
<td>demo</td>
<td>sdk初始化、登录功能演示</td></tr>
<tr>
<td>polyvLiveEcomerceScene</td>
<td>播放、聊天、打赏、购物等功能演示</td></tr>
<tr>
<td>polyvLiveCommonModul</td>
<td>播放、聊天契约协议，直播配置类，工具类等</td></tr>
</tbody></table>

### 3 功能模块集成

#### 3.1 导入模块

拷贝polyv项目中的polyvLiveCommonModul模块、polyvLiveEcommerceScene模块至您项目的根目录下，打开您项目的`settings.gradle`文件，添加如下代码：

	include ':polyvLiveCommonModul', ':polyvLiveEcommerceScene'

#### 3.2 配置模块编译版本

打开您项目的`build.gradle`文件，添加如下代码：

	ext {
    	compileSdkVersion = 27
    	minSdkVersion = 21
    	targetSdkVersion = 27
	}

#### 3.3 配置maven地址

打开您项目的`build.gradle`文件，添加如下代码：

	maven { url "https://jitpack.io" }
    maven { url 'http://maven.aliyun.com/nexus/content/repositories/releases/' }
    maven { url 'https://dl.bintray.com/polyv/android' }

#### 3.4 调整sdkVersion

打开您项目的`app/build.gradle`文件(app/..皆指您项目的application模块)，把里面的`minSdkVersion`设置为21或以上，把`targetSdkVersion`设置为27或以下。

如果要兼容`minSdkVersion`为16，可以参考[OkHttp4.x版本降级到3.x兼容方案](https://github.com/polyv/polyv-android-cloudClass-sdk-demo/wiki/OkHttp4.x%E7%89%88%E6%9C%AC%E9%99%8D%E7%BA%A7%E5%88%B03.x%E5%85%BC%E5%AE%B9%E6%96%B9%E6%A1%88)处理。

如果要兼容`targetSdkVersion`至28，需要在`AndroidManifest.xml`文件中添加android:usesCleartextTraffic="true"。

#### 3.5 配置abiFilters

打开您项目的`app/build.gradle`文件，添加如下代码：

	//依赖的包可能支持很多类型的ABI
    //为了避免打包了我们没有支持的ABI，指定需要打包的ABI目录
    ndk {
        abiFilters 'arm64-v8a', "armeabi-v7a", "x86" // DO NOT MODIFY THIS LINE, IT'S UPDATED BY BUILD MACHINE AUTOMATICALLY.
    }

如果您项目原来就有该配置，并且`abiFilters`配置的范围在上述的范围内，则不用更改。

#### 3.6 配置RenderScript

RenderScript是在polyv项目demo中高斯模糊背景使用的，demo中为了兼容到api 16，使用到了`support`包中的RenderScript。如果您项目的`minSdkVersion`为16，并且需要使用高斯模糊功能，则需要在`app/build.gradle`文件中添加如下配置：

	renderscriptSupportModeEnabled true

如果您项目的`minSdkVersion`大于16，或者不需要使用polyv项目demo中的高斯模糊功能，则需要做以下的操作：

	1.移除polyvLiveCommonModul模块中build.gradle文件的renderscriptSupportModeEnabled true配置
	2.移除polyvLiveCommonModul模块中SupportRenderScriptBlur类
	3.把polyvLiveCommonModul模块中PLVBlurUtils类中的SupportRenderScriptBlur修改为RenderScriptBlur

#### 3.7 配置方法数超过 64K 的应用
如果您项目的`minSdkVersion`设为21或更高的值，则默认情况下会启用 MultiDex，并且您不需要MultiDex支持库。

否则可以参考[配置方法数超过 64K 的应用](https://developer.android.google.cn/studio/build/multidex?hl=cn)里的方式添加MultiDex。

### 4 初始化sdk

打开您项目的`Application.java`这个文件(在`AndroidManifest.xml`里配置了application name的类)，在`onCreate`方法里添加如下代码：

	PLVLiveSDKConfig.init(
            new PLVLiveSDKConfig.Parameter(this)
                    .isOpenDebugLog(true)
                    .isEnableHttpDns(false)
    );

### 5 登录验证&进入观看页

进入直播/回放观看页时，需要先进行登录校验。登录验证的作用是校验参数是否正确，以及获取直播/回放的场景类型，和聊天室的私有域名设置。可以使用`PLVSceneLoginManager`进行登录验证，示例代码：

	//直播登录验证
    loginManager.loginLive(userId, appSecret, channelId, appId, new IPLVNetRequestListener<PLVLiveLoginResult>() {
	    @Override
	    public void onSuccess(PLVLiveLoginResult polyvLiveLoginResult) {
	        if (!polyvLiveLoginResult.isNormal()) {
				//直播带货场景需要使用非三分屏的直播频道登录
	            PLVToastUtils.showShort("直播带货场景不支持三分屏类型");
	            return;
	        }
			//进入直播间
	        PLVECLiveActivity.launchLive(PLVLoginActivity.this, channelId, viewerId, viewerName);
	    }

	    @Override
	    public void onFailed(String msg, Throwable throwable) {
	        //登录失败
	    }
	});

### 6 功能模块简介

#### 6.1 播放器

直播播放器的功能使用位于`PLVECLiveVideoLayout`类中，在该类中示例了直播播放器的使用方式。

`PLVLivePlayerPresenter`是直播播放器的presenter，负责播放器的控制以及通知`IPLVLivePlayerContract.IPLVLivePlayerView`更新播放器的UI状态。

回放播放器的功能使用位于`PLVECPlaybackVideoLayout`类中，在该类中示例了回放播放器的使用方式。

`PLVPlaybackPlayerPresenter`是直播播放器的presenter，负责播放器的控制以及通知`IPLVPlaybackPlayerContract.IPLVPlaybackPlayerView`更新播放器的UI状态。

#### 6.2 聊天室

聊天室的功能使用位于`PLVECLiveHomeFragment`类中，在该类中示例了聊天室的使用方式。

`PLVChatroomPresenter`是聊天室presnter，负责登录聊天室的控制以及通知`IPLVChatroomContract.IChatroomView`更新聊天室的UI状态。

#### 6.3 打赏

打赏的功能使用位于`PLVECLiveHomeFragment`类中，在该类中实例了打赏的使用方式。

打赏的信息事件发送是通过`PLVChatroomPresenter`的`sendCustomMsg`方法，可以使用自己定义的`dataBean`构建`PolyvCustomEvent`，通过`sendCustomMsg`方法发送自己定义的打赏事件。

#### 6.4 商品

商品的功能使用位于`PLVECLiveHomeFragment`类中，在该类中实例了商品的使用方式。

商品的获取目前是每打开一次商品的弹层，就请求一次polyv商品接口获取最新的商品更新数据，该逻辑在`PLVECLiveHomeFragment`的`showCommodityLayout`方法中。如果需要替换为自己商品库的商品，只需把获取商品的接口改为自己的接口，把使用到的商品数据模型更改为自己的商品数据模型即可。

----------

直播带货项目是基于云课堂SDK的一个定制demo版本，云课堂SDK完整功能可以参考wiki文档：https://github.com/polyv/polyv-android-cloudClass-sdk-demo/wiki