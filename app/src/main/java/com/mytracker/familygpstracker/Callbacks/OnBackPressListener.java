package com.mytracker.familygpstracker.Callbacks;


import com.mytracker.familygpstracker.Fragments.RootFragment;


public interface OnBackPressListener {

    public boolean onBackPressed();

    public RootFragment getFragment();

}
