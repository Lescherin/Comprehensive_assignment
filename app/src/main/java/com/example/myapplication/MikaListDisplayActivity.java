package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MikaListDisplayActivity extends AppCompatActivity {

    private ListView actualListView; // ListView 控件的引用
    private Mika_emojiAdapter mikaAdapter;
    private final List<Mika_emoji> mikaEmojiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 这个是承载 ListView 控件的布局文件
        setContentView(R.layout.activity_mika_list_display);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Mika 表情秀");
            actionBar.setDisplayHomeAsUpEnabled(true); // 显示返回箭头
        }

        // 1. 获取布局文件中的 ListView 控件
        actualListView = findViewById(R.id.mika_list_view_in_activity_layout);

        // 2. 初始化数据
        prepareMikaEmojiData();

        // 3. 创建适配器实例
        //    第一个参数是 Context (this Activity)
        //    第二个参数是自定义的列表项布局 (mika_emoji_item.xml)
        //    第三个参数是数据源
        mikaAdapter = new Mika_emojiAdapter(this, R.layout.mika_emoji_item, mikaEmojiList);

        // 4. 将适配器设置给 ListView
        actualListView.setAdapter(mikaAdapter);

        // 设置列表项点击事件（AI提供的交互元素）
        actualListView.setOnItemClickListener((parent, view, position, id) -> {
            Mika_emoji clickedEmoji = mikaEmojiList.get(position);
            Toast.makeText(MikaListDisplayActivity.this, "你摸了未花,她" + clickedEmoji.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void prepareMikaEmojiData() {
        // 填充表情数据
        mikaEmojiList.add(new Mika_emoji("跳起了蛋糕卷舞蹈", R.drawable.dancing_01));
        mikaEmojiList.add(new Mika_emoji("蛋糕卷切片被同伴抢去了", R.drawable.snatching_02));
        mikaEmojiList.add(new Mika_emoji("期待你的问候", R.drawable.awaiting_03));
        mikaEmojiList.add(new Mika_emoji("对这个事物感到疑惑", R.drawable.confusing_04));
        mikaEmojiList.add(new Mika_emoji("觉得这个事物很好笑", R.drawable.laughing_05));
        mikaEmojiList.add(new Mika_emoji("充满智慧地凝视着", R.drawable.staring_06));
        mikaEmojiList.add(new Mika_emoji("感觉很满足", R.drawable.smiling_07));
        mikaEmojiList.add(new Mika_emoji("正在吃喜欢的瑞士卷", R.drawable.eating_08));
    }

    // 处理 ActionBar 返回按钮
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 关闭当前 Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}