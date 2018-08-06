package io.github.oxmose.passlock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;

import io.github.oxmose.passlock.data.Session;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.fragments.AddPasswordFragment;
import io.github.oxmose.passlock.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AddPasswordFragment.OnFragmentInteractionListener {

    private TextView passwordsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset != 0) {

                    passwordsCount.setText(Session.getInstance().getCurrentUser().getPasswordCount() + "");
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String decryptionKey = intent.getStringExtra("decryptionKey");

        /* Create the user session */
        if(!createUserSession(username, decryptionKey)) {
            Toast.makeText(this, "Cannot retrieve user information", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        /* Init UI */
        initDrawerHeader();

        if(null == savedInstanceState) {
            /* Init fragment management */
            initFragments();
        }
    }

    private void initFragments() {
        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.activity_main_fragment_placeholder, new SearchFragment());
        ft.commit();
    }

    private void initDrawerHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        CircularImageView headerIcon = headerView.findViewById(R.id.nav_header_main_circularimageview);
        TextView usernameTextView = headerView.findViewById(R.id.nav_header_main_title_textview);
        TextView headerSubtitle = headerView.findViewById(R.id.nav_header_main_subtitle_textview);
        passwordsCount = headerView.findViewById(R.id.nav_header_main_passwd_count_textview);

        /* Get user */
        User user = Session.getInstance().getCurrentUser();

        String iconPath = user.getAvatar();

        /* If no image is set, display the default one */
        if(iconPath.isEmpty()) {
            int id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_account_circle",
                            null, null);
            headerIcon.setImageResource(id);
        }
        else {
            File imgFile = new  File(iconPath);

            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                headerIcon.setImageBitmap(myBitmap);
            }
            else {
                int id = getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_account_circle",
                                null, null);
                headerIcon.setImageResource(id);
            }
        }

        usernameTextView.setText(user.getUsername());
        if(user.isPrincipal())
            headerSubtitle.setText(R.string.princ_account);
        else
            headerSubtitle.setText(R.string.sec_account);

        passwordsCount.setText(user.getPasswordCount() + "");

    }

    private boolean createUserSession(String username, String decryptionKey) {
        /* Get user data */
        DatabaseSingleton db = DatabaseSingleton.getInstance();
        User user = db.getUser(username);

        if(user == null)
            return false;

        user.setDecryptionKey(decryptionKey);

        /* Add user to session */
        Session.getInstance().setCurrentUser(user);

        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


        if (id == R.id.nav_search) {
            // Replace the contents of the container with the new fragment
            ft.replace(R.id.activity_main_fragment_placeholder, new SearchFragment());
        } else if (id == R.id.nav_add) {
            // Replace the contents of the container with the new fragment
            ft.replace(R.id.activity_main_fragment_placeholder, new AddPasswordFragment());
        } else if (id == R.id.nav_passwords) {

        } else if (id == R.id.nav_pins) {

        } else if (id == R.id.nav_digicodes) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

            logout();

            Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        ft.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        Session.getInstance().setCurrentUser(null);
    }


    @Override
    public void updatePasswordCount() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView passwordsCount = headerView.findViewById(R.id.nav_header_main_passwd_count_textview);

        User user = Session.getInstance().getCurrentUser();

        /* Update UI */
        passwordsCount.setText(user.getPasswordCount() + "");
        passwordsCount.invalidate();
    }
}
