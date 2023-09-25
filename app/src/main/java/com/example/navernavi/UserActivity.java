package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


public class UserActivity extends AppCompatActivity {

    String arvTime;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //SharedPreferences To Print
        SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
        int key = sharedPreferences.getAll().size();

        LinearLayout scr_layout = (LinearLayout)findViewById(R.id.scrLayout);
        ArrayList<Sub_category> categoriesSub = new ArrayList<>();

        for(int i = 0;key > i; i++) {
            try {
                categoriesSub.add(new Sub_category(getApplicationContext()));
                scr_layout.addView(categoriesSub.get(i));
                Button btn = (Button)categoriesSub.get(i).findViewById(R.id.catButton);
                TextView dep = (TextView) categoriesSub.get(i).findViewById(R.id.deptext);
                TextView arv = (TextView) categoriesSub.get(i).findViewById(R.id.arvtext);

                String SP = sharedPreferences.getString(i+"","").toString();
                btn.setText(SP.split(":")[0]);
                dep.setText(SP.split(":")[1]);
                arv.setText(SP.split(":")[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for(int i = 0;categoriesSub.size()>i;i++) {
            Button btn = (Button)categoriesSub.get(i).findViewById(R.id.catButton);
            final int finalI = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Button "+ finalI +" Clicked",Toast.LENGTH_SHORT).show();
                }
            });
        }


//        if(key > 0) {
//            try {
//                String[][] data = new String[1][3];
//                final Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        UserSub n_layout = new UserSub(getApplicationContext());
//                        LinearLayout con = (LinearLayout) findViewById(R.id.scrLayout);
//                        con.addView(n_layout);
//                        TextView name = (TextView) n_layout.findViewById(R.id.bmName);
//                        TextView dep = (TextView) n_layout.findViewById(R.id.bmDep);
//                        TextView arv = (TextView) n_layout.findViewById(R.id.bmArv);
//                        name.setText(data[0][0]);
//                        dep.setText(data[0][1]);
//                        arv.setText(data[0][2]);
//                    }
//                };
//                class NewRunnable implements Runnable {
//                    @Override
//                    public void run() {
//                        data[0][1] = requestReverseGc(data[0][1]);
//                        data[0][2] = requestReverseGc(data[0][2]);
//                        mHandler.post(runnable);
//                    }
//                }
//
//                for (int i = 0; key > i; i++) {
//                    String input = sharedPreferences.getString(i + "", "");
//                    data[0] = input.split(":");
//                    Thread thread = new Thread(new NewRunnable());
//                    thread.start();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            TextView isnull = (TextView) findViewById(R.id.isnull);
//            isnull.setVisibility(View.VISIBLE);
//        }



        // 북마크 추가 버튼
        Button plusBtn = (Button) findViewById(R.id.btnPlus);
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.clear();
//                editor.apply();
                Intent intent = new Intent(getApplicationContext(),SubActivity.class);
                startActivity(intent);
            }
        });
    }

    public String requestReverseGc (String location) {
        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder result = new StringBuilder();

            String query = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordToaddr&coords="+location+"&output=json&orders=roadaddr";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "52qqm2ev4e");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "xTdW0pV93xz6x9ZM948xmH4iGvpheQZwmKwx0PjM");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();

                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                int firstIndex = stringBuilder.indexOf("land");
                String tmpStr = stringBuilder.substring(firstIndex);

                result.append(tmpStr.substring(tmpStr.indexOf("name")+7,tmpStr.indexOf("coords")-3)).append(" ");
                result.append(tmpStr.substring(tmpStr.indexOf("number1")+9,tmpStr.indexOf("number2")).replaceAll("[^0-9]", ""));
                if (tmpStr.substring(tmpStr.indexOf("number2")+9,tmpStr.indexOf("addition0")).replaceAll("[^0-9]", "").length() > 0) {
                    result.append("-").append(tmpStr.substring(tmpStr.indexOf("number2") + 9, tmpStr.indexOf("addition0")).replaceAll("[^0-9]", ""));
                }

                return result.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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