package com.example.navernavi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static NaverMap naverMap;
    private Marker marker1 = new Marker();
    private Marker marker2 = new Marker();
    List<List<LatLng>> loot;
    ArrayList<LatLng> loot2 = new ArrayList<>();
    List<Integer> Congestion = new ArrayList<>();
    MultipartPathOverlay multipartPath = new MultipartPathOverlay();

    private double depX, depY, arvX, arvY;  //모든 함수에서 좌표값을 사용하기 위한 location 전역변수

    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
//    private static final String[] PERMISSIONS = {
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 마크 버튼 클릭시 지정된 좌표에 마크 표시
        Button btnMark = (Button) findViewById(R.id.btnmark1);
        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    requestGeocode(1);
                }).start();
                setMark(marker1, depX, depY, com.naver.maps.map.R.drawable.navermap_default_marker_icon_red);
                cameraSet(depX,depY);
            }
        });
        Button btnMark2 = (Button) findViewById(R.id.btnmark2);
        btnMark2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    requestGeocode(2);
                }).start();
                setMark(marker2, arvX, arvY, com.naver.maps.map.R.drawable.navermap_default_marker_icon_green);
                cameraSet(arvX,arvY);
            }
        });
        Button lootBtn = (Button) findViewById(R.id.lootGen);
        lootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    requestDirect(0,depY+","+depX,arvY+","+arvX);
                    requestDirect(1,depY+","+depX,arvY+","+arvX);
                }).start();
                drawPath(loot);
                cameraSet((depX+arvX)/2,(depY+arvY)/2);
            }
        });

        // + 버튼 클릭시 로그인 페이지 전환
        Button pageTrans = (Button) findViewById(R.id.btn1);
        pageTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SubActivity.class);
                startActivity(intent);
            }
        });

//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow()


        // 네이버 지도 UI출력
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    private void setMark(Marker marker, double lat, double lng, int resourceID) {
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
            marker.setPosition(new LatLng(lat, lng));
            //마커 우선순위
            marker.setZIndex(10);
            //마커 표시
            marker.setMap(naverMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Navi);

        //건물 표시
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, true);

        //위치 및 각도 조정
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(37.51315056189764,126.94673392918533),   // 위치 지정
                18,                                     // 줌 레벨
                0,                                       // 기울임 각도
                0                                     // 방향
        );
        naverMap.setCameraPosition(cameraPosition);
    }

    private void requestGeocode(int div) {
        try {
            TextView tmptxt;
            tmptxt = findViewById(R.id.textView2);

            EditText searchBar;
            searchBar = findViewById(R.id.editTextSearch);

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String addr = searchBar.getText().toString(); //노량진동 147-12
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
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                }

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                int indexFirst;
                int indexLast;

                indexFirst = stringBuilder.indexOf("\"x\":\"");
                indexLast = stringBuilder.indexOf("\",\"y\":");
                String x = stringBuilder.substring(indexFirst + 5, indexLast);

                indexFirst = stringBuilder.indexOf("\"y\":\"");
                indexLast = stringBuilder.indexOf("\",\"distance\":");
                String y = stringBuilder.substring(indexFirst + 5, indexLast);

                tmptxt.setText("X: " + x + ", " + "Y: " + y);
                if(div == 1) {
                    depX = Double.parseDouble(y);
                    depY = Double.parseDouble(x);
                } else if (div == 2){
                    arvX = Double.parseDouble(y);
                    arvY = Double.parseDouble(x);
                }

                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int requestDirect(int div, String depart, String arrival) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();

            depart = depY+","+depX;
            arrival = arvY+","+arvX;
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
                if(div == 0) { //0 = 루트 생성 , 1 = 트래픽 검색
                    ArrayList<LatLng> Loot = new ArrayList<>();
                    ArrayList<Integer> pointIndex = new ArrayList<>();
                    ArrayList<Integer> pointCount = new ArrayList<>();
                    ArrayList<Integer> congestion = new ArrayList<>();

                    indexFirst = stringBuilder.indexOf("\"path\":");
                    indexLast = stringBuilder.indexOf(",\"section\"");
                    String[] coord = (stringBuilder.substring(indexFirst + 8,indexLast-1)).split(",");


                    indexFirst = stringBuilder.indexOf("\"guide");
                    String section = new String(stringBuilder.substring(indexLast+9,indexFirst));
                    ArrayList<Integer> tmpArr = new ArrayList<>();
                    Matcher matcher = Pattern.compile("pointIndex").matcher(section);
                    while (matcher.find()) { tmpArr.add(matcher.start()); }
                    while(!tmpArr.isEmpty()) {
                        pointIndex.add(Integer.parseInt(section.substring(tmpArr.get(0)+12,
                                tmpArr.get(0)+17).replaceAll("[^0-9]","")));
                        tmpArr.remove(0);}

                    matcher = Pattern.compile("pointCount").matcher(section);
                    while (matcher.find()) { tmpArr.add(matcher.start()); }
                    while(!tmpArr.isEmpty()) {
                        pointCount.add(Integer.parseInt(section.substring(tmpArr.get(0)+12,
                                tmpArr.get(0)+17).replaceAll("[^0-9]","")));
                        tmpArr.remove(0);}
                    matcher = Pattern.compile("congestion").matcher(section);
                    while (matcher.find()) { tmpArr.add(matcher.start()); }
                    while(!tmpArr.isEmpty()) {
                        congestion.add(Integer.parseInt(section.substring(tmpArr.get(0)+12,
                                tmpArr.get(0)+17).replaceAll("[^0-9]","")));
                        tmpArr.remove(0);}
                    loot2.add(new LatLng(depX,depY));
                    for(int i = 0; coord.length > i; i+=2) {
                        Double x,y;
                        y = Double.parseDouble((coord[i].replace("[","")));
                        x = Double.parseDouble((coord[i+1].replace("]","")));
                        Loot.add(new LatLng(x,y));
                        loot2.add(new LatLng(x,y));
                    }
                    loot2.add(new LatLng(arvX,arvY));

                    ArrayList<List<LatLng>> multiPath = new ArrayList<>();
                    ArrayList<LatLng> tmpPath = new ArrayList<>(1000);
                    int cnt=0;
                    while(!pointIndex.isEmpty()) {
                        try {
                            if(pointIndex.get(0)+pointCount.get(0) == cnt) {
                                multiPath.add((List<LatLng>) tmpPath.clone());
                                tmpPath.clear();
                                pointCount.remove(0);
                                pointIndex.remove(0);
                                Congestion.add(congestion.get(0));
                                congestion.remove(0);
                            } else if (pointIndex.get(0) == cnt) {
                                multiPath.add((List<LatLng>) tmpPath.clone());
                                tmpPath.clear();
                                Congestion.add(0);
                            }
                            if (pointIndex.get(0)+pointCount.get(0) >= cnt && cnt >= pointIndex.get(0)) {
                                tmpPath.add(Loot.get(0));
                                Loot.remove(0);
                                cnt++;
                            } else if(pointIndex.get(0)+pointCount.get(0) > cnt) {
                                tmpPath.add(Loot.get(0));
                                Loot.remove(0);
                                cnt++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    multiPath.add(Loot);
                    Congestion.add(1);

                    loot = multiPath;
                    return div;
                } else if (div == 1) {
                    SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    indexFirst = stringBuilder.indexOf("\"currentDateTime\":");
                    indexLast = stringBuilder.indexOf("\"route");

                    Date currentTime = (Date) DateFormat.parse(stringBuilder.substring(indexFirst+19,indexLast-2).replace("T"," "));
                    System.out.println(currentTime);

                    indexFirst = stringBuilder.indexOf("\"duration\":");
                    indexLast = stringBuilder.indexOf("\"etaService");

                    double arrivalTime = Integer.parseInt(stringBuilder.substring(indexFirst+11,indexLast-1))/1000;

                    TextView tmptxt =  findViewById(R.id.textTime);

                    long hour;
                    long minute = Math.round(arrivalTime/60);
                    long second = Math.round(arrivalTime%60);
                    if(minute >= 60) {
                        hour = Math.round(minute/60);
                        minute = Math.round(minute%60);
                        tmptxt.setText("예상 소요 시간 : "+hour+"시간 "+minute+"분 "+second+"초");
                    } else { tmptxt.setText("예상 소요 시간 : "+minute+"분 "+second+"초"); }




                    return Integer.parseInt(String.valueOf(arrivalTime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return div;
    }


    private void cameraSet(double x,double y) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                new LatLng(x, y),13).animate(CameraAnimation.Easing);
        naverMap.moveCamera(cameraUpdate);
    }

    private void drawPath(List<List<LatLng>> Loot) {
        try {
            PathOverlay path2 = new PathOverlay();
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
                }
            }
            path2.setCoords(loot2);
            path2.setColor(Color.parseColor("#AAAAAA"));
            path2.setOutlineWidth(0);
            path2.setMap(naverMap);
            path.setColorParts(colorParts);
            path.setMap(naverMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

