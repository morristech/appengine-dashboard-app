/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.friedran.appengine.dashboard.gui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerAccountsList;
    private ListView mDrawerApplicationsList;
    private ActionBarDrawerToggle mDrawerToggle;
    private AppEngineDashboardClient mAppEngineClient;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        Context applicationContext = getApplicationContext();
        AccountManager accountManager = AccountManager.get(applicationContext);

        Account[] accounts = accountManager.getAccounts();
        List<String> accountNames = new ArrayList<String>();
        for (Account account : accounts) {
            accountNames.add(account.name);
        }
        mAppEngineClient = AppEngineDashboardAPI.getInstance().getClient(accounts[0]);
        List<String> applicationsList = mAppEngineClient.getLastRetrievedApplications();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerAccountsList = (ListView) findViewById(R.id.drawer_accounts);
        mDrawerAccountsList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_accounts_list_item, accountNames));
        mDrawerAccountsList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectAccountItem(position);
                updateUIWithChosenParameters();
            }
        });

        mDrawerApplicationsList = (ListView) findViewById(R.id.drawer_applications);
        mDrawerApplicationsList.setAdapter(
                new ArrayAdapter<String>(this, R.layout.drawer_applications_list_item, applicationsList));
        mDrawerApplicationsList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectApplicationItem(position);
                updateUIWithChosenParameters();
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                return super.onOptionsItemSelected(item);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Mark the default account
        if (savedInstanceState == null) {
            selectAccountItem(0);
            selectApplicationItem(0);
        }
        updateUIWithChosenParameters();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setHomeButtonEnabled(true);
    }

    private void selectAccountItem(int position) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (mDrawerAccountsList.getSelectedItemPosition() != position)
            mDrawerApplicationsList.setItemChecked(0, true);

        mDrawerAccountsList.setItemChecked(position, true);
    }

    private void selectApplicationItem(int position) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        mDrawerApplicationsList.setItemChecked(position, true);
    }

    private void updateUIWithChosenParameters() {
        String selectedAccount = getNavigationListCheckedItem(mDrawerAccountsList);
        String selectedApp = getNavigationListCheckedItem(mDrawerApplicationsList);

        updateActionBarTitleFromNavigation(selectedAccount, selectedApp);
        updateLoadFragmentFromNavigation(selectedAccount, selectedApp);
    }

    private void updateActionBarTitleFromNavigation(String selectedAccount, String selectedApp) {
        if (selectedAccount==null || selectedApp==null) {
            Log.e("DashboardActivity", "No selected Account/App");
            return;
        }

        getActionBar().setTitle(selectedAccount);
        getActionBar().setSubtitle(selectedApp);
    }

    private void updateLoadFragmentFromNavigation(String selectedAccount, String selectedApp) {
        // TODO: Currently ignores the account
        Fragment dashboardLoadFragment = new DashboardLoadFragment(
                mAppEngineClient, selectedApp);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, dashboardLoadFragment).commit();
    }

    private static String getNavigationListCheckedItem(ListView listView) {
        int checkedPosition = listView.getCheckedItemPosition();
        if (checkedPosition == ListView.INVALID_POSITION)
            return null;

        return (String) listView.getItemAtPosition(checkedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
