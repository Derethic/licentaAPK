package com.cti.fmi.licentaapk.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.fragments.CategoryFragment;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.services.ServiceGenerator;
import com.cti.fmi.licentaapk.utilities.FileUtils;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAdvertActivity extends AppCompatActivity
        implements CategoryFragment.OnFragmentInteractionListener
{
    private final static String PREFERENCES = "MySharedPrefs";

    private final static int NUMBER_OF_PHOTOS = 4;

    private List<MultipartBody.Part> pictureList;

    private EditText mTitleView;
    private TextView mGalleryTextView;
    private TextView mGalleryError;
    private LinearLayout mGalleryLayout;
    private TextInputLayout mCategoryTextInputLayout;
    private EditText mCategoryView;
    private EditText mPriceView;
    private Spinner mSpinnerCurrency;
    private Spinner mSpinnerCondition;
    private EditText mDescriptionView;
    private Button mCreateAdButton;
    private ImageView mPictureTestView;

    private ArrayAdapter<CharSequence> spinnerCurrencyAdapter;
    private ArrayAdapter<CharSequence> conditionAdapter;
    private int idMapCategory;

    private View mProgressView;
    private View mCreateAdFormView;

    @NonNull
    private RequestBody createPartFromString(String descriptionString)
    {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri)
    {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(getBaseContext(), fileUri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        Bitmap bm = decodeFile(file);
        mGalleryLayout.addView(getNewImageView(bm));

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ad);

        pictureList = new ArrayList<>();

        mPictureTestView = findViewById(R.id.imgTest);

        mTitleView = findViewById(R.id.create_ad_title);

        mGalleryTextView = findViewById(R.id.create_ad_gallery_textview);
        mGalleryError = findViewById(R.id.create_ad_gallery_error);

        mGalleryLayout = findViewById(R.id.create_ad_gallery);
        mGalleryLayout.addView(getDefaultImageView());

        mCategoryTextInputLayout = findViewById(R.id.create_ad_category_textinput);

        mCategoryView = findViewById(R.id.create_ad_category);
        mCategoryView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CategoryFragment categoryFragment = new CategoryFragment();

                categoryFragment.show(
                        getSupportFragmentManager(),
                        "categoryPicker");
            }
        });

        mPriceView = findViewById(R.id.create_ad_price);
        mSpinnerCurrency = findViewById(R.id.create_ad_currency_spinner);
        mSpinnerCondition = findViewById(R.id.create_ad_condition_spinner);
        mDescriptionView = findViewById(R.id.create_ad_description);

        mCreateAdButton = findViewById(R.id.create_ad_button);
        mCreateAdButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptCreateAd();
            }
        });

        spinnerCurrencyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.currency_values,
                android.R.layout.simple_spinner_item);
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCurrency.setAdapter(spinnerCurrencyAdapter);

        conditionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.condition_values,
                android.R.layout.simple_spinner_item);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCondition.setAdapter(conditionAdapter);

        Toolbar toolbar = findViewById(R.id.create_ad_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Create new advert");
        }

        mCreateAdFormView = findViewById(R.id.create_ad_form);
        mProgressView = findViewById(R.id.create_ad_progress);
    }

    private  void attemptCreateAd()
    {
        mTitleView.setError(null);
        mGalleryTextView.setError(null);
        mCategoryView.setError(null);
        mPriceView.setError(null);
        mDescriptionView.setError(null);

        String title = mTitleView.getText().toString();
        String category = mCategoryView.getText().toString();
        String priceString = mPriceView.getText().toString();
        String description = mDescriptionView.getText().toString();

        //priceString = priceString.substring(0, priceString.length() - 1);

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(description))
        {
            mDescriptionView.setError("Please enter a description");
            cancel = true;
            focusView = mDescriptionView;
        }

        if (TextUtils.isEmpty(priceString))
        {
            mPriceView.setError("Please enter a price");
            cancel = true;
            focusView = mPriceView;
        }
        else if(priceString.charAt(0) == '.' || priceString.charAt(priceString.length()-1) == '.' )
        {
            mPriceView.setError("Invalid price number");
            cancel = true;
            focusView = mPriceView;
        }

        if (TextUtils.isEmpty(category))
        {
            mCategoryTextInputLayout.setError("Please choose a category");
            cancel = true;
            focusView = mTitleView;
        }

        if(mGalleryLayout.getChildCount() == 1 )
        {
            mGalleryError.setVisibility(View.VISIBLE);
            cancel = true;
            focusView = mTitleView;
        }

        if (TextUtils.isEmpty(title))
        {
            mTitleView.setError("Please enter a title");
            cancel = true;
            focusView = mTitleView;
        }

        if (cancel)
        {
            focusView.requestFocus();
        }
        else
        {
            SharedPreferences sharedPrefs =
                    getSharedPreferences(PREFERENCES,
                            Context.MODE_PRIVATE
                    );

            String authToken = sharedPrefs.getString("auth-token", "");
            int idUser = sharedPrefs.getInt("user-id", 0);
            // Log.e("authToken",authToken);

            if (authToken.equals("") || idUser == 0)
            {
                Intent intent = new Intent(CreateAdvertActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            }
            else
            {
                showProgress(true);

                UserService userService =
                        ServiceGenerator.createAdvertService(UserService.class);

                RequestBody userRequest = createPartFromString(String.valueOf(idUser));
                RequestBody titleRequest = createPartFromString(title);
                RequestBody categoryRequest = createPartFromString(String.valueOf(idMapCategory));
                RequestBody priceRequest = createPartFromString(priceString);
                RequestBody currencyRequest = createPartFromString(mSpinnerCurrency.getSelectedItem().toString());
                RequestBody conditionRequest = createPartFromString(mSpinnerCondition.getSelectedItem().toString());
                RequestBody descriptionRequest = createPartFromString(description);

                HashMap<String, RequestBody> map = new HashMap<>();
                map.put("user",userRequest);
                map.put("title", titleRequest);
                map.put("mapcategory", categoryRequest);
                map.put("price", priceRequest);
                map.put("currency", currencyRequest);
                map.put("condition", conditionRequest);
                map.put("description", descriptionRequest);

                Call<ResponseBody> call = userService.createNewAdvert("Token " + authToken, map, pictureList);
                call.enqueue(new Callback<ResponseBody>()
                {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response)
                    {
                        if(response.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Advert was succesfully created",Toast.LENGTH_LONG).show();

                            finish();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(),
                                    "The server is not responding. Please try again later",
                                    Toast.LENGTH_LONG)
                                    .show();

                            showProgress(false);
                            Log.e("NewAdvertResponse", "Response was not successful");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t)
                    {
                        Toast.makeText(getBaseContext(),
                                "The server is not responding. Please try again later",
                                Toast.LENGTH_LONG)
                                .show();

                        showProgress(false);
                        Log.e("NewAdvertResponse", "Call failure");
                    }
                });
            }
        }
    }

    private ImageView getDefaultImageView()
    {
        ImageView defaultImageView  = new ImageView(getBaseContext());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            defaultImageView.setImageDrawable(getBaseContext().getDrawable(R.drawable.default_ad_photo));
        } else
        {
            defaultImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_ad_photo));
        }

        defaultImageView.setLayoutParams(new LinearLayout.LayoutParams(300, 200));
        setMargins(defaultImageView, 4 , 0 , 4 , 0);
        defaultImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        defaultImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mGalleryError.getVisibility() == View.VISIBLE )
                {
                    mGalleryError.setVisibility(View.GONE);
                }

                startGalleryActivity();
            }
        });

        return defaultImageView;
    }

    private void startGalleryActivity()
    {
        FishBun.with(CreateAdvertActivity.this)
                .MultiPageMode()
                .setMaxCount(4)
                .setMinCount(1)
                .setAlbumSpanCount(2, 3)
                .exceptGif(true)
                .setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(CreateAdvertActivity.this, R.drawable.ic_back_button_white))
                .setAllViewTitle("All")
                .setActionBarTitle("Select Pictures")
                .textOnNothingSelected("Please select at least one picture!")
                .textOnImagesSelectionLimitReached("You can't choose more than 4 pictures")
                .startAlbum();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData)
    {
        super.onActivityResult(requestCode, resultCode, imageData);
        switch (requestCode)
        {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {

                    //You can get image path(ArrayList<String>) Under version 0.6.2
                    //You can get image path(ArrayList<Uri>) Version 0.6.2 or later

//                    final int childCount = mGalleryLayout.getChildCount();
//
//                    for (int i = 0; i < childCount; i++)
//                    {
//                        View view = mGalleryLayout.getChildAt(i);
//                        ImageView imageView = (ImageView) view;
//                        BitmapDrawable bd = (BitmapDrawable)imageView.getDrawable();
//                        bd.getBitmap().recycle();
//                        imageView.setImageBitmap(null);
//                    }


                    mGalleryLayout.removeAllViews();

                    ArrayList<Uri> path = imageData.getParcelableArrayListExtra(Define.INTENT_PATH);

                    for (int j = 0; j < path.size(); j++)
                    {
                        switch(j)
                        {
                            case 0:
                                MultipartBody.Part picture1 = prepareFilePart("picture1", path.get(j));
                                pictureList.add(picture1);
                                break;

                            case 1:
                                MultipartBody.Part picture2 = prepareFilePart("picture2", path.get(j));
                                pictureList.add(picture2);
                                break;

                            case 2:
                                MultipartBody.Part picture3 = prepareFilePart("picture3", path.get(j));
                                pictureList.add(picture3);
                                break;

                            case 3:
                                MultipartBody.Part picture4 = prepareFilePart("picture4", path.get(j));
                                pictureList.add(picture4);
                                break;
                        }
                    }

                    if(path.size() < NUMBER_OF_PHOTOS)
                    {
                        mGalleryLayout.addView(getDefaultImageView());
                    }

                    break;
                }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Bitmap decodeFile(File f)
    {
        try
        {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_SIZE=150;

            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE)
            {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private ImageView getNewImageView(Bitmap bm)
    {
        ImageView imageView = new ImageView(getBaseContext());

        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 200));
        setMargins(imageView, 4 , 0 , 4 , 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mGalleryError.getVisibility() == View.VISIBLE )
                {
                    mGalleryError.setVisibility(View.GONE);
                }

                startGalleryActivity();
            }
        });

        Drawable drawable = new BitmapDrawable(getResources(), bm);

        imageView.setImageDrawable(drawable);
        mPictureTestView.setImageDrawable(drawable);

        return imageView;
    }

    public static void setMargins(View v, int left, int top, int right, int bottom)
    {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
        {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
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

        mCreateAdFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mCreateAdFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCreateAdFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    @Override
    public void onFragmentInteraction(String categoryName, String subcategoryName, int idMapCategory)
    {
        if(idMapCategory!= 0)
        {
            this.idMapCategory = idMapCategory;
        }
        else
        {
            Log.e("idMapCategory", "e neasignat");
        }

        if(subcategoryName != null && categoryName != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(categoryName);
            sb.append(" > ");
            sb.append(subcategoryName);

            mCategoryView.setText(sb.toString());
        }
        else
        {
            Log.e("categoryName", "e neasignat");
            Log.e("subcategoryName", "e neasignat");
        }
    }
}
