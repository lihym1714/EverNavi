package com.example.navernavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        // 맵 버튼
        Button pageTransBtn = (Button) findViewById(R.id.btn2);
        pageTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼
        Button loginBtn = (Button) findViewById(R.id.Login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextID = (EditText) findViewById(R.id.loginID);
                EditText editTextPW = (EditText) findViewById(R.id.loginPW);
                String ID = new String(editTextPW.getText().toString());
                String PW = new String(editTextID.getText().toString());
                if (ID.equals(PW)) {
                    Intent intent = new Intent(getApplicationContext(),UserActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button registerBtn = (Button) findViewById(R.id.Register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}


