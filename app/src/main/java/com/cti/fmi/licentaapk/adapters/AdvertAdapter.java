package com.cti.fmi.licentaapk.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cti.fmi.licentaapk.R;
import com.cti.fmi.licentaapk.models.Advert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AdvertAdapter extends RecyclerView.Adapter<AdvertAdapter.ViewHolder>
{
    private String API_URL = "http://192.168.0.102:8000";

    private Context context;
    private ArrayList<Advert> advertList;

    public AdvertAdapter(Context context, ArrayList<Advert> advertList)
    {
        this.context = context;
        this.advertList = advertList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.advert_card_layout,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.mTitleTextView.setText(advertList.get(position).getTitle());
        holder.mPriceTextView.setText(String.valueOf(advertList.get(position).getPrice()));
        holder.mCurrencyTextView.setText(advertList.get(position).getCurrency());
        Glide.with(context).load(API_URL + advertList.get(position).getPicture1()).into(holder.imageViewPicture1);

        try
        {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            String advertDateCreatedAt = advertList.get(position).getCreatedAt();
            // "2017-08-26T11:54:11.865596Z"
            Date advertDate = sourceFormat.parse(advertDateCreatedAt);

            Date now = new Date();

            Calendar calendarNow = Calendar.getInstance();
            calendarNow.setTime(now);

            Calendar calendarAdvertDate = Calendar.getInstance();
            calendarAdvertDate.setTime(advertDate);

            int dayNow = calendarNow.get(Calendar.DAY_OF_MONTH);
            int dayDest = calendarAdvertDate.get(Calendar.DAY_OF_MONTH);

            if(dayNow - dayDest == 0)
            {
                TimeZone defaultTimeZone = TimeZone.getDefault();
                // Summer Time Zone
                TimeZone localTimeZone = TimeZone.getTimeZone(defaultTimeZone
                        .getDisplayName(defaultTimeZone.inDaylightTime(now),TimeZone.SHORT));

                SimpleDateFormat destFormat = new SimpleDateFormat("'Today' HH:mm", Locale.US);
                destFormat.setTimeZone(localTimeZone);

                String advertDateString =  destFormat.format(advertDate);

                StringBuilder sb = new StringBuilder();
                sb.append(advertDateString);
                sb.append(" - ");
                sb.append(advertList.get(position).getUser().getCountryName());

                holder.mDateTextView.setText(sb.toString());
            }
            else if(dayNow - dayDest == 1)
            {
                TimeZone defaultTimeZone = TimeZone.getDefault();
                // Summer Time Zone
                TimeZone localTimeZone = TimeZone.getTimeZone(defaultTimeZone
                        .getDisplayName(defaultTimeZone.inDaylightTime(now),TimeZone.SHORT));

                SimpleDateFormat destFormat = new SimpleDateFormat("'Yesterday' HH:mm", Locale.US);
                destFormat.setTimeZone(localTimeZone);

                String advertDateString =  destFormat.format(advertDate);

                StringBuilder sb = new StringBuilder();
                sb.append(advertDateString);
                sb.append(" - ");
                sb.append(advertList.get(position).getUser().getCountryName());

                holder.mDateTextView.setText(sb.toString());
            }
            else
            {
                TimeZone defaultTimeZone = TimeZone.getDefault();
                // Summer Time Zone
                TimeZone localTimeZone = TimeZone.getTimeZone(defaultTimeZone
                        .getDisplayName(defaultTimeZone.inDaylightTime(now),TimeZone.SHORT));

                SimpleDateFormat destFormat = new SimpleDateFormat("dd MMMM HH:mm", Locale.US);
                destFormat.setTimeZone(localTimeZone);

                String advertDateString =  destFormat.format(advertDate);

                StringBuilder sb = new StringBuilder();
                sb.append(advertDateString);
                sb.append(" - ");
                sb.append(advertList.get(position).getUser().getCountryName());

                holder.mDateTextView.setText(sb.toString());
            }
        }
        catch(ParseException e)
        {
            Log.e("parseErrorDate", e.getMessage());
        }
    }

    @Override
    public int getItemCount()
    {
        return advertList.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder
    {
        public ImageView imageViewPicture1;
        public TextView mTitleTextView;
        public TextView mPriceTextView;
        public TextView mCurrencyTextView;
        public TextView mDateTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.advert_cardview_title);
            mPriceTextView = itemView.findViewById(R.id.advert_cardview_price);
            mCurrencyTextView = itemView.findViewById(R.id.advert_cardview_currency);
            imageViewPicture1 = itemView.findViewById(R.id.advert_cardview_main_picture);
            mDateTextView = itemView.findViewById(R.id.advert_cardview_date);
        }
    }


}

