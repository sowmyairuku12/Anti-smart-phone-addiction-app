package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.antismartphoneaddictionapp.Utility.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("SetTextI18n")
public class RestrictedAppActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvAppName;
    private AppCompatButton btnGetOTP, btnOk;
    private TextInputLayout ilOPT;
    private TextInputEditText etOpt;
    String appName, packageName;
    int restrictedAppId;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restricted_app);

        initUI();
        initListener();
        initObj();
    }

    private void initUI() {
        tvAppName = findViewById(R.id.tvAppName);
        btnGetOTP = findViewById(R.id.btnGetOTP);
        ilOPT = findViewById(R.id.ilOPT);
        etOpt = findViewById(R.id.etOpt);
        btnOk = findViewById(R.id.btnOk);
        appName = getIntent().getStringExtra("APP_NAME");
        packageName = getIntent().getStringExtra("PACKAGE_NAME");
        restrictedAppId = getIntent().getIntExtra("APP_ID", -1);

        if (appName != null) {
            tvAppName.setText("Access to " + appName + " is restricted!"); // Update UI
        }
    }

    private void initListener() {
        btnGetOTP.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    private void initObj() {
        db = new DatabaseHandler(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnOk) {
            onClickBtnOk();
        } else if (id == R.id.btnGetOTP)
            onClickBtnGetOPT();
    }

    private void onClickBtnOk() {
        long opt = Long.parseLong(etOpt.getText().toString().trim());
        if (opt == Constants.OPT) {
            //call database method to update the TempUnlockExpiryTime from current date time + 2 min
            //also create method in db to update TempUnlockExpiryTime where id = ?
            updateTempUnlockExpiryTime(restrictedAppId);
        } else {
            Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show();

        }
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }


    private void onClickBtnGetOPT() {
        ilOPT.setVisibility(View.VISIBLE);
        btnOk.setVisibility(View.VISIBLE);
    }

    private void updateTempUnlockExpiryTime(int appId) {
        // Call the database method to update the TempUnlockExpiryTime
        db.updateTempUnlockExpiryTime(appId);
        Toast.makeText(this, "Temporary unlock successful", Toast.LENGTH_SHORT).show();
    }
    long parseTime(String expiryTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(expiryTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}