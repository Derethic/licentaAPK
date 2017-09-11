package com.cti.fmi.licentaapk.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.adapters.AdvertAdapter;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.models.Advert;
import com.cti.fmi.licentaapk.services.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdvertActivity extends AppCompatActivity
{
    private final static String PREFERENCES = "MySharedPrefs";
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private final static int ADVERT_VISIBLE_LIMIT_NUMBER = 10;
    private final static int ADVERT_VISIBLE_DECREMENT = 3;
    private static int pageNumber;
    private boolean endOfAdvertList;

    private RecyclerView mAdvertRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private AdvertAdapter advertAdapter;

    private ArrayList<Advert> advertList;

    private View mMainLayoutView;
    private View mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_advert);

        pageNumber = 1;
        endOfAdvertList = false;

        mAdvertRecyclerView = findViewById(R.id.my_adverts_recyclerview);

        linearLayoutManager = new LinearLayoutManager(this);
        mAdvertRecyclerView.setLayoutManager(linearLayoutManager);

        advertList = new ArrayList<>();

        advertAdapter = new AdvertAdapter(getBaseContext(),advertList);
        mAdvertRecyclerView.setAdapter(advertAdapter);

        mAdvertRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {

                if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == advertList.size() - ADVERT_VISIBLE_DECREMENT)
                {
                    Log.e("adverListSize:", String.valueOf(advertList.size()));

                    if (!endOfAdvertList)
                    {
                        SharedPreferences sharedPrefs =
                                getSharedPreferences(PREFERENCES,
                                        Context.MODE_PRIVATE
                                );

                        String authToken = sharedPrefs.getString("auth-token", "");
                        pageNumber += 1;

                        getAdverts(authToken, pageNumber);
                    }
                }
            }
        });

        mMainLayoutView = findViewById(R.id.my_adverts_main_layout);
        mProgressView = findViewById(R.id.my_adverts_progress);

        Toolbar toolbar = findViewById(R.id.my_adverts_toolbar);
        toolbar.setTitle("My Adverts");

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        SharedPreferences sharedPrefs =
                getSharedPreferences(PREFERENCES,
                        Context.MODE_PRIVATE
                );

        String authToken = sharedPrefs.getString("auth-token", "");
        getAdverts(authToken, pageNumber);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void getAdverts(String authToken, int pageNumber)
    {
        showProgress(true);

        UserService userService = ServiceGenerator.createService(UserService.class);
        Call<ArrayList<Advert>> call = userService.getUserAdverts("Token " + authToken, pageNumber);
        call.enqueue(new Callback<ArrayList<Advert>>()
        {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Advert>> call,@NonNull Response<ArrayList<Advert>> response)
            {
                if(response.isSuccessful())
                {
                    final ArrayList<Advert> list = response.body();

                    if(list != null && response.code() == 202)
                    {
                        advertList.addAll(list);
                        advertAdapter.notifyDataSetChanged();
                        advertAdapter.notifyItemRangeChanged(0, advertAdapter.getItemCount());

//                        if(list.size() < ADVERT_VISIBLE_LIMIT_NUMBER)
//                        {
//                            endOfAdvertList = true;
//                        }
                        showProgress(false);
                    }
                    else if( response.code() == 204)
                    {
                        endOfAdvertList = true;
                        showProgress(false);
                    }
                    else
                    {
                        Log.e("response.body()" , " e null");
                        endOfAdvertList = true;
                        showProgress(false);
                    }
                }
                else
                {
                    Log.e("response" , "not successful");
                    showProgress(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Advert>> call, @NonNull Throwable t)
            {
                Log.e("call.enqueue()" , "failed");
                showProgress(false);
            }
        });
    }

    private void showProgress(final boolean show)
    {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mMainLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
        mMainLayoutView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMainLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
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
