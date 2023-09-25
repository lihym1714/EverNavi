package com.example.navernavi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


public class Sub_category extends LinearLayout{
    public Sub_category(Context context, AttributeSet attrs) {
        super(context,attrs);

        init(context);
    }

    public Sub_category(Context context) {
        super(context);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sub_cat,this,true);
    }
}
