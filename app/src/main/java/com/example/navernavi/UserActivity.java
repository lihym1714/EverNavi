package com.example.navernavi;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.naver.maps.geometry.LatLng;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class UserActivity extends AppCompatActivity {

    String arvTime;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);


        listUp();
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

        Button ReloadBtn = (Button) findViewById(R.id.btnReload);
        ReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//인텐트 종료
                overridePendingTransition(0, 0);//인텐트 효과 없애기
                Intent intent = getIntent(); //인텐트
                startActivity(intent); //액티비티 열기
                overridePendingTransition(0, 0);//인텐트 효과 없애기
            }
        });
    }

    private void listUp() {
        //SharedPreferences To Print
        SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
        int key = sharedPreferences.getAll().size();

        LinearLayout scr_layout = (LinearLayout)findViewById(R.id.scrLayout);
        ArrayList<Sub_category> categoriesSub = new ArrayList<>();
        ArrayList<ArrayList<UserSub>> userSubList = new ArrayList<>();
        ArrayList<UserSub> userSub = new ArrayList<>();
        String runtime;

        if (key>0) {
            TextView isnull = (TextView) findViewById(R.id.isnull);
            isnull.setVisibility(View.GONE);
        }

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

                SharedPreferences loot = getSharedPreferences(SP.split(":")[0],MODE_PRIVATE);
                LinearLayout layout = (LinearLayout) categoriesSub.get(i).findViewById(R.id.catlayout);
                for(int j = 0;loot.getAll().size()>j;j++) {
                    userSub.add(new UserSub(getApplicationContext()));
                    layout.addView(userSub.get(j));

                    String SP2 = loot.getString(j+"","");
                    TextView SubName = (TextView) userSub.get(j).findViewById(R.id.bmName);
                    TextView SubTime = (TextView) userSub.get(j).findViewById(R.id.bmTime);
                    TextView wayPTxt = (TextView) userSub.get(j).findViewById(R.id.waypointText);
                    SubName.setText("경로명 : "+SP2.split(":")[0]);
                    final String[] wayPointTmp = new String[1];
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            SubTime.setText(arvTime);
                            wayPTxt.setText(wayPointTmp[0]);
                        }
                    };
                    class NewRunnable implements Runnable {
                        @Override
                        public void run() {
                            String[] Dep = requestKeyword(dep.getText().toString()).split(",");
                            String[] Arv = requestKeyword(arv.getText().toString()).split(",");
                            String waypoints = "";
                            try {
                                if(SP2.split(":")[3].length() > 0) waypoints = SP2.split(":")[3];
                                wayPointTmp[0] = waypoints;
                            } catch (Exception e) {e.printStackTrace();}
                            arvTime = requestDirect((Dep[1]+","+Dep[0]),(Arv[1]+","+Arv[0]),waypoints);
                            mHandler.post(runnable);
                        }
                    }
                    Thread thread = new Thread(new NewRunnable());
                    thread.start();
                }
                userSubList.add((ArrayList<UserSub>) userSub.clone());
                userSub.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for(int i = 0;categoriesSub.size()>i;i++) {
            final int finalI = i;
            Button lootGen = (Button)categoriesSub.get(i).findViewById(R.id.catButton);
            Button name = (Button)categoriesSub.get(finalI).findViewById(R.id.catButton);
            TextView dep = categoriesSub.get(finalI).findViewById(R.id.deptext);
            TextView arv = categoriesSub.get(finalI).findViewById(R.id.arvtext);
            lootGen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(UserActivity.this,MainActivity.class);
                        String input = "viewLoot:"+name.getText().toString()+":"+dep.getText().toString()+":"+arv.getText().toString();
                        intent.putExtra("key",input);
                        startActivity(intent);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            for(int j = 0;userSubList.get(finalI).size()>j;j++) {
                int finalJ = j;
                Button sublootGen = (Button)userSubList.get(i).get(j).findViewById(R.id.SubLootgen);
                sublootGen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(UserActivity.this,MainActivity.class);
                            String input = "viewLootOnly:"+name.getText().toString()+":"+dep.getText().toString()+":"+arv.getText().toString()+":"+finalJ;
                            intent.putExtra("key",input);
                            startActivity(intent);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            Button catPlus = (Button)categoriesSub.get(i).findViewById(R.id.catPlus);
            catPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = "saveLoot:"+name.getText().toString()+":"+dep.getText().toString()+":"+arv.getText().toString();
                    Intent intent = new Intent(UserActivity.this,MainActivity.class);
                    intent.putExtra("key",input);
                    startActivity(intent);
                }
            });


            ToggleButton toggleBtn = (ToggleButton) categoriesSub.get(i).findViewById(R.id.toggleButton);
            toggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    LinearLayout layout = (LinearLayout) categoriesSub.get(finalI).findViewById(R.id.catlayout);
                    if (isChecked) {
                        layout.setVisibility(View.GONE);
                        toggleBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.triangle_drawable2));
                    } else {
                        layout.setVisibility(View.VISIBLE);
                        toggleBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.triangle_drawable));
                    }
                }
            });
        }

    }


    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        // 기존의 뒤로가기 버튼의 기능 제거
        // super.onBackPressed();

        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2초 이내에 뒤로가기 버튼을 한번 더 클릭시 finish()(앱 종료)
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }


    public String requestDirect(String depart, String arrival, String waypoints) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();

            String query = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+depart+"&goal="+arrival;
            String[] option = {"trafast","tracomfort","traoptimal","traavoidtoll","traavoidcaronly"};
            if(waypoints != "") query += "&waypoints="+waypoints;
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
                indexFirst = stringBuilder.indexOf("\"goal\":");
                indexLast = stringBuilder.indexOf("\"etaService");

                String[] indexing = stringBuilder.substring(indexFirst + 11, indexLast - 1).split(",");
                long arrivalTime = Long.parseLong(indexing[indexing.length - 1].replaceAll("[^0-9]", "")) / 1000;

                long hour;
                long minute = Math.round(arrivalTime / 60);
                long second = Math.round(arrivalTime % 60);
                if (minute >= 60) {
                    hour = Math.round(minute / 60);
                    minute = Math.round(minute % 60);
                    return "예상 소요 시간 : " + hour + "시간 " + minute + "분 " + second + "초";
                } else {
                    return "예상 소요 시간 : " + minute + "분 " + second + "초";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed";
    }
    private String requestKeyword(String addr) {
        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=1&sort=accuracy&query=" + URLEncoder.encode(addr, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "KakaoAK 85866fd056ceefc6ea65b49fbfd5fe75");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) { //200 = OK , 400 = INVALID_REQUEST , 500 = SYSTEM ERROR
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    int index = stringBuilder.indexOf("x")+3;
                    String  x = stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", "");
                    index = index + 22;
                    String y = stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", "");
                    return y+","+x;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}