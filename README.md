## 这里作为上传测试的阅读文档,这个Android项目是一个综合性Android应用，集成了联系人管理、网络功能、邮箱跳转、传感器读取、多媒体播放、ListView展示等多项功能
## 主要功能模块简要说明:
### 启动与主界面
MainActivity.java 主界面集成所有功能入口，支持跳转到各个子功能页面，并实现拨号、邮箱、网络诊断、天气，传感器查询等快捷操作。
### 联系人管理
ContactsDbHelper.java	使用SQLite数据库存储联系人，支持姓名和电话唯一性约束。
Read_ContactList.java	实现联系人增删改查，支持通过ListView展示联系人信息。
ContactSyncHelper.java	支持一键将本地联系人同步到系统通讯录（课外技术）。
### ListView与自定义适配器
Mika_emoji.java & Mika_emojiAdapter.java	定义表情数据结构和自定义适配器，定义ListView的资源以及文字展示。
MikaListDisplayActivity.java 		展示Mika表情列表，支持点击交互。
### 多媒体功能
MetalGearSolid_PV.java	实现本地音频视频播放功能、网络视频嵌入（WebView）流媒体播放、VideoView多媒体控制。以及基本的暂停，重新播放。
### 广播与网络
Sensor_Check.java	读取并展示多种手机传感器数据，实时显示传感器变化
### 传感器组件
MyBroadcastReceiver.java	自定义广播接收器，接收广播后自动跳转到网络检测页面。
BootComplete.java	开机广播接收器，开机时弹出提示。
Web_Check.java	实现网络状态监听、浏览器跳转搜索和自定义广播发送。
Weather_Forecast.java	通过外部API获取天气信息，展示网络请求与JSON解析。
MyTrustManager.java	用于网络安全连接时的信任管理（配合天气API模块使用）




