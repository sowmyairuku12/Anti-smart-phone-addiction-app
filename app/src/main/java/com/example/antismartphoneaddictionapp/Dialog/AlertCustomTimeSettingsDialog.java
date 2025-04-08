package com.example.antismartphoneaddictionapp.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.example.antismartphoneaddictionapp.R;
import com.example.antismartphoneaddictionapp.Utility.Constants;
import com.example.antismartphoneaddictionapp.Utility.Helper;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class AlertCustomTimeSettingsDialog implements View.OnClickListener {
    private static final String TAG = AlertCustomTimeSettingsDialog.class.getSimpleName();
    private View alertView;
    private TextInputEditText etRestrictAppAfter, etExpiryTime, etTemporaryUnlockTime, etPhoneNumber;
    private AppCompatButton btnCancel, btnUpdate;
    private Dialog dialog;
    private Context context;

    public AlertCustomTimeSettingsDialog(Context context) {
        this.context = context;
    }

    public Dialog openCustomSettingsDialog() {
        try {
            try {
                LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
                alertView = layoutInflater.inflate(R.layout.dilog_custom_settings, null);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                if (alertView.getParent() != null) {
                    ((ViewGroup) alertView.getParent()).removeView(alertView);
                }
                alertBuilder.setView(alertView);

                initUI();
                setData();
                setListeners();
                dialog = alertBuilder.create();

            } catch (Exception e) {
                Log.e(TAG, "Error in openCustomSettingsDialog: ", e);
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in openCustomSettingsDialog: ", e);
        }
        return dialog;
    }

    private void initUI() {
        etRestrictAppAfter = alertView.findViewById(R.id.etRestrictAppAfter);
        etExpiryTime = alertView.findViewById(R.id.etExpiryTime);
        etTemporaryUnlockTime = alertView.findViewById(R.id.etTemporaryUnlockTime);
        etPhoneNumber = alertView.findViewById(R.id.etPhoneNumber);
        btnCancel = alertView.findViewById(R.id.btnCancel);
        btnUpdate = alertView.findViewById(R.id.btnUpdate);
    }

    private void setData() {
        etRestrictAppAfter.setText(String.valueOf(Constants.FOREGROUND_CHECK_TIME / 3600000));  //Milliseconds → Hours
        etExpiryTime.setText(String.valueOf(Constants.EXPIRY_TIME));  //In Hours
        etTemporaryUnlockTime.setText(String.valueOf(Constants.TEMP_EXPIRY_TIME / 60000));  //Minutes → Milliseconds
        etPhoneNumber.setText(String.valueOf(Constants.PHONE_NUMBER));

    }

    private void setListeners() {
        btnCancel.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnCancel) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } else if (id == R.id.btnUpdate) {
            onClickBtnUpdate();
        }
    }

    private void onClickBtnUpdate() {
        View[] views = {etRestrictAppAfter, etExpiryTime, etTemporaryUnlockTime, etPhoneNumber};
        if (Helper.isEmptyFieldValidation(views) && Helper.isContactValid(etPhoneNumber)) {
            long restrictedAppAfterHours = Long.parseLong(Objects.requireNonNull(Helper.getStringFromInput(etRestrictAppAfter)));
            long restrictedAppAfterMs = restrictedAppAfterHours * 3600000;    //Hours → Milliseconds

            int expiryTime = Integer.parseInt(Objects.requireNonNull(Helper.getStringFromInput(etExpiryTime)));
            long tempLockTimeMinutes = Long.parseLong(Objects.requireNonNull(Helper.getStringFromInput(etTemporaryUnlockTime)));
            long tempLockTimeMs = tempLockTimeMinutes * 60000;   //Minutes → Milliseconds
            String phoneNumber = Helper.getStringFromInput(etPhoneNumber);
//            Open AlertUpdateSettingsOPTDialog and call Constants.updateValues() there after opt is successfully validated
            new AlertUpdateSettingsOTPDialog(context, restrictedAppAfterMs, expiryTime, tempLockTimeMs, phoneNumber)
                    .openUpdateSettingsDialog();

            // Save values in SharedPreferences and update Constants value
//            Constants.updateValues(context, restrictedAppAfterMs, expiryTime, tempLockTimeMs);

            // Close dialog
//            if (dialog != null && dialog.isShowing()) {
//                dialog.dismiss();
//            }
        }
    }

}
