package com.example.navernavi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class MainSub extends LinearLayout {
    public MainSub(Context context, AttributeSet attrs) {
        super(context,attrs);

        init(context);
    }

    public MainSub(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.main_sub,this,true);
    }
}
