package com.cti.fmi.licentaapk.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.models.LoginResponse;
import com.cti.fmi.licentaapk.models.UserLogin;
import com.cti.fmi.licentaapk.services.ServiceGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity
{
    private final static String PREFERENCES = "MySharedPrefs";
    private final static String LOG_TAG = LoginActivity.class.getSimpleName();

    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView mRegisterTextLinkView;

    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = findViewById(R.id.login_email);

        mPasswordView = findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRegisterTextLinkView = findViewById(R.id.registerLink);
        mRegisterTextLinkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin()
    {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_email_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel)
        {
            focusView.requestFocus();
        } else
        {
            showProgress(true);

            UserLogin userLogin = new UserLogin(email,password);
            UserService userService =
                    ServiceGenerator.createService(UserService.class);
            Call<LoginResponse> call = userService.loginRequest(userLogin);
            call.enqueue(new Callback<LoginResponse>()
            {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call ,
                                       @NonNull Response<LoginResponse> response)
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

                        Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );

                        Toast.makeText(getApplicationContext(),
                                "Login successful",
                                Toast.LENGTH_LONG)
                                .show();

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

                                Toast.makeText(getBaseContext(),
                                        jObjError.optJSONArray("non_field_errors").optString(0),
                                        Toast.LENGTH_LONG)
                                        .show();
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
                                    "The server is not responding. Please try again later.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }

                        showProgress(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call , @NonNull Throwable t)
                {
                    Log.e(LOG_TAG , t.getMessage() + "Login failed");
                    showProgress(false);
                    Toast.makeText(getBaseContext(),
                            "The server is not responding. Please try again later.",
                            Toast.LENGTH_LONG)
                            .show();
                }

            });
        }
    }

    private boolean isEmailValid(String email)
    {
        return !TextUtils.isEmpty(email) &&
                android.util
                        .Patterns
                        .EMAIL_ADDRESS
                        .matcher(email)
                        .matches();
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 6;
    }

    private void showProgress(final boolean show)
    {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

