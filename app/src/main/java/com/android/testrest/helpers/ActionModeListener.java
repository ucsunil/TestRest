package com.android.testrest.helpers;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by umonssu on 10/9/14.
 */
public class ActionModeListener implements AbsListView.MultiChoiceModeListener {

    Context context;
    ActionMode actionMode;
    ListView listView;

    public ActionModeListener(Context context, ListView listView) {
        this.context = context;
        this.listView = listView;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }
}
