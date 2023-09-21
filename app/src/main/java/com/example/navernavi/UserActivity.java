package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UserActivity extends AppCompatActivity {

    String arvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //SharedPreferences To Print

        SharedPreferences sharedPreferences = getSharedPreferences("bookmark",MODE_PRIVATE);
        int key = sharedPreferences.getAll().size();

        if(key > 0) {
            try {
                for(int i = 0;key>i;i++) {
                    String input = sharedPreferences.getString(i+"","");
                    String[] data = input.split(":");
                    UserSub n_layout = new UserSub(getApplicationContext());
                    LinearLayout con = (LinearLayout) findViewById(R.id.scrLayout);
                    con.addView(n_layout);
                    TextView name = (TextView)n_layout.findViewById(R.id.bmName);
                    TextView dep = (TextView)n_layout.findViewById(R.id.bmDep);
                    TextView arv = (TextView)n_layout.findViewById(R.id.bmArv);
                    name.setText(data[0]);
                    dep.setText(data[1]);
                    arv.setText(data[2]);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            TextView isnull = (TextView) findViewById(R.id.isnull);
            isnull.setVisibility(View.VISIBLE);
        }


        // 북마크 추가 버튼
        Button plusBtn = (Button) findViewById(R.id.btnPlus);
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("bookmark",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
            }
        });
    }

    public void requestDirect(String depart, String arrival) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();

            String query = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+depart+"&goal="+arrival;
            String[] option = {"trafast","tracomfort","traoptimal","traavoidtoll","traavoidcaronly"};
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "52qqm2ev4e");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "xTdW0pV93xz6x9ZM948xmH4iGvpheQZwmKwx0PjM");
                conn.setDoInput(true);

                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                int indexFirst,indexLast;
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                indexFirst = stringBuilder.indexOf("\"currentDateTime\":");
                indexLast = stringBuilder.indexOf("\"route");

                Date currentTime = (Date) DateFormat.parse(stringBuilder.substring(indexFirst+19,indexLast-2).replace("T"," "));
                System.out.println(currentTime);

                indexFirst = stringBuilder.indexOf("\"duration\":");
                indexLast = stringBuilder.indexOf("\"etaService");

                int arrivalTime = Integer.parseInt(stringBuilder.substring(indexFirst+11,indexLast-1))/1000;

                long hour;
                long minute = Math.round(arrivalTime/60);
                long second = Math.round(arrivalTime%60);
                if(minute >= 60) {
                    hour = Math.round(minute/60);
                    minute = Math.round(minute%60);
                    arvTime = ("예상 소요 시간 : "+hour+"시간 "+minute+"분 "+second+"초");
                } else { arvTime = ("예상 소요 시간 : "+minute+"분 "+second+"초"); }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}