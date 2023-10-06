package com.example.navernavi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Retrofit;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static NaverMap naverMap;
    private final Marker marker = new Marker();
    private final Marker markerDep = new Marker();
    private final Marker markerArv = new Marker();
    private final Marker wayP1 = new Marker();
    private final Marker wayP2 = new Marker();
    private final Marker wayP3 = new Marker();
    private final Marker wayP4 = new Marker();
    private final Marker wayP5 = new Marker();
    private boolean isView = false;
    List<List<LatLng>> loot;
    List<Integer> Congestion = new ArrayList<>();

    private double depX, depY, arvX, arvY;
    private LatLng depart,arrival, tmpLoc;
    private String intentText;
    private final ArrayList<LatLng> wayPoints = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private  static Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // 네이버 지도 UI출력
        MapView mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        LinearLayout cardBar2 = (LinearLayout) findViewById(R.id.naviLayout);
        cardBar2.setVisibility(View.INVISIBLE);


        Intent userIntent = getIntent();
        String text = userIntent.getStringExtra("key");
        TextView depAddr = (TextView)findViewById(R.id.departureAddr);
        TextView arvAddr = (TextView)findViewById(R.id.arrivalAddr);
        depAddr.setText(text.split(":")[2]);
        arvAddr.setText(text.split(":")[3]);
        intentText = text;
        LinearLayout lootGenLayout = (LinearLayout) findViewById(R.id.lootGenLayout);

        if(true) {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!markerDep.isAdded()) {
                        depY = tmpLoc.longitude; depX = tmpLoc.latitude;
                        depart = new LatLng(depX,depY);
                        setMark(markerDep, new LatLng(depX, depY), com.naver.maps.map.R.drawable.navermap_default_marker_icon_red);
                    }
                    else {
                        arvY = tmpLoc.longitude; arvX = tmpLoc.latitude;
                        setMark(markerArv, new LatLng(arvX, arvY), com.naver.maps.map.R.drawable.navermap_default_marker_icon_green);
                        cameraSet(new LatLng((depX+arvX)/2,(depY+arvY)/2),2);
                    }
                }
            };
            new Thread(() -> {
                requestKeyword(text.split(":")[2]);
                if (tmpLoc != null) {
                    mHandler.post(runnable);
                }
                requestKeyword(text.split(":")[3]);
                if (tmpLoc != null) {
                    mHandler.post(runnable);
                }
            }).start();
        }


        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String[] items = {"Trafast","Traoptional"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        int verticalOffset = Math.round(getApplicationContext().getResources().getDisplayMetrics().density * 30);
        spinner.setDropDownVerticalOffset(verticalOffset);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        EditText searchBar = (EditText)findViewById(R.id.editTextSearch);
        try {
            searchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchBar.setOnEditorActionListener((v, actionId, event) -> {
                if(searchBar.length() > 0) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm;
                                imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                                searchBar.clearFocus();
                                setMark(marker, new LatLng(tmpLoc.latitude, tmpLoc.longitude), com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue);
                            }
                        };
                          new Thread(() -> {
                              requestKeyword();
                              if (tmpLoc != null) {
                                  cameraSet(tmpLoc, 0);
                                  mHandler.post(runnable);
                              }
                          }).start();
                    } else {
                        Toast.makeText(getApplicationContext(),"주소지를 입력해주세요",Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button WaypBtn = (Button) findViewById(R.id.btnWay);
        WaypBtn.setOnClickListener(view -> {
            try {
                if(tmpLoc != null) {
                    if (1 > wayPoints.size()) {setMark(wayP1, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_gray);}
                    else if (2 > wayPoints.size()) {setMark(wayP2, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_gray);}
                    else if (3 > wayPoints.size()) {setMark(wayP3, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_gray);}
                    else if (4 > wayPoints.size()) {setMark(wayP4, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_gray);}
                    else if (5 > wayPoints.size()) {setMark(wayP5, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_gray);}
                    wayPoints.add(tmpLoc);
                    marker.setMap(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button lootBtn = (Button) findViewById(R.id.lootGen);
        lootBtn.setOnClickListener(view -> {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (loot != null) {
                        isView=true;
                        cardBar2.setVisibility(View.VISIBLE);
                        searchBar.setVisibility(View.INVISIBLE);
                        drawPath(loot);
                    }
                }
            };
            class NewRunnable implements Runnable {
                @Override
                public void run() {
                    try {
                        if (depart == null) {
                            depX = locationSource.getLastLocation().getLatitude();
                            depY = locationSource.getLastLocation().getLongitude();
                        } else {
                            depX = depart.latitude;
                            depY = depart.longitude;
                        }
                        requestDirect(0, depY + "," + depX, arvY + "," + arvX,"");
                        requestDirect(1, depY + "," + depX, arvY + "," + arvX,"");
                        LatLng avgLoc = new LatLng((depX+arvX)/2,(depY+arvY)/2);
                        mHandler.post(runnable);
                        cameraSet(avgLoc,2);
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
            Thread thread = new Thread(new NewRunnable());
            thread.start();
        });

        Button mapClear = (Button) findViewById(R.id.btnClear);
        mapClear.setOnClickListener(view -> {
            finish();
        });

        Button saveBtn = (Button) findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("경로 저장");
                ad.setMessage("저장할 경로의 이름을 입력해주세요");

                final EditText et = new EditText(MainActivity.this);
                et.setSingleLine();
                ad.setView(et);
                ad.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(et.getText().length() > 0) {
                            sharedPref(et.getText().toString());
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"\""+ et.getText().toString()+"\" 경로가 저장되었습니다",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),"경로명을 입력해주세요",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });

        //After Intent Changed
        if(text.split(":")[0].contains("viewLootOnly")) {
            SharedPreferences lootShared = getSharedPreferences(text.split(":")[1],MODE_PRIVATE);
            String[] lootString = lootShared.getString(text.split(":")[4]+"","").split(":");
            lootGenLayout.setVisibility(View.GONE);
            isView = true;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    drawPath(loot);
                }
            };
            new Thread(() -> {
                try {
                    if (4 > lootString.length) {requestDirect(0, lootString[1], lootString[2],"");}
                    else {requestDirect(0, lootString[1], lootString[2],lootString[3]);}
                    mHandler.post(runnable);
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
        else if(text.split(":")[0].contains("viewLoot")) {
            lootGenLayout.setVisibility(View.GONE);
            isView = true;
            try {
                SharedPreferences lootShared = getSharedPreferences(text.split(":")[1],MODE_PRIVATE);
                for(int i = 0;lootShared.getAll().size()>i;i++) {
                    String[] lootString = lootShared.getString(i+"","").split(":");
                    //lootString [0] = name / [1] = dep / [2] = arv / [3] = waypoint
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            drawPath(loot);
                        }
                    };
                    new Thread(() -> {
                        try {
                            if (4 > lootString.length) {requestDirect(0, lootString[1], lootString[2],"");}
                            else {requestDirect(0, lootString[1], lootString[2],lootString[3]);}
                            mHandler.post(runnable);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        MainActivity.naverMap = naverMap;

        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setZoomControlEnabled(false);
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);
        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Navi);
//        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC,true);
//        naverMap.setLiteModeEnabled(true);
        //건물 표시
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);
        naverMap.setOnMapClickListener((pointF, latLng) -> {
            EditText searchbar = (EditText)findViewById(R.id.editTextSearch);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchbar.getWindowToken(),0);
            searchbar.clearFocus();
            if(!isView) {
                tmpLoc = latLng;
                setMark(marker, tmpLoc, com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue);
            }
        });

    }

    private void requestKeyword() {
        try {
            EditText searchBar;
            searchBar = findViewById(R.id.editTextSearch);

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String addr = searchBar.getText().toString();
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
                    double x = Double.parseDouble(stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", ""));
                    index = index + 22;
                    double y = Double.parseDouble(stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", ""));
                    tmpLoc = new LatLng(y,x);
                    bufferedReader.close();
                    conn.disconnect();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestKeyword(String addr) {
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
                    double x = Double.parseDouble(stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", ""));
                    index = index + 22;
                    double y = Double.parseDouble(stringBuilder.substring(index,index+18).replaceAll("[^0-9.]", ""));
                    tmpLoc = new LatLng(y,x);
                    bufferedReader.close();
                    conn.disconnect();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public void requestDirect(int div, String Depart, String Arrival, String waypoints) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String[] option = {"trafast","tracomfort","traoptimal","traavoidtoll","traavoidcaronly"};
            StringBuilder query = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=" + Depart + "&goal=" + Arrival);
            query.append("&").append(option[0]);
            if(wayPoints.size() > 0 && waypoints == "") {
                query.append("&waypoints=");
                for(int i = 0;wayPoints.size()>i;i++) {
                    String wayP = wayPoints.get(i).longitude + "," + wayPoints.get(i).latitude;
                    query.append(wayP).append("|");
                }
            } else {query.append("&waypoints="+waypoints);}
            URL url = new URL(query.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "52qqm2ev4e");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "xTdW0pV93xz6x9ZM948xmH4iGvpheQZwmKwx0PjM");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    int indexFirst, indexLast;
                    if (div == 0) { //0 = 루트 생성 , 1 = 트래픽 검색
                        ArrayList<LatLng> Loot = new ArrayList<>();
                        ArrayList<Double> duration = new ArrayList<>();
                        ArrayList<Double> distance = new ArrayList<>();
                        ArrayList<Integer> pointindex = new ArrayList<>();
                        ArrayList<String> type = new ArrayList<>();


                        indexFirst = stringBuilder.indexOf("\"path\":");
                        indexLast = stringBuilder.indexOf(",\"section\"");
                        String[] coord = (stringBuilder.substring(indexFirst + 8, indexLast - 1)).split(",");

                        indexFirst = stringBuilder.indexOf("\"guide");
                        String guide = stringBuilder.substring(indexFirst);
                        ArrayList<Integer> tmpArr = new ArrayList<>();
                        Matcher matcher = Pattern.compile("duration").matcher(guide);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            duration.add(Double.parseDouble(guide.substring(tmpArr.get(0) + 8,
                                    tmpArr.get(0) + 17).replaceAll("[^0-9]", "")));
                            tmpArr.remove(0);
                        }

                        matcher = Pattern.compile("distance").matcher(guide);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            distance.add(Double.parseDouble(guide.substring(tmpArr.get(0) + 9,
                                    tmpArr.get(0) + 16).replaceAll("[^0-9]", "")));
                            tmpArr.remove(0);
                        }
                        matcher = Pattern.compile("pointIndex").matcher(guide);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            pointindex.add(Integer.parseInt(guide.substring(tmpArr.get(0) + 12,
                                    tmpArr.get(0) + 17).replaceAll("[^0-9]", "")));
                            tmpArr.remove(0);
                        }
                        matcher = Pattern.compile("instructions").matcher(guide);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            type.add(guide.substring(tmpArr.get(0) + 12, tmpArr.get(0) + 55));
                            tmpArr.remove(0);
                        }
                        for (int i = 0; coord.length > i; i += 2) {
                            double x, y;
                            y = Double.parseDouble((coord[i].replace("[", "")));
                            x = Double.parseDouble((coord[i + 1].replace("]", "")));
                            Loot.add(new LatLng(x, y));
                        }

                        ArrayList<List<LatLng>> multiPath = new ArrayList<>();
                        ArrayList<LatLng> tmpPath = new ArrayList<>(1000);
                        double time;
                        int cnt = 0, speed1=0, speed2=0;
//                        Congestion.add(1);
                        while(!pointindex.isEmpty()) {
                            try {
                                if (pointindex.get(0) == cnt) {
                                    //(distance / (duration/1000)) * 3.6
                                    if(type.get(0).contains("고속도로 진입")) {
                                        if (type.get(0).contains("도시고속도로")) {speed1 = 30; speed2 = 15;}
                                        else if (type.get(0).contains("고속도로 진출")) {speed1 = 0; speed2=0;}
                                        else {speed1 = 40; speed2 = 25;}
                                    }
                                    tmpPath.add(Loot.get(0));
                                    time = (distance.get(0) / (duration.get(0)/1000)) * 3.6;
                                    if(tmpPath.size()==1) tmpPath.add(tmpPath.get(0));
                                    if (!tmpPath.isEmpty()) multiPath.add((List<LatLng>) tmpPath.clone());
                                    pointindex.remove(0); distance.remove(0); duration.remove(0); type.remove(0);
                                    tmpPath.clear();
                                    if (time >= 30+speed1) {Congestion.add(1);}
                                    else if (time >= 15+speed2) {Congestion.add(2);}
                                    else if (15+speed2 > time) {Congestion.add(3);}
//                                    else if (tmpPath.isEmpty()) {Congestion.add(1);}
                                }
                                else {
                                    tmpPath.add(Loot.get(0));
                                    Loot.remove(0);
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            cnt++;
                        }
                        multiPath.add(Loot);
                        loot = multiPath;
                    } else if (div == 1) {
                        indexFirst = stringBuilder.indexOf("\"goal\":");
                        indexLast = stringBuilder.indexOf("\"etaService");

                        String[] indexing = stringBuilder.substring(indexFirst + 11, indexLast - 1).split(",");
                        long arrivalTime = Long.parseLong(indexing[indexing.length - 1].replaceAll("[^0-9]", "")) / 1000;

                        TextView runtime = findViewById(R.id.runningTime);

                        long hour;
                        long minute = Math.round(arrivalTime / 60);
                        long second = Math.round(arrivalTime % 60);
                        if (minute >= 60) {
                            hour = Math.round(minute / 60);
                            minute = Math.round(minute % 60);
                            runtime.setText("예상 소요 시간 : " + hour + "시간 " + minute + "분 " + second + "초");
                        } else {
                            runtime.setText("예상 소요 시간 : " + minute + "분 " + second + "초");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void cameraSet(LatLng loc,int zoom) {
        try {
            naverMap.cancelTransitions();
            int zoomLevel = 13;
            if (zoom == 0) {zoomLevel = 17;}
            else if (zoom == 1) {zoomLevel = 13;}
            else if (zoom == 2) {zoomLevel = 11;}
            else {zoomLevel = 6;}
            CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                    loc,zoomLevel).animate(CameraAnimation.Easing);
            naverMap.moveCamera(cameraUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawPath(List<List<LatLng>> Loot) {
        try {
            MultipartPathOverlay path = new MultipartPathOverlay();
            path.setCoordParts(Loot);
            path.setPatternImage(OverlayImage.fromResource(R.drawable.triangle_drawable_path));
            List colorParts = new ArrayList<Color>();
            for(int i = 0;Congestion.size()>i;i++) {
                switch (Congestion.get(i)) {
                    case 1: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#1DDB16"), Color.BLACK, Color.GRAY, Color.LTGRAY));
                        break;
                    case 2: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#FFDF24"), Color.BLACK, Color.GRAY, Color.LTGRAY));
                        break;
                    case 3: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#ED0000"), Color.BLACK, Color.GRAY, Color.LTGRAY));
                        break;
                }
            }
            path.setCoordParts(Loot);
            path.setWidth(15);
            path.setOutlineWidth(5);
            path.setColorParts(colorParts);
            path.setMap(naverMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMark(Marker marker, LatLng Loc, int resourceID) {
        try {
            marker.setHeight(70);
            marker.setWidth(50);
            //원근감 표시
            marker.setIconPerspectiveEnabled(true);
            //아이콘 지정
            marker.setIcon(OverlayImage.fromResource(resourceID));
            //마커의 투명도
            marker.setAlpha(1f);
            //마커 위치
            marker.setPosition(Loc);
            //마커 우선순위
            marker.setZIndex(10);
            //마커 표시
            marker.setMap(naverMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialog(String title,String msg) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(msg);
        alertBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.show();
    }

    private void sharedPref(String name) {
        StringBuilder data = new StringBuilder();
        data.append(name).append(":");
        data.append(depY+","+depX).append(":");
        data.append(arvY+","+arvX).append(":");
        for(int i = 0;wayPoints.size()>i;i++) {
            data.append(wayPoints.get(i).longitude+","+wayPoints.get(i).latitude).append("|");
        }
        String save = data.toString();

        SharedPreferences sharedPreferences = getSharedPreferences(intentText.split(":")[1], MODE_PRIVATE);

        String key = String.valueOf(sharedPreferences.getAll().size());
        for(int i = 0;sharedPreferences.getAll().size()>i;i++) {
            if(!sharedPreferences.contains(i+"")) {
                key = i+"";
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,save);
        editor.commit();
    }
}

