package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        Button SaveBtn = (Button) findViewById(R.id.Save);
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("Category",MODE_PRIVATE);
                StringBuilder input = new StringBuilder();

                EditText name = (EditText) findViewById(R.id.nameInput);
                EditText  Dep = (EditText) findViewById(R.id.departureInput);
                EditText  Arv = (EditText) findViewById(R.id.arrivalInput);
                String key = String.valueOf(sharedPreferences.getAll().size());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(name.length()>0 & Dep.length()>0 & Arv.length()>0) {
                    input.append(name.getText().toString()).append(":");
                    input.append(Dep.getText().toString()).append(":");
                    input.append(Arv.getText().toString()).append(":");

                    editor.putString(key,input.toString());
                    editor.commit();
                    Toast.makeText(getApplicationContext(),"새로운 경로 카테고리가 생성되었습니다",Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),"빈 칸 없이 모두 입력해주세요",Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 맵 버튼
//        Button pageTransBtn = (Button) findViewById(R.id.btnMap);
//        pageTransBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                startActivity(intent);
//            }
//        });


    }
}


