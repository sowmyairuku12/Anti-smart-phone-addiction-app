package com.example.antismartphoneaddictionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.antismartphoneaddictionapp.Utility.Constants;
import com.example.antismartphoneaddictionapp.Utility.Helper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;


@SuppressLint("SetTextI18n")
public class RestrictedAppActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "RestrictedAppActivity";
    private TextView tvAppName;
    private AppCompatButton btnGetOTP, btnOk;
    private TextInputLayout ilOPT;
    private TextInputEditText etOpt, etPhoneNumber;
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
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
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
        if (Helper.isEmptyFieldValidation(etOpt)) {
            String otpText = etOpt.getText().toString().trim();
            if (TextUtils.isEmpty(otpText)) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            int enteredOtp;
            try {
                enteredOtp = Integer.parseInt(otpText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid OTP format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve OTP from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("OTP_PREF", MODE_PRIVATE);
            int savedOtp = preferences.getInt("OTP", -1); // Default -1 if OTP is not found

            if (enteredOtp == savedOtp) {
                // OTP Verified → Unlock App
                updateTempUnlockExpiryTime(restrictedAppId);

                // Clear OTP from SharedPreferences after successful verification
                preferences.edit().remove("OTP").apply();
                Toast.makeText(this, "Temporary unlock successful", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
            }
        }
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }

    private void onClickBtnGetOPT() {
        if (Helper.isEmptyFieldValidation(etPhoneNumber) && Helper.isContactValid(etPhoneNumber)) {
            ilOPT.setVisibility(View.VISIBLE);
            btnOk.setVisibility(View.VISIBLE);
            etOpt.requestFocus();

            String phoneNumber = etPhoneNumber.getText().toString().trim();

            // Generate a random 6-digit OTP
            int otp = new Random().nextInt(900000) + 100000;

            // Store OTP in SharedPreferences
            SharedPreferences preferences = getSharedPreferences("OTP_PREF", MODE_PRIVATE);
            preferences.edit().putInt("OTP", otp).apply();

            // Send OTP using Fast2SMS API
            sendOtpViaSms(phoneNumber, otp);
        }
    }

    private void updateTempUnlockExpiryTime(int appId) {
        // Call the database method to update the TempUnlockExpiryTime
        db.updateTempUnlockExpiryTime(appId);
        Toast.makeText(this, "Temporary unlock successful", Toast.LENGTH_SHORT).show();
    }

    // Function to send OTP using Fast2SMS API
    private void sendOtpViaSms(String phoneNumber, int otp) {
        String apiKey = "fZ3Ib2YDAFJ0Kt9m46GUhRBwz1a7xCQdqiPksLjygXcuHNWEvVhJE27SgbMRoP0FHA6a4dwtikuvWDVe";
        String message = "Your OTP for app access is: " + otp;
        String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey +
                "&route=q&message=" + message +
                "&language=english&flash=0&numbers=" + phoneNumber;

        // Send HTTP request
        new SendOtpTask().execute(url);
    }

    // AsyncTask to send OTP request
    private static class SendOtpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("cache-control", "no-cache");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, "OTP sent successfully: " + result);
            } else {
                Log.e(TAG, "Failed to send OTP");
            }
        }
    }
}