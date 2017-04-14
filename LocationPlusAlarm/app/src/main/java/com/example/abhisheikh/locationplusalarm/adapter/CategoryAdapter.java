package com.example.abhisheikh.locationplusalarm.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.abhisheikh.locationplusalarm.R;
import com.example.abhisheikh.locationplusalarm.fragment.GoogleMapFragment;
import com.example.abhisheikh.locationplusalarm.fragment.PNRFragment;

/**
 * Created by Aman Saurabh on 4/14/2017.
 */

public class CategoryAdapter extends FragmentPagerAdapter {

    private Context mContext;
    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new GoogleMapFragment();
        } else {
            return new PNRFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return mContext.getString(R.string.category_map);
        else {
            return mContext.getString(R.string.category_pnr);
        }
    }
}
