package com.cti.fmi.licentaapk.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.adapters.AdvertAdapter;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.models.Advert;
import com.cti.fmi.licentaapk.models.UserProfile;
import com.cti.fmi.licentaapk.services.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
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

    private TextView mUsernameTextView;

    private UserProfile userProfile;
    private ArrayList<Advert> advertList;

    private View mMainLayoutView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pageNumber = 1;
        endOfAdvertList = false;

        mAdvertRecyclerView = findViewById(R.id.advert_main_recyclerview);

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

        mMainLayoutView = findViewById(R.id.advert_main_layout);
        mProgressView = findViewById(R.id.main_progress);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        FloatingActionButton fab = findViewById(R.id.add_announcement_button_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateAdvertActivity.class);
                startActivity(intent);

                //finish();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu navigationMenu = navigationView.getMenu();

        View headerView = navigationView.getHeaderView(0);

        mUsernameTextView = headerView.findViewById(R.id.username_main);

        MenuItem navAnnouncementMenuItem = navigationMenu.findItem(R.id.nav_add_announcement);

        SpannableString s = new SpannableString(navAnnouncementMenuItem.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.NavCustomMenuItemAppearance), 0, s.length(), 0);
        navAnnouncementMenuItem.setTitle(s);
        navAnnouncementMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_plus_dark_orange));
        //navAnnouncementMenuItem.setChecked(true);

        MenuItem navMyAdvertsMenuItem = navigationMenu.findItem(R.id.nav_my_adverts);
        navMyAdvertsMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_user_my_adverts));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        checkAuthentication();
    }

    private void checkAuthentication()
    {
        showProgress(true);

        SharedPreferences sharedPrefs =
                getSharedPreferences(PREFERENCES,
                        Context.MODE_PRIVATE
                );

        String authToken = sharedPrefs.getString("auth-token", "");
        int idUser = sharedPrefs.getInt("user-id", 0);
        // Log.e("authToken",authToken);

        //check if user is authenticated and load data for user profile
        if (authToken.equals("") || idUser == 0)
        {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        }
        else
        {
            UserService userService = ServiceGenerator.createService(UserService.class);
            Call<UserProfile> call = userService.getUserDetails("Token " + authToken,idUser);
            call.enqueue(new Callback<UserProfile>()
            {
                @Override
                public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response)
                {
                    if(response.isSuccessful() && response.code() == 202)
                    {
                        userProfile = response.body();

                        StringBuilder sb = new StringBuilder();

                        sb.append(userProfile.getLastName());
                        sb.append(" ");
                        sb.append(userProfile.getFirstName());

                        mUsernameTextView.setText(sb.toString());
                    }
                    else
                    {
                        Log.e("UserProfile", "Response was not succesful");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t)
                {
                    Log.e("UserProfile", "Service Call error" + t.getMessage());
                }
            });

            getAdverts(authToken, pageNumber);
        }
        showProgress(false);
    }

    private void getAdverts(String authToken, int pageNumber)
    {
        UserService userService = ServiceGenerator.createService(UserService.class);
        Call<ArrayList<Advert>> call = userService.getAllAdverts("Token " + authToken, pageNumber);
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
                    }
                    else if( response.code() == 204)
                    {
                        endOfAdvertList = true;
                    }
                    else
                    {
                        Log.e("response.body()" , " e null");
                        endOfAdvertList = true;
                    }
                }
                else
                {
                    Log.e("response" , "not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Advert>> call,@NonNull Throwable t)
            {
                Log.e("call.enqueue()" , "failed");
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem mSearchMenuItem = menu.findItem(R.id.main_search);
        SearchView mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setQueryHint(fromHtml("<font color =#8d9c87>Search</font>"));

        // Associate searchable configuration with the SearchView
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchActivity.class)));

        return true;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html)
    {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log_out)
        {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(PREFERENCES,
                            Context.MODE_PRIVATE
                    );

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("auth-token");
            editor.remove("user-id");
            editor.apply();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finish();
        }
        else if(id == R.id.nav_add_announcement)
        {
            Intent intent = new Intent(getApplicationContext(), CreateAdvertActivity.class);
            startActivity(intent);

            //finish();
        }
        else if(id == R.id.nav_my_adverts)
        {
            Intent intent = new Intent(getBaseContext(), UserAdvertActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
