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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    List<List<LatLng>> loot;
    List<Integer> Congestion = new ArrayList<>();

    private double depX, depY, arvX, arvY;
    private LatLng depart,arrival, tmpLoc;
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
        LinearLayout cardBar = (LinearLayout) findViewById(R.id.cardView);
        LinearLayout cardBar2 = (LinearLayout) findViewById(R.id.naviLayout);
        cardBar2.setVisibility(View.INVISIBLE);

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
                                cardBar.setVisibility(View.VISIBLE);
                            }
                        };
                          new Thread(() -> {
                            requestGeocode();
                            if (tmpLoc != null) {
                                cameraSet(tmpLoc, 0);
                                mHandler.post(runnable);
                            }
                        }).start();
                    } else {
                        showDialog("Warning","주소지를 입력해주세요.");
                    }
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 마크 버튼 클릭시 지정된 좌표에 마크 표시
        Button btnMark = (Button) findViewById(R.id.btnmark1);
        btnMark.setOnClickListener(view -> {
            TextView depAddr = (TextView)findViewById(R.id.departureAddr);
            depart = tmpLoc;
            setMark(markerDep, depart, com.naver.maps.map.R.drawable.navermap_default_marker_icon_red);
            depAddr.setText(searchBar.getText().toString());
            searchBar.setText("");
            marker.setMap(null);
        });
        Button btnMark2 = (Button) findViewById(R.id.btnmark2);
        btnMark2.setOnClickListener(view -> {
            TextView arvAddr = (TextView)findViewById(R.id.arrivalAddr);
            arrival = tmpLoc;
            setMark(markerArv, arrival, com.naver.maps.map.R.drawable.navermap_default_marker_icon_green);
            arvAddr.setText(searchBar.getText().toString());
            searchBar.setText("");
            marker.setMap(null);
        });
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
                    cardBar.setVisibility(View.INVISIBLE);
                    if (loot != null) {
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
                        arvX = arrival.latitude;
                        arvY = arrival.longitude;
                        requestDirect(0, depY + "," + depX, arvY + "," + arvX);
                        requestDirect(1, depY + "," + depX, arvY + "," + arvX);
                        LatLng avgLoc = new LatLng((depX+arvX)/2,(depY+arvY)/2);
                        mHandler.post(runnable);
                        cameraSet(avgLoc,2);
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
            Thread thread = new Thread(new NewRunnable());
            thread.start();
        });

        // + 버튼 클릭시 북마크 페이지 전환
        Button pageTrans = (Button) findViewById(R.id.btn1);
        pageTrans.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivity(intent);
        });

        Button mapClear = (Button) findViewById(R.id.btnClear);
        mapClear.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            finish();
            startActivity(intent);
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
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        MainActivity.naverMap = naverMap;

        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setZoomControlEnabled(false);
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Navi);

        //건물 표시
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);
    }

    private void requestGeocode() {
        try {
            EditText searchBar;
            searchBar = findViewById(R.id.editTextSearch);

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String addr = searchBar.getText().toString();
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query= " + URLEncoder.encode(addr, "UTF-8");
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

                if (responseCode == 200) { //200 = OK , 400 = INVALID_REQUEST , 500 = SYSTEM ERROR
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    int indexFirst;
                    int indexLast;

                    indexFirst = stringBuilder.indexOf("\"x\":\"");
                    indexLast = stringBuilder.indexOf("\",\"y\":");
                    String x = stringBuilder.substring(indexFirst + 5, indexLast);

                    indexFirst = stringBuilder.indexOf("\"y\":\"");
                    indexLast = stringBuilder.indexOf("\",\"distance\":");
                    String y = stringBuilder.substring(indexFirst + 5, indexLast);

                    tmpLoc = new LatLng(Double.parseDouble(y), Double.parseDouble(x));
                    bufferedReader.close();
                    conn.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void requestDirect(int div, String Depart, String Arrival) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String[] option = {"trafast","tracomfort","traoptimal","traavoidtoll","traavoidcaronly"};
            StringBuilder query = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=" + Depart + "&goal=" + Arrival);
            query.append("&").append(option[0]);
            if(wayPoints != null) {
                query.append("&waypoints=");
                for(int i = 0;wayPoints.size()>i;i++) {
                    String wayP = wayPoints.get(i).longitude + "," + wayPoints.get(i).latitude;
                    query.append(wayP).append("|");
                }
            }
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
                        ArrayList<Integer> pointIndex = new ArrayList<>();
                        ArrayList<Integer> pointCount = new ArrayList<>();
                        ArrayList<Integer> congestion = new ArrayList<>();

                        indexFirst = stringBuilder.indexOf("\"path\":");
                        indexLast = stringBuilder.indexOf(",\"section\"");
                        String[] coord = (stringBuilder.substring(indexFirst + 8, indexLast - 1)).split(",");

                        indexFirst = stringBuilder.indexOf("\"guide");
                        String section = stringBuilder.substring(indexLast + 9, indexFirst);
                        ArrayList<Integer> tmpArr = new ArrayList<>();
                        Matcher matcher = Pattern.compile("pointIndex").matcher(section);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            pointIndex.add(Integer.parseInt(section.substring(tmpArr.get(0) + 12,
                                    tmpArr.get(0) + 17).replaceAll("[^0-9]", "")));
                            tmpArr.remove(0);
                        }

                        matcher = Pattern.compile("pointCount").matcher(section);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            pointCount.add(Integer.parseInt(section.substring(tmpArr.get(0) + 12,
                                    tmpArr.get(0) + 17).replaceAll("[^0-9]", "")));
                            tmpArr.remove(0);
                        }
                        matcher = Pattern.compile("congestion").matcher(section);
                        while (matcher.find()) {
                            tmpArr.add(matcher.start());
                        }
                        while (!tmpArr.isEmpty()) {
                            congestion.add(Integer.parseInt(section.substring(tmpArr.get(0) + 12,
                                    tmpArr.get(0) + 17).replaceAll("[^0-9]", "")));
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

                        int cnt = 0;
                        while (!pointIndex.isEmpty()) {
                            try {
                                if (pointIndex.get(0) + pointCount.get(0) == cnt) {
                                    tmpPath.add(Loot.get(0));
                                    multiPath.add((List<LatLng>) tmpPath.clone());
                                    tmpPath.clear();
                                    pointCount.remove(0);
                                    pointIndex.remove(0);
                                    Congestion.add(congestion.get(0));
                                    if (congestion.size() > 1) congestion.remove(0);
                                } else if (pointIndex.get(0) == cnt) {
                                    tmpPath.add(Loot.get(0));
                                    multiPath.add((List<LatLng>) tmpPath.clone());
                                    tmpPath.clear();
                                    Congestion.add(congestion.get(0));
                                }
                                if (pointIndex.get(0) + pointCount.get(0) >= cnt && cnt >= pointIndex.get(0)) {
                                    tmpPath.add(Loot.get(0));
                                    Loot.remove(0);
                                    cnt++;
                                } else if (pointIndex.get(0) + pointCount.get(0) > cnt) {
                                    tmpPath.add(Loot.get(0));
                                    Loot.remove(0);
                                    cnt++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (Loot.size() <= 1) {
                            Loot.add(Loot.get(0));
                        }
                        multiPath.add(Loot);
                        Congestion.add(congestion.get(0));

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

                        Integer.parseInt(String.valueOf(arrivalTime));
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
            List colorParts = new ArrayList<Color>();
            for(int i = 0;Congestion.size()>i;i++) {
                switch (Congestion.get(i)) {
                    case 0: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#1DDB16"), Color.WHITE, Color.GRAY, Color.LTGRAY));
                        break;
                    case 1: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#FFDF24"), Color.WHITE, Color.GRAY, Color.LTGRAY));
                        break;
                    case 2: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#FF8224"), Color.WHITE, Color.GRAY, Color.LTGRAY));
                        break;
                    case 3: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#ED0000"), Color.WHITE, Color.GRAY, Color.LTGRAY));
                        break;
                    case 4: colorParts.add(new MultipartPathOverlay.ColorPart(Color.parseColor("#AAAAAA"), Color.WHITE, Color.GRAY, Color.LTGRAY));
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
            marker.setAlpha(0.8f);
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

        SharedPreferences sharedPreferences = getSharedPreferences("bookmark", MODE_PRIVATE);

        int key = sharedPreferences.getAll().size();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key+"",save);
        editor.commit();
    }
}

