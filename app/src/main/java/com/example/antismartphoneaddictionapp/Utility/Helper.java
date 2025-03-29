package com.example.antismartphoneaddictionapp.Utility;

import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class Helper {
    private static final String TAG = Helper.class.getSimpleName();

    public static double getDoubleValueFromString(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }
    public static double getLongValueFromString(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }
    public static String getStringFromInput(View view) {
        try {
            if (view instanceof TextInputEditText) {
                TextInputEditText editText = (TextInputEditText) view;
                return Objects.requireNonNull(editText.getText()).toString().trim();
            } else if (view instanceof MaterialAutoCompleteTextView) {
                MaterialAutoCompleteTextView editText = (MaterialAutoCompleteTextView) view;
                return Objects.requireNonNull(editText.getText()).toString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static boolean isEmptyFieldValidation(TextInputEditText editText) {
        boolean isValidate = true;
        try {
            TextInputLayout textInputLayout = null;
            ViewParent parent = editText.getParent().getParent();
            if (parent instanceof TextInputLayout) {
                textInputLayout = (TextInputLayout) parent;
            }
            if (Objects.requireNonNull(editText.getText()).toString().trim().isEmpty()) {
                if (textInputLayout != null) {
                    textInputLayout.isHelperTextEnabled();
                    textInputLayout.setError("Please " + textInputLayout.getHint());
                    textInputLayout.setErrorEnabled(true);
                } else {
                    editText.setError("Empty");
                }
                isValidate = false;
            } else {
                if (textInputLayout != null) {
                    textInputLayout.setErrorEnabled(false);
                } else {
                    editText.setError(null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in Helper Class: ", e);
            isValidate = false;
        }
        return isValidate;
    }

    public static boolean isEmptyFieldValidation(View[] inputFields) {
        boolean isValidate = true;
        try {
            for (View view : inputFields) {
                TextInputLayout textInputLayout = null;
                ViewParent parent = view.getParent().getParent();
                if (parent instanceof TextInputLayout) {
                    textInputLayout = (TextInputLayout) parent;
                }

                String inputText = "";
                if (view instanceof TextInputEditText) {
                    inputText = Objects.requireNonNull(((TextInputEditText) view).getText()).toString().trim();
                } else if (view instanceof MaterialAutoCompleteTextView) {
                    inputText = Objects.requireNonNull(((MaterialAutoCompleteTextView) view).getText()).toString().trim();
                }

                if (inputText.isEmpty()) {
                    if (textInputLayout != null) {
                        textInputLayout.setError("Please " + textInputLayout.getHint());
                        textInputLayout.setErrorEnabled(true);
                    } else {
                        if (view instanceof TextInputEditText) {
                            ((TextInputEditText) view).setError("Empty");
                        } else if (view instanceof MaterialAutoCompleteTextView) {
                            ((MaterialAutoCompleteTextView) view).setError("Empty");
                        }
                    }
                    isValidate = false;
                } else {
                    if (textInputLayout != null) {
                        textInputLayout.setErrorEnabled(false);
                    } else {
                        if (view instanceof TextInputEditText) {
                            ((TextInputEditText) view).setError(null);
                        } else if (view instanceof MaterialAutoCompleteTextView) {
                            ((MaterialAutoCompleteTextView) view).setError(null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in validation: ", e);
            isValidate = false;
        }
        return isValidate;
    }

}
