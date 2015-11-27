/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hyperaware.conference.android.BuildConfig;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.Event;
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
import com.hyperaware.conference.android.service.EventDataFetchService;
import com.hyperaware.conference.android.ui.model.FragmentFactory;
import com.hyperaware.conference.android.ui.model.Section;
import com.hyperaware.conference.android.util.AdjustableClock;
import com.hyperaware.conference.android.util.BundleSerializer;
import com.hyperaware.conference.android.util.TwitterConfig;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener,
    FragmentManager.OnBackStackChangedListener,
    ContentHost,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private static final Logger LOGGER = Logging.getLogger(MainActivity.class);

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarLayout appBarLayout;
    private ActionBar actionBar;

    private Bus bus;
    private String hashtag;
    private AdjustableClock clock;

    private Event event;

    private static class ActivityState implements Serializable {
        boolean isNavSelected;
    }

    private ActivityState state;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
        initSections();
        initViews();

        clock = Singletons.deps.getAdjustableClock();

        bus = Singletons.deps.getBus();
        bus.register(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.v_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        updateHomeButton();
    }

    private void initNavigation() {
        drawerLayout = (DrawerLayout) findViewById(R.id.v_drawer);

        navigationView = (NavigationView) findViewById(R.id.v_nav);
        navigationView.setNavigationItemSelectedListener(this);

        if (! TwitterConfig.getInstance().isConfigured()) {
            navigationView.getMenu().removeItem(R.id.mi_tweets);
        }
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
        case R.id.mi_refresh:
            return onMenuRefreshSelected();
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

    private boolean onMenuRefreshSelected() {
        EventDataFetchService.start(this);
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
                return HomeFragment.instantiate(getString(R.string.event_name));
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
            tweetsSection = new Section(hashtag, new FragmentFactory() {
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

    private void showSection(Section section) {
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
    public void setTitle(@Nullable String name) {
        actionBar.setTitle(name);
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
    // Event subscribers
    //

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        event = data.event;
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
        LOGGER.fine("Date set to " + c.getTime());

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
