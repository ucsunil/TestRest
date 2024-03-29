package com.android.testrest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.testrest.requestfragments.DeleteFragment;
import com.android.testrest.requestfragments.GetFragment;
import com.android.testrest.requestfragments.PostFragment;
import com.android.testrest.requestfragments.PutFragment;


public class RestHome extends Activity implements AdapterView.OnItemClickListener {

    private DrawerLayout drawerLayout = null;
    private ActionBarDrawerToggle toggle = null;
    private ListView choices;
    private GetFragment getFragment = null;
    private PostFragment postFragment = null;
    private PutFragment putFragment = null;
    private DeleteFragment deleteFragment = null;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_home);

        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fr = fragmentManager.findFragmentById(R.id.content);
                if(fr!=null){
                    Log.e("fragment=", fr.getClass().getSimpleName());
                }
            }
        });
        choices = (ListView) findViewById(R.id.choices);
        choices.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_row, RestTestApplication.requests));
        choices.setOnItemClickListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        toggle.onConfigurationChanged(configuration);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if(position == 0) {
            showGetFragment();
        } else if(position == 1) {
            showPostFragment();
        } else if(position == 2) {
            showPutFragment();
        } else if(position == 3) {
            showDeleteFragment();
        }
        drawerLayout.closeDrawers();
    }

    private void showGetFragment() {
        if(getFragment == null) {
            getFragment = GetFragment.newInstance();
        }
        if(!getFragment.isVisible()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, getFragment).commit();
            //transaction.addToBackStack(null);
            //transaction.commit();
        }
    }

    private void showPostFragment() {

        if(postFragment == null) {
            postFragment = PostFragment.newInstance();
        }
        if(!postFragment.isVisible()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, postFragment).commit();
            //transaction.addToBackStack(null);
            //transaction.commit();
        }
    }

    private void showPutFragment() {
        if(putFragment == null) {
            putFragment = PutFragment.newInstance();
        }
        if(!putFragment.isVisible()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, putFragment).commit();
            //transaction.addToBackStack(null);
            //transaction.commit();
        }
    }

    private void showDeleteFragment() {
        if(deleteFragment == null) {
            deleteFragment = DeleteFragment.newInstance();
        }
        if(!deleteFragment.isVisible()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, deleteFragment).commit();
            //transaction.addToBackStack(null);
            //transaction.commit();
        }
    }

}
