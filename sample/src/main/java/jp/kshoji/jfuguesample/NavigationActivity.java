package jp.kshoji.jfuguesample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import jp.kshoji.blemidi.util.BleUtils;
import jp.kshoji.javax.sound.midi.BleMidiSystem;
import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kshoji.jfuguesample.fragment.AbstractExampleFragment;
import jp.kshoji.jfuguesample.fragment.Example00Fragment_;
import jp.kshoji.jfuguesample.fragment.Example01Fragment_;
import jp.kshoji.jfuguesample.fragment.Example02Fragment_;
import jp.kshoji.jfuguesample.fragment.Example03Fragment_;
import jp.kshoji.jfuguesample.fragment.Example04Fragment_;
import jp.kshoji.jfuguesample.fragment.Example05Fragment_;
import jp.kshoji.jfuguesample.fragment.Example06Fragment_;
import jp.kshoji.jfuguesample.fragment.Example07Fragment_;
import jp.kshoji.jfuguesample.fragment.Example08Fragment_;
import jp.kshoji.jfuguesample.fragment.Example09Fragment_;
import jp.kshoji.jfuguesample.fragment.Example10Fragment_;
import jp.kshoji.jfuguesample.fragment.Example11Fragment_;
import jp.kshoji.jfuguesample.fragment.Example12Fragment_;
import jp.kshoji.jfuguesample.fragment.Example13Fragment_;
import jp.kshoji.jfuguesample.fragment.Example14Fragment_;
import jp.kshoji.jfuguesample.fragment.Example15Fragment_;
import jp.kshoji.jfuguesample.fragment.Example16Fragment_;
import jp.kshoji.jfuguesample.fragment.Example17Fragment_;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Nullable
    private UsbMidiSystem usbMidiSystem;
    @Nullable
    private BleMidiSystem bleMidiSystem;
    private AbstractExampleFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        final FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (fragment != null) {
                    fragment.start();
                    Snackbar.make(view, R.string.executing_sample, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.stop, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    fragment.stop();
                                }
                            }).show();
                }
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // make url text linkable
        final TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        final Spannable content = new SpannableString(textView.getText());
        content.setSpan(new UnderlineSpan(), 0, textView.getText().length(), 0);
        textView.setText(content);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github))));
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_ex_00);

        if (usbMidiSystem == null) {
            usbMidiSystem = new UsbMidiSystem(getApplicationContext());
        }

        if (bleMidiSystem == null) {
            if (BleUtils.isBleSupported(getApplicationContext())) {
                bleMidiSystem = new BleMidiSystem(getApplicationContext());
                if (BleUtils.isBluetoothEnabled(getApplicationContext())) {
                    bleMidiSystem.initialize();
                }
            }
        }

        fragment = new Example00Fragment_();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment).commit();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (usbMidiSystem != null) {
            usbMidiSystem.terminate();
            usbMidiSystem = null;
        }
        if (bleMidiSystem != null) {
            if (BleUtils.isBleSupported(getApplicationContext())) {
                bleMidiSystem.stopScanDevice();
                if (BleUtils.isBlePeripheralSupported(getApplicationContext())) {
                    bleMidiSystem.stopAdvertising();
                }
                bleMidiSystem.terminate();
                bleMidiSystem = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);

        // restore check status
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        final MenuItem usbMidiMenuItem = menu.getItem(0);
        usbMidiMenuItem.setChecked(preferences.getBoolean(getString(R.string.preference_usb_midi_enable), false));
        if (usbMidiSystem != null) {
            if (usbMidiMenuItem.isChecked()) {
                usbMidiSystem.initialize();
            } else {
                usbMidiSystem.terminate();
            }
        }

        final MenuItem bleCentralMenuItem = menu.getItem(1);
        final MenuItem blePeripheralMenuItem = menu.getItem(2);
        if (BleUtils.isBleSupported(getApplicationContext()) && BleUtils.isBluetoothEnabled(getApplicationContext())) {
            bleCentralMenuItem.setVisible(true);
            bleCentralMenuItem.setChecked(preferences.getBoolean(getString(R.string.preference_ble_midi_central_enable), false));
            if (bleMidiSystem != null) {
                if (bleCentralMenuItem.isChecked()) {
                    bleMidiSystem.startScanDevice();
                } else {
                    bleMidiSystem.stopScanDevice();
                }
            }

            if (BleUtils.isBlePeripheralSupported(getApplicationContext())) {
                blePeripheralMenuItem.setVisible(true);
                blePeripheralMenuItem.setChecked(preferences.getBoolean(getString(R.string.preference_ble_midi_peripheral_enable), false));
                if (bleMidiSystem != null) {
                    if (blePeripheralMenuItem.isChecked()) {
                        bleMidiSystem.startAdvertising();
                    } else {
                        bleMidiSystem.stopAdvertising();
                    }
                }
            } else {
                blePeripheralMenuItem.setVisible(false);
            }
        } else {
            bleCentralMenuItem.setVisible(false);
            blePeripheralMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        // remember check status
        final SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        switch (id) {
            case R.id.action_enable_usb:
                item.setChecked(!item.isChecked());
                if (usbMidiSystem != null) {
                    editor.putBoolean(getString(R.string.preference_usb_midi_enable), item.isChecked());
                    editor.apply();
                    if (item.isChecked()) {
                        usbMidiSystem.initialize();
                    } else {
                        usbMidiSystem.terminate();
                    }
                }
                return true;
            case R.id.action_enable_ble_central:
                item.setChecked(!item.isChecked());
                if (BleUtils.isBleSupported(getApplicationContext()) && //
                        BleUtils.isBluetoothEnabled(getApplicationContext())) {
                    if (bleMidiSystem != null) {
                        editor.putBoolean(getString(R.string.preference_ble_midi_central_enable), item.isChecked());
                        editor.apply();
                        if (item.isChecked()) {
                            bleMidiSystem.startScanDevice();
                        } else {
                            bleMidiSystem.stopScanDevice();
                        }
                    }
                }
                return true;
            case R.id.action_enable_ble_peripheral:
                item.setChecked(!item.isChecked());
                if (BleUtils.isBleSupported(getApplicationContext()) && //
                        BleUtils.isBluetoothEnabled(getApplicationContext()) && //
                        BleUtils.isBlePeripheralSupported(getApplicationContext())) {
                    if (bleMidiSystem != null) {
                        editor.putBoolean(getString(R.string.preference_ble_midi_peripheral_enable), item.isChecked());
                        editor.apply();
                        if (item.isChecked()) {
                            bleMidiSystem.startAdvertising();
                        } else {
                            bleMidiSystem.stopAdvertising();
                        }
                    }
                }
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void setFragmentByItemId(final int itemId) {
        boolean fragmentChanged = true;
        switch (itemId) {
            case R.id.nav_ex_00:
                fragment = new Example00Fragment_();
                break;
            case R.id.nav_ex_01:
                fragment = new Example01Fragment_();
                break;
            case R.id.nav_ex_02:
                fragment = new Example02Fragment_();
                break;
            case R.id.nav_ex_03:
                fragment = new Example03Fragment_();
                break;
            case R.id.nav_ex_04:
                fragment = new Example04Fragment_();
                break;
            case R.id.nav_ex_05:
                fragment = new Example05Fragment_();
                break;
            case R.id.nav_ex_06:
                fragment = new Example06Fragment_();
                break;
            case R.id.nav_ex_07:
                fragment = new Example07Fragment_();
                break;
            case R.id.nav_ex_08:
                fragment = new Example08Fragment_();
                break;
            case R.id.nav_ex_09:
                fragment = new Example09Fragment_();
                break;
            case R.id.nav_ex_10:
                fragment = new Example10Fragment_();
                break;
            case R.id.nav_ex_11:
                fragment = new Example11Fragment_();
                break;
            case R.id.nav_ex_12:
                fragment = new Example12Fragment_();
                break;
            case R.id.nav_ex_13:
                fragment = new Example13Fragment_();
                break;
            case R.id.nav_ex_14:
                fragment = new Example14Fragment_();
                break;
            case R.id.nav_ex_15:
                fragment = new Example15Fragment_();
                break;
            case R.id.nav_ex_16:
                fragment = new Example16Fragment_();
                break;
            case R.id.nav_ex_17:
                fragment = new Example17Fragment_();
                break;
            default:
                fragmentChanged = false;
                break;
        }

        if (fragmentChanged) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        setFragmentByItemId(item.getItemId());

        switch (item.getItemId()) {
            case R.id.nav_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github))));
                break;
            case R.id.nav_jfugue:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_jfugue))));
                break;
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
