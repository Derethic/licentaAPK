package com.cti.fmi.licentaapk.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.interfaces.UserService;
import com.cti.fmi.licentaapk.models.CategoryDisplay;
import com.cti.fmi.licentaapk.services.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends DialogFragment
{
    private final static String PREFERENCES = "MySharedPrefs";
    private final DialogFragment dialogFragment = this;
    private OnFragmentInteractionListener mListener;

    private LinearLayout mLinearListView;
    private ViewGroup container;
    boolean isCategoryViewClicked=false;

    public CategoryFragment()
    {

    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(String categoryName, String subcategoryName, int idMapCategory);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {


        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View root = inflater.inflate(R.layout.fragment_category_list, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //Customizing the dialog features
        final Dialog dialog = new Dialog(getActivity());

        if(dialog.getWindow() != null )
        {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(root);
            dialog.getWindow().
                    setBackgroundDrawable(
                            new ColorDrawable(
                                    ContextCompat.getColor(getActivity().getApplicationContext(),
                                            R.color.colorSecondary)
                            )
                    );

            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        else
        {
            Log.e("Dialog get window", " e null");
        }
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CategoryFragmentTheme);

        View rootView = inflater.inflate(R.layout.fragment_category_list, container, false);
        mLinearListView = rootView.findViewById(R.id.linear_category_listview);

        this.container = container;

        // TODO: Request pentru categories
        getCategories();

        // Inflate the layout for this fragment
        return rootView;
    }


    private boolean getCategories()
    {
        SharedPreferences sharedPrefs = this.getActivity()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        final String authToken = sharedPrefs.getString("auth-token", "");

        if (!authToken.equals(""))
        {
            UserService userService = ServiceGenerator.createService(UserService.class);
            Call<ArrayList<CategoryDisplay>> call = userService.getCategories("Token " + authToken);

            call.enqueue(new Callback<ArrayList<CategoryDisplay>>()
            {
                @Override
                public void onResponse(@NonNull Call<ArrayList<CategoryDisplay>> call,
                                       @NonNull Response<ArrayList<CategoryDisplay>> response)
                {
                    if (response.isSuccessful())
                    {
                        ArrayList<CategoryDisplay> categoryList = response.body();

                        for (int i = 0; i < categoryList.size(); i++)
                        {
                            final CategoryDisplay currentCategory = categoryList.get(i);

                            LayoutInflater categoryInflater = (LayoutInflater) getActivity()
                                    .getApplicationContext()
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View mCategoryRowView = categoryInflater.inflate(R.layout.category_row_first, container, false);

                            final RelativeLayout mLinearCategoryRowLayout = mCategoryRowView.findViewById(R.id.linear_category_row_layout);
                            final TextView mLinearCategoryName = mCategoryRowView.findViewById(R.id.linear_category_name);
                            final ImageView mLinearCategoryArrow = mCategoryRowView.findViewById(R.id.linear_category_arrow);
                            final LinearLayout mLinearCategoryScroll = mCategoryRowView.findViewById(R.id.linear_category_scroll);

                            //checkes if menu is already opened or not
                            if(!isCategoryViewClicked)
                            {
                                mLinearCategoryScroll.setVisibility(View.GONE);
                                mLinearCategoryArrow.setBackgroundResource(R.drawable.arrow_forward);
                            }
                            else
                            {
                                mLinearCategoryScroll.setVisibility(View.VISIBLE);
                                mLinearCategoryArrow.setBackgroundResource(R.drawable.arrow_down);
                            }
                            //Handles onclick effect on list item
                            mLinearCategoryRowLayout.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    if(!isCategoryViewClicked)
                                    {
                                        isCategoryViewClicked=true;
                                        mLinearCategoryArrow.setBackgroundResource(R.drawable.arrow_down);
                                        mLinearCategoryScroll.setVisibility(View.VISIBLE);

                                    }
                                    else
                                    {
                                        isCategoryViewClicked=false;
                                        mLinearCategoryArrow.setBackgroundResource(R.drawable.arrow_forward);
                                        mLinearCategoryScroll.setVisibility(View.GONE);
                                    }
                                }
                            });

                            final String name = categoryList.get(i).getCategoryName();
                            mLinearCategoryName.setText(name);

                            for (int j = 0; j < categoryList.get(i).getSubcategories().size(); j++)
                            {
                                final CategoryDisplay.SubcategoryDisplay currentSubcategory = currentCategory.getSubcategories().get(j);

                                LayoutInflater subcategoryInflater = (LayoutInflater) getActivity()
                                        .getApplicationContext()
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                View mSubcategoryRowView = subcategoryInflater.inflate(R.layout.category_row_second, container, false);
                                mSubcategoryRowView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        onSubcategoryPressed(currentCategory.getCategoryName(),
                                                currentSubcategory.getSubcategoryName(),
                                                currentSubcategory.getIdMapCategory()
                                        );
                                        dialogFragment.dismiss();
                                    }
                                });

                                final TextView mLinearSubcategoryName = mSubcategoryRowView.findViewById(R.id.linear_subcategory_name);

                                final String subcategoryName = categoryList.get(i).getSubcategories().get(j).getSubcategoryName();
                                mLinearSubcategoryName.setText(subcategoryName);

                                mLinearCategoryScroll.addView(mSubcategoryRowView);
                            }

                            mLinearListView.addView(mCategoryRowView);
                        }
                    }
                    else
                    {
                        Log.e("error code" , String.valueOf(response.code()));
                        Log.e("authToken" , authToken);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<CategoryDisplay>> call,
                                      @NonNull Throwable t)
                {
                    Log.d("Error categories", t.getMessage());
                }
            });
        }
        return true;

    }

    public void onSubcategoryPressed(String categoryName, String subcategoryName, int idMapCategory)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(categoryName, subcategoryName, idMapCategory);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

//    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//    private void showProgress(final boolean show)
//    {
//        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//        mCategoryFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
//        mCategoryFormView.animate().setDuration(shortAnimTime).alpha(
//                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mCategoryFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
//            }
//        });
//
//        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//        mProgressView.animate().setDuration(shortAnimTime).alpha(
//                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            }
//        });
//    }
}
