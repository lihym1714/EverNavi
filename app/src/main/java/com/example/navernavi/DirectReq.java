package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectReq extends AppCompatActivity {

    public int requestDirect(int div, String depart, String arrival) {
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
                if(div == 0) { //0 = 루트 생성 , 1 = 트래픽 검색
                    ArrayList<LatLng> Loot = new ArrayList<>();
                    ArrayList<Integer> pointIndex = new ArrayList<>();
                    ArrayList<Integer> pointCount = new ArrayList<>();

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
                    for(int i = 0; coord.length > i; i+=2) {
                        Double x,y;
                        y = Double.parseDouble((coord[i].replace("[","")));
                        x = Double.parseDouble((coord[i+1].replace("]","")));
                        Loot.add(new LatLng(x,y));
                    }

                    ArrayList<ArrayList> multiPath = new ArrayList<>();
                    ArrayList<LatLng> tmpPath = new ArrayList<>();
                    int cnt=0;
                    while(!pointIndex.isEmpty()) {
                        try {
                            if(pointIndex.get(0)+pointCount.get(0) == cnt) {
                                multiPath.add(tmpPath);
                                tmpPath.clear();
                                pointCount.remove(0);
                                pointIndex.remove(0);
                            } else if (pointIndex.get(0) == cnt) {
                                multiPath.add(tmpPath);
                                tmpPath.clear();
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
                            break;
                        }
                    }
                    multiPath.add(Loot);
                    System.out.println(multiPath);

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

                    UserActivity user = new UserActivity();

                    return Integer.parseInt(String.valueOf(arrivalTime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return div;
    }
}
