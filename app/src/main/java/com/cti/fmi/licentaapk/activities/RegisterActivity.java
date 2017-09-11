package com.cti.fmi.licentaapk.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.models.LoginResponse;
import com.cti.fmi.licentaapk.models.UserRegister;
import com.cti.fmi.licentaapk.services.ServiceGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity
{
    private final static String PREFERENCES = "MySharedPrefs";
    private final static String LOG_TAG = RegisterActivity.class.getSimpleName();

    // Account Info
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    // Personal Info
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPhoneNumberView;
    private EditText mLocationView;
    private String location = "";
    private String locationCountryName = "";

    private View mProgressView;
    private View mRegisterFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = findViewById(R.id.register_email);
        mPasswordView = findViewById(R.id.register_password);
        mPasswordConfirmView = findViewById(R.id.register_password_confirm);

        mFirstNameView = findViewById(R.id.register_first_name);
        mLastNameView = findViewById(R.id.register_last_name);
        mPhoneNumberView = findViewById(R.id.register_phone_number);
        mLocationView = findViewById(R.id.register_location);

        mLocationView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivityForResult(new Intent(getApplicationContext(), MapsActivity.class), 200);
            }
        });

        Button mEmailRegisterButton = findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptRegister();
            }
        });


        Toolbar toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    private void attemptRegister()
    {
        // Account Info
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmView.setError(null);

        // Personal Info
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mPhoneNumberView.setError(null);
        mLocationView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mPasswordConfirmView.getText().toString();

        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String phoneNumber = mPhoneNumberView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if( location.equals(""))
        {
            mLocationView.setError(getString(R.string.error_location));
            cancel = true;
            focusView = mLocationView;
        }

        if(!isValidMobile(phoneNumber))
        {
            mPhoneNumberView.setError(getString(R.string.error_phone_number));
            cancel = true;
            focusView = mPhoneNumberView;
        }

        if(!isNameValid(lastName))
        {
            mLastNameView.setError(getString(R.string.error_name));
            cancel = true;
            focusView = mLastNameView;
        }

        if(!isNameValid(firstName))
        {
            mFirstNameView.setError(getString(R.string.error_name));
            cancel = true;
            focusView = mFirstNameView;
        }

        if (TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_password_required));
            cancel = true;
            focusView = mPasswordView;

        } else if (!isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
            focusView = mPasswordView;

        } else if(!password.equals(confirmPassword))
        {
            mPasswordConfirmView.setError(getString(R.string.error_password_match));
            cancel = true;
            focusView = mPasswordConfirmView;
        }

        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(
                    getString(R.string.error_email_required)
            );
            cancel = true;
            focusView = mEmailView;

        } else if (!isEmailValid(email))
        {
            mEmailView.setError(
                    getString(R.string.error_invalid_email)
            );
            cancel = true;
            focusView = mEmailView;
        }

        if (cancel)
        {
            focusView.requestFocus();
        } else
        {
            showProgress(true);
            UserRegister userRegister = new UserRegister(email,password,firstName,lastName,location, locationCountryName,phoneNumber);

            UserService userService =
                    ServiceGenerator.createService(UserService.class);
            Call<LoginResponse> call = userService.registerRequest(userRegister);
            call.enqueue(new Callback<LoginResponse>()
            {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response)
                {
                    if(response.isSuccessful())
                    {
                        String authToken = response.body().getToken();

                        SharedPreferences preferences =
                                getSharedPreferences(PREFERENCES,
                                        Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("auth-token",authToken);

                        Log.e(LOG_TAG,"Token login created : " + authToken);

                        editor.putInt("user-id",response.body().getId());
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Register successful", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(
                                RegisterActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);

                        finish();
                    }
                    else
                    {
                        if (response.code() == 400)
                        {
                            try
                            {
                                JSONObject jObjError = new JSONObject (response.errorBody().string());

                                String errorString = jObjError.optJSONArray("email").optString(0);

                                mEmailView.setError(errorString);
                                mEmailView.requestFocus();
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                            catch(IOException e)
                            {
                                Log.e(LOG_TAG,e.getMessage());
                            }
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(),
                                    "The server is not responding. Please try again later",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }

                        showProgress(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call,@NonNull Throwable t)
                {
                    Log.e(LOG_TAG , t.getMessage() + "Register failed");
                    showProgress(false);
                    Toast.makeText(getBaseContext(),
                            "The server is not responding. Please try again later",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    public static boolean isNameValid(String txt)
    {
        String regx = "^[\\p{L} .'-]+$";
        Pattern pattern = Pattern
                .compile(regx,
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher = pattern.matcher(txt);

        return matcher.find();
    }

    private boolean isEmailValid(String email)
    {

        return !TextUtils.isEmpty(email) &&
                android.util.Patterns
                        .EMAIL_ADDRESS
                        .matcher(email)
                        .matches();
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 6;
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 200 && resultCode == RESULT_OK)
        {
            String locality = data.getStringExtra("locality");
            String countryName = data.getStringExtra("countryName");

            StringBuilder sb = new StringBuilder();

            if (locality != null && !locality.equals(""))
            {
                sb.append(locality);
                sb.append(", ");
            }

            if (countryName != null && !countryName.equals(""))
            {
                sb.append(countryName);
            }

            mLocationView.setText(sb.toString());
            location = data.getStringExtra("location");
            locationCountryName = countryName;
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void showProgress(final boolean show)
    {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
