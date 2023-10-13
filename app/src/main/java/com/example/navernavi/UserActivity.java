package com.example.navernavi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.navernavi.inflate.Sub_category;
import com.example.navernavi.inflate.UserSub;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;

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



        Button information = (Button) findViewById(R.id.BtnInfo);
        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Balloon balloon = new Balloon.Builder(getApplicationContext())
                        .setArrowSize(10)
                        .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.skyblue))
                        .setTextColor(Color.WHITE)
                        .setArrowOrientation(ArrowOrientation.TOP)
                        .setArrowPosition(0.9f)
                        .setArrowVisible(false)
                        .setWidthRatio(0.8f)
                        .setHeight(80)
                        .setTextSize(14f)
                        .setCornerRadius(4f)
                        .setAlpha(0.9f)
                        .setText("박스를 누르면 해당 경로를 확인할 수 있습니다.\n박스를 길게 누르면 해당 경로를 삭제할 수 있습니다.")
                        .setAutoDismissDuration(2500L)
                        .setBalloonAnimation(BalloonAnimation.FADE)
                        .build();
                balloon.show(v,10,10);
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

        if (key>0) {
            TextView isnull = (TextView) findViewById(R.id.isnull);
            isnull.setVisibility(View.GONE);
            Button plutBtn = (Button) findViewById(R.id.btnPlus);
        }
        int tmpI=0;
        for(int i = 0;key > i; i++) {
            try {
                categoriesSub.add(new Sub_category(getApplicationContext()));
                scr_layout.addView(categoriesSub.get(i));
                Button btn = (Button)categoriesSub.get(i).findViewById(R.id.catButton);
                TextView dep = (TextView) categoriesSub.get(i).findViewById(R.id.deptext);
                TextView arv = (TextView) categoriesSub.get(i).findViewById(R.id.arvtext);

                if(!sharedPreferences.getAll().keySet().contains(i+"")) {++tmpI;}

                String SP = sharedPreferences.getString((i+tmpI)+"","").toString();
                btn.setText(SP.split(":")[0]);
                dep.setText(SP.split(":")[1]);
                arv.setText(SP.split(":")[2]);

                SharedPreferences loot = getSharedPreferences(SP.split(":")[0],MODE_PRIVATE);
                LinearLayout layout = (LinearLayout) categoriesSub.get(i).findViewById(R.id.catlayout);
                int tmpJ = 0;
                for(int j = 0;loot.getAll().size()>j;j++) {
                    userSub.add(new UserSub(getApplicationContext()));
                    layout.addView(userSub.get(j));
                    if(!loot.getAll().keySet().contains(j+"")) {++tmpJ;}

                    String SP2 = loot.getString((j+tmpJ)+"","");
                    TextView SubName = (TextView) userSub.get(j).findViewById(R.id.bmName);
                    TextView SubTime = (TextView) userSub.get(j).findViewById(R.id.bmTime);
                    TextView wayPTxt = (TextView) userSub.get(j).findViewById(R.id.waypointText);
                    SubName.setText(SP2.split(":")[0]);
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

        tmpI = 0;
        for(int i = 0;categoriesSub.size()>i;i++) {
            final int finalI = i;
            LinearLayout SubCatLayout = (LinearLayout) categoriesSub.get(i).findViewById(R.id.SubCatLayout);
            Button name = (Button)categoriesSub.get(finalI).findViewById(R.id.catButton);
            TextView dep = categoriesSub.get(finalI).findViewById(R.id.deptext);
            TextView arv = categoriesSub.get(finalI).findViewById(R.id.arvtext);
            SubCatLayout.setOnClickListener(new View.OnClickListener() {
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
            SharedPreferences Sp = getSharedPreferences("Category",MODE_PRIVATE);
            SharedPreferences Sp2 = getSharedPreferences(name.getText().toString(),MODE_PRIVATE);
            if(!Sp.getAll().keySet().contains(i+"")) {++tmpI;}
            int finalTmpI = tmpI;
            SubCatLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(UserActivity.this);
                    ad.setTitle("삭제");
                    ad.setMessage("해당 경로 카테고리를 삭제하시겠습니까?");
                    ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = Sp.edit();
                            SharedPreferences.Editor editor1 = Sp2.edit();
                            editor1.clear();
                            editor1.commit();
                            editor.remove((finalTmpI+finalI)+"");
                            editor.commit();
                            Toast.makeText(getApplicationContext(),"경로가 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                            scr_layout.removeView(categoriesSub.get(finalI));
                        }
                    });
                    ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    ad.show();
                    return true;
                }
            });

            int tmpJ = 0;
            for(int j = 0;userSubList.get(finalI).size()>j;j++) {
                int finalJ = j;
                if(!Sp2.getAll().keySet().contains(j+"")) {++tmpJ;}
                LinearLayout UserSubLayout = (LinearLayout) userSubList.get(i).get(j).findViewById(R.id.userSubLayout);
                int finalTmpJ = tmpJ;
                UserSubLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(UserActivity.this,MainActivity.class);
                            String input = "viewLootOnly:"+name.getText().toString()+":"+dep.getText().toString()+":"+arv.getText().toString()+":"+(finalJ+finalTmpJ);
                            intent.putExtra("key",input);
                            startActivity(intent);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                UserSubLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(UserActivity.this);
                        ad.setTitle("삭제");
                        ad.setMessage("해당 경로를 삭제하시겠습니까?");
                        ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LinearLayout layout = (LinearLayout) categoriesSub.get(finalI).findViewById(R.id.catlayout);
                                SharedPreferences.Editor editor = Sp2.edit();
                                editor.remove((finalTmpJ+finalJ)+"");
                                editor.commit();
                                layout.removeView(userSubList.get(finalI).get(finalJ));

                                Toast.makeText(getApplicationContext(),"경로가 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                            }
                        });
                        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        ad.show();
                        return true;
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
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", Const.Client_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", Const.Client_Secret);
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