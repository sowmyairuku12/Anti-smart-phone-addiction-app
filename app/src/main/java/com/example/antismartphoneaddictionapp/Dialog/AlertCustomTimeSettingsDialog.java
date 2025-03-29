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
    private TextInputEditText etRestrictAppAfter, etExpiryTime, etTemporaryUnlockTime;
    private AppCompatButton btnCancel, btnUpdate;
    private Dialog dialog;
    private Context context;

    public AlertCustomTimeSettingsDialog(Context context) {
        this.context = context;
    }

    public Dialog openProfileDialog() {
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
                Log.e(TAG, "Error in AlertProfileDialog: ", e);
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in AlertProfileDialog: ", e);
        }
        return dialog;
    }

    private void initUI() {
        etRestrictAppAfter = alertView.findViewById(R.id.etRestrictAppAfter);
        etExpiryTime = alertView.findViewById(R.id.etExpiryTime);
        etTemporaryUnlockTime = alertView.findViewById(R.id.etTemporaryUnlockTime);
        btnCancel = alertView.findViewById(R.id.btnCancel);
        btnUpdate = alertView.findViewById(R.id.btnUpdate);
    }

    private void setData() {
        etRestrictAppAfter.setText(String.valueOf(Constants.FOREGROUND_CHECK_TIME));
        etExpiryTime.setText(String.valueOf(Constants.EXPIRY_TIME));
        etTemporaryUnlockTime.setText(String.valueOf(Constants.TEMP_EXPIRY_TIME));
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
        View[] views = {etRestrictAppAfter, etExpiryTime, etTemporaryUnlockTime};
        if (Helper.isEmptyFieldValidation(views)) {
            long restrictedAppAfter = Long.parseLong(Objects.requireNonNull(Helper.getStringFromInput(etRestrictAppAfter)));
            int expiryTime = Integer.parseInt(Objects.requireNonNull(Helper.getStringFromInput(etExpiryTime)));
            long tempLockTime = Long.parseLong(Objects.requireNonNull(Helper.getStringFromInput(etTemporaryUnlockTime)));

            // Save values in SharedPreferences and update Constants
            Constants.updateValues(context, restrictedAppAfter, expiryTime, tempLockTime);

            // Close dialog
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

}
