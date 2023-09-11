package com.example.navernavi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
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
import com.naver.maps.map.overlay.ArrowheadPathOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static NaverMap naverMap;
    private Marker marker1 = new Marker();
    private Marker marker2 = new Marker();
    ArrayList<LatLng> loot;

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
                    requestGeocoding(1);
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
                    requestGeocoding(2);
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
                    requestDirection();
                }).start();
                drawPath(loot);

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

    private void requestGeocoding(int div) {
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

    private void requestDirection() {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();

            String depart = String.valueOf(depY)+","+String.valueOf(depX);
            String arrival = String.valueOf(arvY)+","+String.valueOf(arvX);
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

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) { //200
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                }

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                ArrayList<LatLng> Loot = new ArrayList<>();

                int indexFirst = stringBuilder.indexOf("\"path\":");
                int indexLast = stringBuilder.indexOf(",\"section\"");
                String coord = stringBuilder.substring(indexFirst + 8,indexLast-1);
                String[] s = (coord.split(","));
                int lenCoord = (int) s.length;

                for(int i = 0; lenCoord > i; i+=2) {
                    Double x,y;
                    y = Double.parseDouble((s[i].replace("[","")));
                    x = Double.parseDouble((s[i+1].replace("]","")));
                    Loot.add(new LatLng(x,y));
                }

                loot = Loot;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }





//    private void requestDirectionTest() {
//        try {
//
//            BufferedReader bufferedReader;
//            StringBuilder stringBuilder = new StringBuilder("{\"code\":0,\"message\":\"길찾기를 성공하였습니다.\",\"currentDateTime\":\"2023-09-11T10:17:27\",\"route\":{\"traoptimal\":[{\"summary\":{\"start\":{\"location\":[126.9568537,37.5524898]},\"goal\":{\"location\":[126.9573631,37.5470426],\"dir\":2},\"distance\":2175,\"duration\":452568,\"etaServiceType\":0,\"departureTime\":\"2023-09-11T10:17:27\",\"bbox\":[[126.9544882,37.5467652],[126.9594446,37.5572770]],\"tollFare\":0,\"taxiFare\":5700,\"fuelPrice\":272},\"path\":[[126.9567690,37.5523019],[126.9566545,37.5523347],[126.9565955,37.5523516],[126.9564071,37.5524057],[126.9562143,37.5524616],[126.9563369,37.5527181],[126.9565813,37.5532078],[126.9566905,37.5534463],[126.9571415,37.5543335],[126.9571728,37.5543931],[126.9572487,37.5545422],[126.9573156,37.5546858],[126.9573323,37.5547210],[126.9575173,37.5551212],[126.9575619,37.5552160],[126.9576511,37.5554057],[126.9577484,37.5555783],[126.9579543,37.5559082],[126.9580272,37.5560086],[126.9581317,37.5561191],[126.9584611,37.5564415],[126.9586499,37.5566244],[126.9589740,37.5569089],[126.9590392,37.5569651],[126.9591011,37.5570149],[126.9591405,37.5570440],[126.9594446,37.5572770],[126.9591405,37.5570440],[126.9591011,37.5570149],[126.9590392,37.5569651],[126.9589740,37.5569089],[126.9586499,37.5566244],[126.9584611,37.5564415],[126.9584116,37.5563935],[126.9581317,37.5561191],[126.9580272,37.5560086],[126.9579543,37.5559082],[126.9577484,37.5555783],[126.9576511,37.5554057],[126.9575619,37.5552160],[126.9575173,37.5551212],[126.9573323,37.5547210],[126.9573156,37.5546858],[126.9572487,37.5545422],[126.9571728,37.5543931],[126.9571415,37.5543335],[126.9566905,37.5534463],[126.9565813,37.5532078],[126.9563369,37.5527181],[126.9562143,37.5524616],[126.9561841,37.5524038],[126.9561640,37.5523649],[126.9561249,37.5522917],[126.9559152,37.5518626],[126.9558907,37.5518138],[126.9555773,37.5511544],[126.9554267,37.5508500],[126.9553876,37.5507759],[126.9553251,37.5506638],[126.9552881,37.5506033],[126.9552512,37.5505391],[126.9552245,37.5504867],[126.9551597,37.5503557],[126.9550750,37.5501786],[126.9548038,37.5496366],[126.9546176,37.5492499],[126.9544882,37.5489816],[126.9546312,37.5489354],[126.9546607,37.5489275],[126.9548150,37.5488867],[126.9555330,37.5486854],[126.9555682,37.5486748],[126.9556499,37.5486481],[126.9559223,37.5485565],[126.9561742,37.5484666],[126.9562570,37.5484463],[126.9564340,37.5483912],[126.9566712,37.5483175],[126.9568992,37.5482491],[126.9570367,37.5481831],[126.9570925,37.5481338],[126.9571939,37.5480459],[126.9572497,37.5479984],[126.9574617,37.5477993],[126.9575988,37.5476241],[126.9576365,37.5475747],[126.9577553,37.5474274],[126.9578410,37.5473251],[126.9578947,37.5472613],[126.9579255,37.5472254],[126.9577871,37.5471031],[126.9576938,37.5470198],[126.9574182,37.5467769],[126.9574058,37.5467652],[126.9572712,37.5469078],[126.9572297,37.5470068],[126.9572262,37.5470212]],\"section\":[{\"pointIndex\":4,\"pointCount\":63,\"distance\":1645,\"name\":\"마포대로\",\"congestion\":2,\"speed\":20},{\"pointIndex\":66,\"pointCount\":24,\"distance\":376,\"name\":\"마포대로14길\",\"congestion\":3,\"speed\":11},{\"pointIndex\":89,\"pointCount\":5,\"distance\":69,\"name\":\"만리재로\",\"congestion\":3,\"speed\":19}],\"guide\":[{\"pointIndex\":4,\"type\":3,\"instructions\":\"'마포대로' 방면으로 우회전\",\"distance\":52,\"duration\":11308},{\"pointIndex\":26,\"type\":6,\"instructions\":\"아현교차로에서 유턴\",\"distance\":614,\"duration\":84136},{\"pointIndex\":66,\"type\":2,\"instructions\":\"'마포대로14길' 방면으로 좌회전\",\"distance\":1031,\"duration\":198669},{\"pointIndex\":89,\"type\":3,\"instructions\":\"'만리재로' 방면으로 우회전\",\"distance\":376,\"duration\":115684},{\"pointIndex\":93,\"type\":3,\"instructions\":\"'만리재옛2길' 방면으로 우회전\",\"distance\":69,\"duration\":13072},{\"pointIndex\":96,\"type\":88,\"instructions\":\"목적지\",\"distance\":33,\"duration\":29699}]}]}}");
//
//
////            String depart = "126.9568537,37.5524898";
////            String arrival = "126.9573631,37.5470426";
////            String query = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+depart+"&goal="+arrival;
////            URL url = new URL(query);
////            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
////            if (conn != null) {
////                conn.setConnectTimeout(5000);
////                conn.setReadTimeout(5000);
////                conn.setRequestMethod("GET");
////                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "52qqm2ev4e");
////                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "xTdW0pV93xz6x9ZM948xmH4iGvpheQZwmKwx0PjM");
////                conn.setDoInput(true);
////
////                int responseCode = conn.getResponseCode();
////
////                if (responseCode == 200) { //200
////                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////                } else {
////                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////                }
//
////                String line = null;
////                while ((line = bufferedReader.readLine()) != null) {
////                    stringBuilder.append(line + "\n");
////
////                }
//            ArrayList<LatLng> Loot = new ArrayList<>();
//
//            int indexFirst = stringBuilder.indexOf("\"path\":");
//            int indexLast = stringBuilder.indexOf(",\"section\"");
//            String coord = stringBuilder.substring(indexFirst + 8,indexLast-1);
//            String[] s = (coord.split(","));
//            int lenCoord = (int) s.length;
//
//            for(int i = 0; lenCoord > i; i+=2) {
//                Double x,y;
//                y = Double.parseDouble((s[i].replace("[","")));
//                x = Double.parseDouble((s[i+1].replace("]","")));
//                Loot.add(new LatLng(x,y));
//            }
//
//            loot = Loot;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    private void cameraSet(double x,double y) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                new LatLng(x, y),18).animate(CameraAnimation.Easing);
        naverMap.moveCamera(cameraUpdate);
    }

    private void drawPath(ArrayList<LatLng> Loot) {
        try {
            PathOverlay path = new PathOverlay();
            path.setCoords(Loot);
            path.setColor(Color.YELLOW);
            path.setMap(naverMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

