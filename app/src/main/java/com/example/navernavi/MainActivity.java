package com.example.navernavi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static NaverMap naverMap;
    private Marker marker1 = new Marker();
    private Marker marker2 = new Marker();

    private double l1, l2;  //모든 함수에서 좌표값을 사용하기 위한 location 전역변수

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
                    requestGeocoding();
                }).start();
                setMark(marker1, l1, l2, com.naver.maps.map.R.drawable.navermap_default_marker_icon_red);
            }
        });
        Button btnMark2 = (Button) findViewById(R.id.btnmark2);
        btnMark2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    requestGeocoding();
                }).start();
                setMark(marker2, l1, l2, com.naver.maps.map.R.drawable.navermap_default_marker_icon_green);
            }
        });
        Button lootBtn = (Button) findViewById(R.id.lootGen);
        lootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDirection();
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

            cameraSet();
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

    private void requestGeocoding() {
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

                l1 = Double.parseDouble(y);
                l2 = Double.parseDouble(x);

                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestDirection() {
        try {


            PathOverlay path = new PathOverlay();
            path.setColor(Color.YELLOW);
            path.setCoords(Arrays.asList(
                    new LatLng(37.55225816292672, 126.95692239769714),
                    new LatLng(37.55091994383829, 126.95627811882665),
                    new LatLng(37.55077141892128, 126.95665731262389),
                    new LatLng(37.54864019679784, 126.95568534439954),
                    new LatLng(37.54819922377543, 126.95708316098926),
                    new LatLng(37.5473030517265, 126.95797764697222),
                    new LatLng(37.54697398450087, 126.95741202900021)
            ));
            path.setMap(naverMap);
            double loc1,loc2;
            loc1 = (37.55225816292672+37.54697398450087)/2;
            loc2 = (126.95692239769714+126.95741202900021)/2;
            CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                    new LatLng(loc1, loc2),15).animate(CameraAnimation.Easing);
            if(l1 != 0){ naverMap.moveCamera(cameraUpdate); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cameraSet() {
        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(
                new LatLng(l1, l2),18).animate(CameraAnimation.Easing);
        if(l1 != 0){ naverMap.moveCamera(cameraUpdate); }
    }
}

