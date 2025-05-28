package com.example.myapplication;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MetalGearSolid_PV Activity
 * 这个 Activity 用于展示与《合金装备3：食蛇者》相关的内容，包括：
 * 1. 使用 MediaPlayer 播放背景音乐 ("snake_eater.ogg")，并提供暂停/继续和停止/开始的控制。
 * 2. 使用 WebView 加载并显示一个 Bilibili 视频播放页面。
 * 3. 使用 VideoView 播放一个本地音频文件 ("mgs_maintheme.ogg")，并附带媒体控制条。
 */
public class MetalGearSolid_PV extends AppCompatActivity {


    MediaPlayer ring;
    VideoView videoview;
    boolean musicPaused = false; //布尔标志，用于跟踪背景音乐是否被用户通过 "Pause" 按钮暂停

    /**
     * 当 Activity 首次创建时调用。
     * 在这里进行所有常规的静态设置：创建视图、绑定数据到列表等。
     *
     * @param savedInstanceState 如果 Activity 被重新初始化 (例如屏幕旋转后)，
     *                           此 Bundle 包含之前通过 onSaveInstanceState(Bundle) 保存的数据。
     *                           如果 Activity 是首次创建，则为 null。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类的 onCreate 方法
        setContentView(R.layout.mgsiii_pv); // 设置 Activity 的用户界面布局，从 R.layout.mgsiii_pv (mgsiii_pv.xml) 文件加载


        videoview = findViewById(R.id.videoView);
        // 创建 MediaPlayer 实例，并加载raw文件夹下的音频
        ring = MediaPlayer.create(MetalGearSolid_PV.this, R.raw.snake_eater);


        // 通过 ID 从布局文件中获取 WebView 控件的引用
        WebView myWebView = findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings(); // 获取 WebView 的设置对象
        webSettings.setJavaScriptEnabled(true); // 允许 WebView 执行 JavaScript
        myWebView.setWebViewClient(new WebViewClient()); // 设置 WebViewClient，这样链接会在 WebView 内部打开，而不是在外部浏览器中
        // 加载指定的视频播放页面 URL
        myWebView.loadUrl("https://player.bilibili.com/player.html?isOutside=true&aid=114547348213916");

        // 由于考虑视频文件过大这里选择在 VideoView 中播放一个音频文件，并利用其媒体控制功能
        Uri uri = Uri.parse("android.resource://" + getPackageName()
                + "/" + R.raw.mgs_maintheme);
        videoview.setVideoURI(uri); // 设置 VideoView 要播放的媒体资源的 URI
        // 创建 MediaController 实例，它提供了标准的播放控制用户界面
        MediaController controller = new MediaController(this);
        // setMediaPlayer 用于让控制器知道它要控制哪个可播放对象
        controller.setMediaPlayer(videoview);
        // 将 VideoView 与 MediaController 关联起来
        // setMediaController 用于给 VideoView 配备一个媒体控制器
        videoview.setMediaController(controller);
        // VideoView 通常在调用 start() 后才开始播放，或者用户点击控制条上的播放按钮
    }

    /**
     * 处理 "Pause" 按钮 (ID: musicBtn1) 的点击事件。
     * 此方法在 mgsiii_pv.xml 文件中通过 android:onClick="musicPause" 属性指定。
     * 功能：切换背景音乐 (ring) 的播放和暂停状态。
     */
    public void musicPause(View v) {
        Toast.makeText(this, "庆祝经典歌曲食蛇者原班人马重置", Toast.LENGTH_SHORT).show();
        // 检查背景音乐是否正在播放，或者是否已被此按钮暂停
        if (ring.isPlaying() || musicPaused) {
            // 如果音乐当前不是被此按钮暂停的状态 (即 musicPaused 为 false)
            if (!musicPaused) {
                ring.pause();       // 暂停音乐
                musicPaused = true; // 记录音乐已被此按钮暂停
            }
            // 如果音乐当前是被此按钮暂停的状态 (即 musicPaused 为 true)
            else {
                ring.start();       // 继续播放音乐 (从暂停处开始)
                musicPaused = false;// 清除暂停标志
            }
        }
        // 如果音乐没有在播放且 musicPaused 也为 false (例如已停止或从未开始)，此按钮不做任何操作
    }

    /**
     * 处理 "Stop/Start" 按钮 (ID: musicBtn2) 的点击事件。
     * 此方法在 mgsiii_pv.xml 文件中通过 android:onClick="musicStopStart" 属性指定。
     * 功能：如果背景音乐 (ring) 正在播放或已暂停，则停止它；如果已停止，则从头开始播放。
     *
     * @param v 被点击的 View 对象 (这里是 Button)
     */
    public void musicStopStart(View v) {
        Toast.makeText(this, "我是小岛秀夫制作者的粉丝", Toast.LENGTH_SHORT).show();
        try {
            // 如果音乐正在播放，或者被 "Pause" 按钮暂停了
            if (ring.isPlaying() || musicPaused) {
                ring.stop();    // 停止音乐播放
                ring.prepare(); // 让 MediaPlayer 进入准备状态，以便下次可以重新播放
                musicPaused = false; // 清除暂停标志，因为音乐现在是停止状态
            }
            // 如果音乐当前没有在播放
            else {
                ring.start(); // 从头开始播放音乐
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈信息，方便调试
        }
    }

    /**
     * 当 Activity 即将被销毁时调用。
     * 这是进行所有最终清理的理想场所，例如释放资源。
     * AI提供的建议结束播放后做资源利用回收
     */
    @Override
    protected void onDestroy() {
        super.onDestroy(); // 调用父类的 onDestroy
        // 释放 MediaPlayer 资源，以避免内存泄漏
        if (ring != null) {
            if (ring.isPlaying()) {
                ring.stop(); // 如果正在播放，先停止
            }
            ring.release(); // 释放 MediaPlayer 对象占用的所有资源
            ring = null;    // 将引用设为 null，帮助垃圾回收
        }
        // VideoView 通常不需要手动释放，系统会处理，但如果 VideoView 正在播放，
        // 并且希望在 Activity 销毁时立即停止，可以调用 videoview.stopPlayback()。
        if (videoview != null) {
            videoview.stopPlayback(); // 停止 VideoView 的播放
        }
    }
}
