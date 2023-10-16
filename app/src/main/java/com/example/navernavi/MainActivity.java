package com.example.navernavi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Slide;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navernavi.inflate.Place;
import com.example.navernavi.inflate.WaypointEdit;
import com.example.navernavi.retrofit.AddrSearchRepository;
import com.example.navernavi.retrofit.Location;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG="MainActivity";

    private static NaverMap naverMap;
    private final Marker markerDep = new Marker();
    private final Marker markerArv = new Marker();
    private ArrayList<Marker> markerSet = new ArrayList<>();
    private boolean isView = false;
    private List<List<LatLng>> loot;
    private List<Integer> Congestion = new ArrayList<>();
    private ArrayList<WaypointEdit> WaypointList = new ArrayList<>();
    private int focused = 5;
    private MultipartPathOverlay path = new MultipartPathOverlay();

    private LatLng tmpLoc;
    private String intentText;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private final int RedPing = com.naver.maps.map.R.drawable.navermap_default_marker_icon_red;
    private final int GreenPing = com.naver.maps.map.R.drawable.navermap_default_marker_icon_green;
    private final int BluePing = com.naver.maps.map.R.drawable.navermap_default_marker_icon_blue;
    private final int YellowPing = com.naver.maps.map.R.drawable.navermap_default_marker_icon_yellow;

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
//        cardBar2.setVisibility(View.INVISIBLE);

        SlidingDrawer slidingDrawer = (SlidingDrawer)findViewById(R.id.slidingDrawer);
        slidingDrawer.setVisibility(View.INVISIBLE);


        //경유지 추가를 위한 EditText파트
        LinearLayout WaypointEditLayout = (LinearLayout)findViewById(R.id.cardViewEdit);
        Button AddWaypointBtn = (Button)findViewById(R.id.AddWaypointBtn);
        AddWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (5 > WaypointList.size()) {
                        if (WaypointList.isEmpty()) {
                            WaypointList.add(new WaypointEdit(getApplicationContext()));
                            WaypointEditLayout.addView(WaypointList.get(WaypointList.size() - 1));
                            markerSet.add(new Marker());
                        }else if (markerSet.size() > 0) {
                            if (markerSet.get(markerSet.size() - 1).isAdded())
                                WaypointList.add(new WaypointEdit(getApplicationContext()));
                            WaypointEditLayout.addView(WaypointList.get(WaypointList.size() - 1));
                            markerSet.add(new Marker());
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                for(int i = 0;WaypointList.size()>i;i++) {
                    int finalI = i;
                    EditText searchbar = (EditText) WaypointList.get(i).findViewById(R.id.WayPointEdit);
                    LinearLayout waypointLayout = (LinearLayout)WaypointList.get(i).findViewById(R.id.WayPointLayout);
                    Button btnDel = (Button) WaypointList.get(i).findViewById(R.id.waypDeleteBtn);
                    btnDel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WaypointList.remove(finalI);
                            WaypointEditLayout.removeViewAt(finalI);
                            markerSet.get(finalI).setMap(null);
                            markerSet.remove(finalI);
                            focused = 5;
                        }
                    });
                    EditText searchBar = (EditText)WaypointList.get(i).findViewById(R.id.WayPointEdit);
                    try {
                        searchBar.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        searchBar.setOnEditorActionListener((View, actionId, event) -> {
                            if(searchBar.length() > 0) {
                                try {
                                    request(searchBar.getText().toString(),0);
                                    InputMethodManager imm;
                                    imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                                    focused = finalI;
                                    searchbar.clearFocus();
                                    slidingDrawer.setVisibility(android.view.View.VISIBLE);
                                    slidingDrawer.animateOpen();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                                } else {
                                    Toast.makeText(getApplicationContext(),"주소지를 입력해주세요",Toast.LENGTH_SHORT).show();
                                }
                            return false;
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    markerSet.get(i).setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {
                            Button waypointFix = (Button) findViewById(R.id.wayPointFix);
                            waypointFix.setVisibility(View.VISIBLE);
                            cameraSet(markerSet.get(finalI).getPosition(),0);
                            markerSet.get(finalI).setMap(null);
                            Marker movingMarker = new Marker();
                            setMark(movingMarker,markerSet.get(finalI).getPosition(),BluePing);

                            naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
                                @Override
                                public void onCameraChange(int i, boolean b) {
                                    if(movingMarker.isAdded()) setMark(movingMarker,naverMap.getCameraPosition().target,BluePing);
                                }
                            });

                            waypointFix.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    setMark(markerSet.get(finalI),movingMarker.getPosition(),BluePing);
                                    movingMarker.setMap(null);
                                    waypointFix.setVisibility(View.GONE);
                                }
                            });
                            return false;
                        }
                    });
                }
            }
        });


        Intent userIntent = getIntent();
        String text = userIntent.getStringExtra("key");
        TextView depAddr = (TextView)findViewById(R.id.departureAddr);
        TextView arvAddr = (TextView)findViewById(R.id.arrivalAddr);
        depAddr.setText(text.split(":")[2]);
        arvAddr.setText(text.split(":")[3]);
        intentText = text;
        LinearLayout lootGenLayout = (LinearLayout) findViewById(R.id.lootGenLayout);
        request(text.split(":")[2],1); //dep , arv  Marker setMap


        Button lootDel = (Button) findViewById(R.id.lootDel);
        lootDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("경로 삭제");
                ad.setMessage("정말로 해당 경로를 삭제하시겠습니까?");
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(intentText.split(":")[0].contains("Only")) {
                            SharedPreferences SP = getSharedPreferences(intentText.split(":")[1],MODE_PRIVATE);
                            SharedPreferences.Editor editor = SP.edit();
                            editor.remove(intentText.split(":")[4]);
                            editor.commit();
                        } else {
                            SharedPreferences SP = getSharedPreferences("Category",MODE_PRIVATE);
                            SharedPreferences.Editor editor = SP.edit();
                            String spString = SP.getString(intentText.split(":")[4],"");
                            SharedPreferences SP2 = getSharedPreferences(spString,MODE_PRIVATE);
                            SharedPreferences.Editor editor2 = SP2.edit();
                            editor.remove(intentText.split(":")[4]);
                            editor.commit();
                            editor2.clear();
                            editor2.commit();
                        }
                        Intent intent = new Intent(MainActivity.this,UserActivity.class);
                        startActivity(intent);
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                ad.show();
            }
        });

        Button btnBack = (Button)findViewById(R.id.btnMap);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button lootGen = (Button) findViewById(R.id.lootGen);
        lootGen.setOnClickListener(view -> {
            String waypoint = "";
            double depX = markerDep.getPosition().longitude;
            double depY = markerDep.getPosition().latitude;
            double arvX = markerArv.getPosition().longitude;
            double arvY = markerArv.getPosition().latitude;

            for(int i = 0;WaypointList.size()>i;i++) {
                LatLng latLng = markerSet.get(i).getPosition();
                waypoint += latLng.longitude+","+latLng.latitude+"|";
            }
            String finalWaypoint = waypoint;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (loot != null) {
                        path.setMap(null);
                        cardBar2.setVisibility(View.VISIBLE);
                        drawPath(loot,1);
                    }
                }
            };
            class NewRunnable implements Runnable {
                @Override
                public void run() {
                    try {
                        requestDirect(0, depX + "," + depY, arvX + "," + arvY, finalWaypoint);
                        LatLng avgLoc = new LatLng((depY+arvY)/2,(depX+arvX)/2);
                        mHandler.post(runnable);
                        cameraSet(avgLoc,2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                            Intent intent = new Intent(MainActivity.this,UserActivity.class);
                            startActivity(intent);
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
        LinearLayout appBar = (LinearLayout) findViewById(R.id.appBar);
        if(text.split(":")[0].contains("viewLootOnly")) {
            SharedPreferences lootShared = getSharedPreferences(text.split(":")[1],MODE_PRIVATE);
            String[] lootString = lootShared.getString(text.split(":")[4]+"","").split(":");
            lootGen.setVisibility(View.GONE);
            cardBar2.setVisibility(View.GONE);
            isView = true;
            appBar.setVisibility(View.VISIBLE);
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    drawPath(loot,1);
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
            lootGen.setVisibility(View.GONE);
            cardBar2.setVisibility(View.GONE);
            isView = true;
            appBar.setVisibility(View.VISIBLE);
            try {
                SharedPreferences lootShared = getSharedPreferences(text.split(":")[1],MODE_PRIVATE);
                for(int i = 0;lootShared.getAll().size()>i;i++) {
                    String[] lootString = lootShared.getString(i+"","").split(":");
                    //lootString [0] = name / [1] = dep / [2] = arv / [3] = waypoint
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            drawPath(loot,0);
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
        } else {
            lootDel.setVisibility(View.GONE);
        }
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        MainActivity.naverMap = naverMap;

        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setRotateGesturesEnabled(false);
        naverMap.getUiSettings().setZoomControlEnabled(false);
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Navi);
        //건물 표시
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);

        naverMap.setOnMapClickListener((pointF, latLng) -> {
            InputMethodManager imm;
            imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
            if(!isView) {
                tmpLoc = latLng;
                if (5>focused) {
                    EditText searchBar = (EditText) WaypointList.get(focused).findViewById(R.id.WayPointEdit);
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                    if (slidingDrawer.isOpened()) {
                        slidingDrawer.animateClose();
                    }
                    else if (searchBar.hasFocus()) {
                        setMark(markerSet.get(focused), tmpLoc, BluePing);
                    }
                }
            }
        });
    }
    public void request(String addr,int div) {
        int size = 10; if (div != 0) size = 1;
        AddrSearchRepository.getINSTANCE().getAddressList(addr, 1, size, new AddrSearchRepository.AddressResponseListener() {
            @Override
            public void onSuccessResponse(Location locationData) {
                if (div == 0) {
                    try {
                        List<Location.Document> documents = locationData.documentsList;
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.contentLinear);
                        linearLayout.removeAllViews();
                        ArrayList<Place> placeList = new ArrayList<>();

                        for(int i = 0;documents.size()>i;i++) {
                            placeList.add(new Place(getApplicationContext()));
                            linearLayout.addView(placeList.get(i));
                            SlidingDrawer slidingDrawer = (SlidingDrawer)findViewById(R.id.slidingDrawer);


                            TextView placeName = (TextView) placeList.get(i).findViewById(R.id.placeName);
                            TextView placeAddr = (TextView) placeList.get(i).findViewById(R.id.placeAddr);
                            TextView placeType = (TextView) placeList.get(i).findViewById(R.id.placeType);
                            TextView placeDistance = (TextView) placeList.get(i).findViewById(R.id.placeDistance);
                            LinearLayout place = (LinearLayout) placeList.get(i).findViewById(R.id.userSubLayout);

                            placeName.setText(documents.get(i).getPlace_name());
                            placeAddr.setText(documents.get(i).getAddress_name());
                            String type[] = documents.get(i).getCategory_name().toString().split(">");
                            placeType.setText(type[type.length-1]);

                            double disX = markerDep.getPosition().latitude; double disY = markerDep.getPosition().longitude;
                            if (locationSource.getLastLocation() != null) {
                                disX = locationSource.getLastLocation().getLatitude();
                                disY = locationSource.getLastLocation().getLongitude();
                            }

                            double distance = getDistance(
                                    disX,
                                    disY,
                                    Double.parseDouble(documents.get(i).getY()),
                                    Double.parseDouble(documents.get(i).getX())
                            );
                            if(distance > 0.1) {placeDistance.setText(Math.round((distance)*10)/10.0+ " km");}
//                            placeDistance.setText(distance+"");


                            int finalI = i;
                            place.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    double x = Double.parseDouble(documents.get(finalI).getY());
                                    double y = Double.parseDouble(documents.get(finalI).getX());
                                    LatLng loc = new LatLng(x,y);
                                    setMark(markerSet.get(focused),loc, BluePing);
                                    slidingDrawer.animateClose();
                                    cameraSet(loc,0);
                                }
                            });

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    double y = Double.parseDouble(locationData.documentsList.get(0).getX());
                    double x = Double.parseDouble(locationData.documentsList.get(0).getY());
                    if(!markerDep.isAdded()) {
                        setMark(markerDep, new LatLng(x, y), RedPing);
                        tmpLoc = new LatLng(x, y);
                        request(intentText.split(":")[3],1);
                    } else {
                        setMark(markerArv, new LatLng(x, y), GreenPing);
                        cameraSet(new LatLng((tmpLoc.latitude+x)/2,(tmpLoc.longitude+y)/2),2);
                    }
                }
            }
            @Override
            public void onFailResponse() {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Double getDistance(Double x1, Double y1, Double x2, Double y2) {
        double distance;
        double radius = 6371;
        double toRadian = Math.PI/180;

        double deltaLatitude = Math.abs(x1 - x2) * toRadian;
        double deltaLongitude = Math.abs(y1 - y2) * toRadian;

        double sinDeltaLat = Math.sin(deltaLatitude / 2);
        double sinDeltaLng = Math.sin(deltaLongitude / 2);
        double squareRoot = Math.sqrt(
                sinDeltaLat * sinDeltaLat +
                        Math.cos(x1 * toRadian) * Math.cos(x2 * toRadian) * sinDeltaLng * sinDeltaLng);

        distance = 2 * radius * Math.asin(squareRoot);

        return distance;
    }



    @SuppressLint("SetTextI18n")
    public void requestDirect(int div, String Depart, String Arrival, String waypoints) {
        try {

            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String[] option = {"trafast","tracomfort","traoptimal","traavoidtoll","traavoidcaronly"};
            StringBuilder query = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=" + Depart + "&goal=" + Arrival);
            query.append("&").append(option[0]);
            if(!waypoints.equals("")) {
                query.append("&waypoints=").append(waypoints);
            }
            URL url = new URL(query.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", Const.Client_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", Const.Client_Secret);
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

    private void drawPath(List<List<LatLng>> Loot, int clear) {
        try {
            if(clear == 0) {path = new MultipartPathOverlay();}
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
        double depX = markerDep.getPosition().latitude;
        double depY = markerDep.getPosition().longitude;

        double arvX = markerArv.getPosition().latitude;
        double arvY = markerArv.getPosition().longitude;
        StringBuilder data = new StringBuilder();
        data.append(name).append(":");
        data.append(depY+","+depX).append(":");
        data.append(arvY+","+arvX).append(":");
        for(int i = 0;markerSet.size()>i;i++) {
            data.append(markerSet.get(i).getPosition().longitude+","+markerSet.get(i).getPosition().latitude).append("|");
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setMessage("정말로 경로 생성을 취소하고 돌아가시겠습니까?");
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        if(!isView) ad.show();
        else finish();
    }
}

