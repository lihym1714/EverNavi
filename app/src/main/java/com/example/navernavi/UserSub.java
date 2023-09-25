package com.example.navernavi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class UserSub extends LinearLayout {

    public UserSub(Context context, AttributeSet attrs) {
        super(context,attrs);

        init(context);
    }

    public UserSub(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sub,this,true);
    }
}