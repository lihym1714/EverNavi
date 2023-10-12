package com.example.navernavi.inflate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.navernavi.R;

public class WaypointEdit extends LinearLayout {

    public WaypointEdit(Context context, AttributeSet attrs) {
        super(context,attrs);

        init(context);
    }

    public WaypointEdit(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.waypoint_edit,this,true);
    }
}
