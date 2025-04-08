package com.example.antismartphoneaddictionapp.Dialog;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Utility.Constants;
import com.example.antismartphoneaddictionapp.Utility.Helper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class AlertUpdateSettingsOTPDialog implements View.OnClickListener {

    public static final String TAG = "AlertUpdateSettingsOPTDialog";
    private TextView tvUpdateMsg;
    private View alertView;

    private AppCompatButton btnGetOTP, btnOk;
    private TextInputLayout ilOPT;
    private TextInputEditText etOpt, etPhoneNumber;

    private Dialog dialog;
    private Context context;
    private long newForegroundCheckTime, newTempExpiryTime;
    private int newExpiryTime;
    private String phoneNumber;

    public AlertUpdateSettingsOTPDialog(Context context, long foregroundTime, int expiry, long tempTime, String phoneNumber) {
        this.context = context;
        this.newForegroundCheckTime = foregroundTime;
        this.newExpiryTime = expiry;
        this.newTempExpiryTime = tempTime;
        this.phoneNumber = phoneNumber;
    }

    public Dialog openUpdateSettingsDialog() {
        try {
            try {
                LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
                alertView = layoutInflater.inflate(R.layout.dialog_update_settings_otp, null);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                if (alertView.getParent() != null) {
                    ((ViewGroup) alertView.getParent()).removeView(alertView);
                }
                alertBuilder.setView(alertView);

                initUI();
                initListener();
                initObj();
                dialog = alertBuilder.create();

            } catch (Exception e) {
                Log.e(TAG, "Error in openUpdateSettingsDialog: ", e);
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in openUpdateSettingsDialog: ", e);
        }
        return dialog;
    }


    private void initUI() {
        tvUpdateMsg = alertView.findViewById(R.id.tvUpdateMsg);
        btnGetOTP = alertView.findViewById(R.id.btnGetOTP);
        ilOPT = alertView.findViewById(R.id.ilOPT);
        etPhoneNumber = alertView.findViewById(R.id.etPhoneNumber);
        etOpt = alertView.findViewById(R.id.etOpt);
        btnOk = alertView.findViewById(R.id.btnOk);
        etPhoneNumber.setText(phoneNumber);

    }

    private void initListener() {
        btnGetOTP.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    private void initObj() {

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
        //for testing
//        long opt = Long.parseLong(etOpt.getText().toString().trim());
//        if (opt == Constants.OPT) {
//            //Update the time in shared pref
//            Constants.updateValues(context, newForegroundCheckTime, newExpiryTime, newTempExpiryTime);
//            dialog.dismiss();
//
//        } else {   
//            Toast.makeText(alertView.getContext(), "Incorrect OTP", Toast.LENGTH_SHORT).show();
//        }


        //by using api for sms////
        if (Helper.isEmptyFieldValidation(etOpt)) {
            String otpText = etOpt.getText().toString().trim();
            if (TextUtils.isEmpty(otpText)) {
                Toast.makeText(alertView.getContext(), "Please enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            int enteredOtp;
            try {
                enteredOtp = Integer.parseInt(otpText);
            } catch (NumberFormatException e) {
                Toast.makeText(alertView.getContext(), "Invalid OTP format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve OTP from SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences("SETTINGS_OTP_PREF", MODE_PRIVATE);
            int savedOtp = preferences.getInt("OTP", -1); // Default -1 if OTP is not found

            if (enteredOtp == savedOtp) {
                // OTP Verified → Update App settings
                Constants.updateValues(context, newForegroundCheckTime, newExpiryTime, newTempExpiryTime, phoneNumber);

                // Clear OTP from SharedPreferences after successful verification
                preferences.edit().remove("OTP").apply();
                Toast.makeText(alertView.getContext(), "Settings updated successful", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(alertView.getContext(), "Incorrect OTP", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onClickBtnGetOPT() {
        if (Helper.isEmptyFieldValidation(etPhoneNumber) && Helper.isContactValid(etPhoneNumber)) {
            ilOPT.setVisibility(View.VISIBLE);
            btnOk.setVisibility(View.VISIBLE);
            etOpt.requestFocus();


            //by using api for sms////
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            // Generate a random 6-digit OTP
            int otp = new Random().nextInt(900000) + 100000;

            // Store OTP in SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences("SETTINGS_OTP_PREF", MODE_PRIVATE);
            preferences.edit().putInt("OTP", otp).apply();

            // Send OTP using Fast2SMS API
            sendOtpViaSms(phoneNumber, otp);
        }
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

