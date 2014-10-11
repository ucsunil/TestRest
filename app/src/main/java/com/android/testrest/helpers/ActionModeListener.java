package com.android.testrest.helpers;

import android.app.Activity;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.testrest.R;
import com.android.testrest.requestfragments.DeleteFragment;
import com.android.testrest.requestfragments.GetFragment;
import com.android.testrest.requestfragments.PostFragment;
import com.android.testrest.requestfragments.PutFragment;

/**
 * Created by umonssu on 10/9/14.
 */
public class ActionModeListener implements AbsListView.MultiChoiceModeListener {

    Activity context;
    ActionMode actionMode;
    ListView listView;
    GetFragment getFragment;
    PostFragment postFragment;
    PutFragment putFragment;
    DeleteFragment deleteFragment;
    private int fragmentFlag;

    public ActionModeListener(Activity context, Fragment fragment, ListView listView, int fragmentFlag) {
        this.context = context;
        this.listView = listView;
        this.fragmentFlag = fragmentFlag;
        if(fragmentFlag == 1) {
            this.getFragment = (GetFragment) fragment;
        } else if(fragmentFlag == 2) {
            this.postFragment = (PostFragment) fragment;
        } else if(fragmentFlag == 3) {
            this.putFragment = (PutFragment) fragment;
        } else if(fragmentFlag == 4) {
            this.deleteFragment = (DeleteFragment) fragment;
        }

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = context.getMenuInflater();
        inflater.inflate(R.menu.context, menu);
        actionMode = mode;
        return(true);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        boolean result = false;
        if(fragmentFlag == 1) {
            result = getFragment.performActions(menuItem);
        } else if(fragmentFlag == 2) {
            result = postFragment.performActions(menuItem);
        } else if(fragmentFlag == 3) {
            result = putFragment.performActions(menuItem);
        } else if(fragmentFlag == 4) {
            result = deleteFragment.performActions(menuItem);
        }
        actionMode.finish();
        return result;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

}
