package com.example.navernavi.inflate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.navernavi.R;

public class Place extends LinearLayout {

    public Place(Context context, AttributeSet attrs) {
        super(context,attrs);

        init(context);
    }

    public Place(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.place,this,true);
    }
}
