package com.hyperaware.conference.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyperaware.conference.android.R;

public class AboutFragment extends Fragment implements Titled {

    public static Fragment instantiate() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.section_title_about);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

}
