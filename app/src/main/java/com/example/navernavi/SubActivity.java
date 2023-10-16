package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.navernavi.retrofit.AddrSearchRepository;
import com.example.navernavi.retrofit.AddrSearchService;
import com.example.navernavi.retrofit.Location;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SubActivity extends AppCompatActivity {


    private String[] Addr = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        EditText  Dep = (EditText) findViewById(R.id.departureInput);
        EditText  Arv = (EditText) findViewById(R.id.arrivalInput);
        Button SaveBtn = (Button) findViewById(R.id.Save);
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
                StringBuilder input = new StringBuilder();

                EditText name = (EditText) findViewById(R.id.nameInput);
                String key = String.valueOf(sharedPreferences.getAll().size());
                for(int i = 0;sharedPreferences.getAll().size()>i;i++) {
                    if(!sharedPreferences.contains(i+"")) {
                        key = i+"";
                    }
                }
                AddrSearchRepository.getINSTANCE().getAddressList(Dep.getText().toString(), 1, 1, new AddrSearchRepository.AddressResponseListener() {
                    @Override
                    public void onSuccessResponse(Location locationData) {
                        if(1 > locationData.documentsList.size()) {
                            Toast.makeText(getApplicationContext(),"출발지가 존재하지 않습니다 다시 입력해주세요",Toast.LENGTH_SHORT).show();
                            Dep.setText("");
                        } else {
                            AddrSearchRepository.getINSTANCE().getAddressList(Arv.getText().toString(), 1, 1, new AddrSearchRepository.AddressResponseListener() {
                                @Override
                                public void onSuccessResponse(Location locationData) {
                                    if(1 > locationData.documentsList.size()) {
                                        Toast.makeText(getApplicationContext(),"도착지가 존재하지 않습니다 다시 입력해주세요",Toast.LENGTH_SHORT).show();
                                        Arv.setText("");
                                    } else {
                                        save(name.getText().toString(),Dep.getText().toString(),Arv.getText().toString());
                                    }
                                }
                                @Override
                                public void onFailResponse() {
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailResponse() {
                    }
                });

            }
        });

        InputMethodManager imm = (InputMethodManager)  getSystemService(INPUT_METHOD_SERVICE);
        Dep.setImeOptions(EditorInfo.IME_ACTION_DONE);
        Dep.setOnEditorActionListener((v, actionId, event) -> {
            request(Dep.getText().toString(),0);
            return false;
        });
        Arv.setOnEditorActionListener((v, actionId, event) -> {
            request(Arv.getText().toString(),1);
            return false;
        });

        // 맵 버튼
        Button pageTransBtn = (Button) findViewById(R.id.btnMap);
        pageTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayout Background = (LinearLayout) findViewById(R.id.SubBackground);
        Background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameInput = (EditText)findViewById(R.id.nameInput);
                EditText depInput = (EditText)findViewById(R.id.departureInput);
                EditText arvInput = (EditText)findViewById(R.id.arrivalInput);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(nameInput.getWindowToken(),0);
                imm.hideSoftInputFromWindow(depInput.getWindowToken(),0);
                imm.hideSoftInputFromWindow(arvInput.getWindowToken(),0);

                nameInput.clearFocus();
                depInput.clearFocus();
                arvInput.clearFocus();
            }
        });
    }

    private void save(String name,String Dep,String Arv) {
        SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
        StringBuilder input = new StringBuilder();


        String key = String.valueOf(sharedPreferences.getAll().size());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(name.length()>0 & Dep.length()>0 & Arv.length()>0) {
            input.append(name).append(":");
            input.append(Dep).append(":");
            input.append(Arv).append(":");
            input.append(Addr[0]).append(":");
            input.append(Addr[1]).append(":");

            editor.putString(key,input.toString());
            editor.commit();
            Toast.makeText(getApplicationContext(),"새로운 경로 카테고리가 생성되었습니다",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SubActivity.this,UserActivity.class);
            startActivity(intent);
            finish();
        }
    }
    public void request(String addr,int div) {
        AddrSearchRepository.getINSTANCE().getAddressList(addr, 1, 10, new AddrSearchRepository.AddressResponseListener() {
            @Override
            public void onSuccessResponse(Location locationData) {
                try {
                    List<Location.Document> documents = locationData.documentsList;
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.contentLinearSub);
                    linearLayout.removeAllViews();
                    SlidingDrawer slidingDrawer = (SlidingDrawer)findViewById(R.id.slidingDrawerSub);
                    ArrayList<Place> placeList = new ArrayList<>();
                    slidingDrawer.setVisibility(View.VISIBLE);
                    slidingDrawer.animateOpen();

                    for(int i = 0;documents.size()>i;i++) {
                        placeList.add(new Place(getApplicationContext()));
                        linearLayout.addView(placeList.get(i));


                        TextView placeName = (TextView) placeList.get(i).findViewById(R.id.placeName);
                        TextView placeAddr = (TextView) placeList.get(i).findViewById(R.id.placeAddr);
                        TextView placeType = (TextView) placeList.get(i).findViewById(R.id.placeType);
                        TextView placeDistance = (TextView) placeList.get(i).findViewById(R.id.placeDistance);
                        LinearLayout place = (LinearLayout) placeList.get(i).findViewById(R.id.userSubLayout);

                        placeName.setText(documents.get(i).getPlace_name());
                        placeAddr.setText(documents.get(i).getAddress_name());
                        String type[] = documents.get(i).getCategory_name().toString().split(">");
                        placeType.setText(type[type.length-1]);

                        placeDistance.setVisibility(View.GONE);


                        place.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (div == 0) {
                                    EditText depInput = (EditText)findViewById(R.id.departureInput);
                                    depInput.setText(placeName.getText());
                                    Addr[0] = placeAddr.getText().toString();
                                } else {
                                    EditText arvInput = (EditText)findViewById(R.id.arrivalInput);
                                    arvInput.setText(placeName.getText());
                                    Addr[1] = placeAddr.getText().toString();
                                }
                                slidingDrawer.animateClose();
                                slidingDrawer.setVisibility(View.GONE);
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailResponse() {
            }
        });
    }

}




