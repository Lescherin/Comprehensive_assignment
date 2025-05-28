package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Mika_emojiAdapter extends ArrayAdapter<Mika_emoji> {
    private final int resourceId;

    public Mika_emojiAdapter(Context context, int textViewResourceId, List<Mika_emoji> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Mika_emoji mika_emoji = getItem(position); // 获取当前项的实例

        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mika_emoji_image = view.findViewById(R.id.mika_emoji_image);
            viewHolder.mika_emoji_name = view.findViewById(R.id.mika_emoji_name);
            view.setTag(viewHolder); // 缓存 viewHolder 对象
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (mika_emoji != null) {
            viewHolder.mika_emoji_image.setImageResource(mika_emoji.getImageId());
            viewHolder.mika_emoji_name.setText(mika_emoji.getName());
        }

        return view;
    }

    // 内部类（ViewHolder 模式）用于优化显示性能
    static class ViewHolder {
        ImageView mika_emoji_image;
        TextView mika_emoji_name;
    }
}