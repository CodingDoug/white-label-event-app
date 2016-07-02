/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
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

package com.hyperaware.conference.android.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hyperaware.conference.android.BuildConfig;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.fragment.AgendaFragment;
import com.hyperaware.conference.android.fragment.AttendeesFragment;
import com.hyperaware.conference.android.fragment.CompaniesFragment;
import com.hyperaware.conference.android.fragment.DatePickerDialogFragment;
import com.hyperaware.conference.android.fragment.EventInfoFragment;
import com.hyperaware.conference.android.fragment.HomeFragment;
import com.hyperaware.conference.android.fragment.MapsFragment;
import com.hyperaware.conference.android.fragment.SpeakersFragment;
import com.hyperaware.conference.android.fragment.TimePickerDialogFragment;
import com.hyperaware.conference.android.fragment.Titled;
import com.hyperaware.conference.android.fragment.TweetsFragment;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.ui.model.FragmentFactory;
import com.hyperaware.conference.android.ui.model.Section;
import com.hyperaware.conference.android.util.AdjustableClock;
import com.hyperaware.conference.android.util.BundleSerializer;
import com.hyperaware.conference.android.util.TwitterConfig;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.Event;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener,
    FragmentManager.OnBackStackChangedListener,
    ContentHost,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    GoogleApiClient.OnConnectionFailedListener {

    private static final Logger LOGGER = Logging.getLogger(MainActivity.class);

    private static final int RC_SIGN_IN = 1;

    private Bus bus;
    private AdjustableClock clock;

    private GoogleApiClient googleApiClient;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseAuth.AuthStateListener authStateListener = new MyAuthStateListener();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MutexViewGroup navigationHeaderView;
    private AppBarLayout appBarLayout;
    private ActionBar actionBar;

    private Event event;

    private static class ActivityState implements Serializable {
        boolean isNavSelected;
        /** Is the user requesting a signin? If so, will toast success in auth state listener. */
        boolean isRequestingSignin;
    }

    private ActivityState state;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        clock = Singletons.deps.getAdjustableClock();
        bus = Singletons.deps.getBus();
        bus.register(this);

        initSections();
        initViews();
        initFirebase();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        if (!bus.hasRegistered(this)) {
            bus.register(this);
        }
    }

    @Override
    protected void onStop() {
        bus.unregister(this);
        auth.removeAuthStateListener(authStateListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Subscribe
    public void onEvent(final Event event) {
        this.event = event;
    }

    //
    // Activity state
    //

    private void restoreState(final Bundle bundle) {
        if (bundle != null) {
            BundleSerializer<ActivityState> serializer = new BundleSerializer<>();
            state = serializer.deserialize(bundle);
        }
        else {
            state = new ActivityState();
        }
    }

    private void saveState(final Bundle bundle) {
        BundleSerializer<ActivityState> serializer = new BundleSerializer<>();
        serializer.serialize(state, bundle);
    }

    //
    // Views
    //

    private void initViews() {
        setContentView(R.layout.activity_main);
        initAppBar();
        initNavigation();

        // Load the home fragment at launch
        if (! state.isNavSelected) {
            showSection(homeSection);
            state.isNavSelected = true;
        }
    }

    private void initAppBar() {
        appBarLayout = (AppBarLayout) findViewById(R.id.v_app_bar_layout);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.v_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        updateHomeButton();
    }

    private void initNavigation() {
        drawerLayout = (DrawerLayout) findViewById(R.id.v_drawer);

        navigationView = (NavigationView) findViewById(R.id.v_nav);
        navigationView.setNavigationItemSelectedListener(this);
        navigationHeaderView = (MutexViewGroup) navigationView.inflateHeaderView(R.layout.inc_nav_header);
        navigationHeaderView.showViewId(R.id.button_sign_in);
        navigationHeaderView.findViewById(R.id.button_sign_in).setOnClickListener(new SignInOnClickListener());
        navigationHeaderView.findViewById(R.id.button_sign_out).setOnClickListener(new SignOutOnClickListener());

        if (! TwitterConfig.getInstance().isConfigured()) {
            navigationView.getMenu().removeItem(R.id.mi_tweets);
        }
    }

    private void initFirebase() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .build();
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
            .enableAutoManage(MainActivity.this, MainActivity.this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();
    }

    //
    // App bar and navigation
    //

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem item = menu.findItem(R.id.mi_fake_date_time);
        if (item != null) {
            item.setEnabled(BuildConfig.ALLOW_FAKE_TIME);
            item.setVisible(BuildConfig.ALLOW_FAKE_TIME);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return onActionBarHomePressed();
        case R.id.mi_fake_date_time:
            return onMenuFakeDateTimeSelected();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(navigationView)) {
            drawerLayout.closeDrawers();
        }
        else {
            super.onBackPressed();
        }
    }

    private boolean onActionBarHomePressed() {
        // The home button can either open the nav drawer or pop fragments
        if (isHomeActingUp()) {
            drawerLayout.closeDrawer(GravityCompat.START);
            getSupportFragmentManager().popBackStack();
        }
        else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    private boolean onMenuFakeDateTimeSelected() {
        if (event != null) {
            final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezoneName()));
            cal.setTimeInMillis(clock.getCurrentTimeMillis());
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day_of_month = cal.get(Calendar.DAY_OF_MONTH);
            getSupportFragmentManager().beginTransaction()
                .add(DatePickerDialogFragment.instantiate(year, month, day_of_month), "date-picker")
                .commit();
        }
        else {
            Toast.makeText(this, "Event not yet available for TZ", Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    private Section homeSection;
    private Section agendaSection;
    private Section speakersSection;
    private Section attendeesSection;
    private Section mapsSection;
    private Section tweetsSection;
    private Section sponsorsSection;
    private Section eventInfoSection;

    private void initSections() {
        final String title_home = getString(R.string.section_title_home);
        homeSection = new Section(title_home, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return HomeFragment.instantiate(event.getFullName());
            }
        });

        final String title_agenda = getString(R.string.section_title_agenda);
        agendaSection = new Section(title_agenda, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return AgendaFragment.instantiate(title_agenda);
            }
        });

        final String title_speakers = getString(R.string.section_title_speakers);
        speakersSection = new Section(title_speakers, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return SpeakersFragment.instantiate(title_speakers);
            }
        });

        final String title_attendees = getString(R.string.section_title_attendees);
        attendeesSection = new Section(title_attendees, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return AttendeesFragment.instantiate(title_attendees);
            }
        });

        final String title_maps = getString(R.string.section_title_maps);
        mapsSection = new Section(title_maps, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return MapsFragment.instantiate(title_maps);
            }
        });

        final String title_sponsors = getString(R.string.section_title_sponsors);
        sponsorsSection = new Section(title_sponsors, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return CompaniesFragment.instantiate(title_sponsors);
            }
        });

        final String title_event_info = getString(R.string.section_title_event_info);
        eventInfoSection = new Section(title_event_info, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return EventInfoFragment.instantiate();
            }
        });

        final TwitterConfig config = TwitterConfig.getInstance();
        if (config.isConfigured()) {
            final String title_hashtag = getString(R.string.event_hashtag);
            tweetsSection = new Section(title_hashtag, new FragmentFactory() {
                @Override
                public Fragment newFragment() {
                    return TweetsFragment.instantiate(config.getEventHashtag());
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        Section section;

        // TODO include mi_ values in Section and create a mapping
        switch (menuItem.getItemId()) {
        case R.id.mi_home:
            section = homeSection;
            break;
        case R.id.mi_agenda:
            section = agendaSection;
            break;
        case R.id.mi_speakers:
            section = speakersSection;
            break;
        case R.id.mi_attendees:
            section = attendeesSection;
            break;
        case R.id.mi_maps:
            section = mapsSection;
            break;
        case R.id.mi_tweets:
            section = tweetsSection;
            break;
        case R.id.mi_sponsors:
            section = sponsorsSection;
            break;
        case R.id.mi_event_info:
            section = eventInfoSection;
            break;
        default:
            section = null;
            break;
        }

        if (section != null) {
            menuItem.setChecked(true);
            showSection(section);
        }

        return true;
    }

    private void showSection(final Section section) {
        // Showing a section creates a new "root" fragment; everything else is popped off
        final FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
            .replace(R.id.v_content, section.fragmentFactory.newFragment())
            .commit();

        appBarLayout.setExpanded(true, false);
        drawerLayout.closeDrawers();
    }

    @Override
    public void showAgendaAtTime(final long time) {
        final String title = getResources().getString(R.string.section_title_agenda);
        showSection(new Section(title, new FragmentFactory() {
            @Override
            public Fragment newFragment() {
                return AgendaFragment.instantiate(title, time);
            }
        }));
        navigationView.setCheckedItem(R.id.mi_agenda);
    }

    @Override
    public void pushFragment(@NonNull Fragment fragment, @Nullable String name) {
        getSupportFragmentManager().beginTransaction()
            .addToBackStack(name)
            .replace(R.id.v_content, fragment)
            .commit();

        appBarLayout.setExpanded(true, false);
    }

    @Override
    public void setTitle(@Nullable final String name) {
        actionBar.setTitle(name);
    }

    @Override
    public void signIn() {
        startSignIn();
    }

    @Override
    public void onBackStackChanged() {
        updateHomeButton();
        updateTitle();
        appBarLayout.setExpanded(true, false);
    }

    private void updateTitle() {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.v_content);
        if (fragment instanceof Titled) {
            setTitle(((Titled) fragment).getTitle());
        }
    }

    private void updateHomeButton() {
        // Enable Up button only if there are entries in the back stack
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (isHomeActingUp()) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            }
            else {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            }
        }
    }

    private boolean isHomeActingUp() {
        return getSupportFragmentManager().getBackStackEntryCount() > 0;
    }

    //
    // Auth
    //

    private void startSignIn() {
        state.isRequestingSignin = true;
        final Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private class SignInOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startSignIn();
        }
    }

    private class SignOutOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            auth.signOut();
            Auth.GoogleSignInApi.signOut(googleApiClient);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LOGGER.severe("onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    private void handleSignInResult(GoogleSignInResult result) {
        LOGGER.fine("handleSignInResult: " + result.isSuccess());
        LOGGER.fine("handleSignInResult: " + result.getStatus());
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LOGGER.fine("signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            LOGGER.log(Level.SEVERE, "signInWithCredential", task.getException());
                            toastAuthFailed();
                        }
                    }
                });

        }
        else {
            toastAuthFailed();
            navigationHeaderView.showViewId(R.id.button_sign_in);
        }
    }

    private void toastAuthFailed() {
        Toast.makeText(MainActivity.this, R.string.msg_sign_in_failed, Toast.LENGTH_SHORT).show();
    }

    private class MyAuthStateListener implements FirebaseAuth.AuthStateListener {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                LOGGER.fine("onAuthStateChanged: " + user.getUid());
                navigationHeaderView.showViewId(R.id.vg_profile);
                final TextView tv_name = (TextView) navigationHeaderView.findViewById(R.id.tv_name);
                final String name = user.getDisplayName();
                tv_name.setText(name != null ? name : "[No name]");
                final Uri photoUrl = user.getPhotoUrl();
                Glide
                    .with(MainActivity.this)
                    .load(photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.nopic)
                    .into((ImageView) navigationHeaderView.findViewById(R.id.iv_pic));
                if (state.isRequestingSignin) {
                    Toast.makeText(
                        MainActivity.this,
                        getString(R.string.msg_sign_in_thank_you, user.getDisplayName()),
                        Toast.LENGTH_SHORT).show();
                    state.isRequestingSignin = false;
                }
            }
            else {
                LOGGER.fine("onAuthStateChanged: signed out");
                navigationHeaderView.showViewId(R.id.button_sign_in);
                ((TextView) navigationHeaderView.findViewById(R.id.tv_name)).setText(null);
                ((ImageView) navigationHeaderView.findViewById(R.id.iv_pic)).setImageBitmap(null);
            }
        }
    }

    //
    // Fake time fragment ui callbacks
    //

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezoneName()));
        c.setTimeInMillis(clock.getCurrentTimeMillis());
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        clock.setCurrentTimeMillis(c.getTimeInMillis());

        int hour_of_day = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        getSupportFragmentManager().beginTransaction()
            .add(TimePickerDialogFragment.instantiate(hour_of_day, minute), "time-picker")
            .commit();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezoneName()));
        c.setTimeInMillis(clock.getCurrentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        clock.setCurrentTimeMillis(c.getTimeInMillis());
        LOGGER.fine("Date set to " + c.getTime());
    }

}
