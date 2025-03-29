package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

@SuppressLint("SetTextI18n")
public class RestrictedAppActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvAppName;
    private AppCompatButton btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restricted_app);

        initUI();
        initListener();
    }

    private void initUI() {
        tvAppName = findViewById(R.id.tvAppName);
        btnOk = findViewById(R.id.btnOk);
        String appName = getIntent().getStringExtra("APP_NAME");

        if (appName != null) {
            tvAppName.setText("Access to " + appName + " is restricted!"); // Update UI
        }
    }

    private void initListener(){
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.btnOk){
            onClickBtnOk();
        }
    }

    private void onClickBtnOk(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}