package jp.kshoji.jfuguesample;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import jp.kshoji.jfuguesample.fragment.IntroductionFragment_;
import jp.kshoji.jfuguesample.util.AssetUtils;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // generated unique id
    private static final int MENU_ITEM_0 = 0xD7B0E7E4;

    private MenuItem bluetoothMenuItem;
    private MenuItem bleCentralMenuItem;
    private MenuItem blePeripheralMenuItem;

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
        final TextView textView = (TextView) navigationView.findViewById(R.id.textView);
        final Spannable content = new SpannableString(textView.getText());
        content.setSpan(new UnderlineSpan(), 0, textView.getText().length(), 0);
        textView.setText(content);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github))));
            }
        });

        // setup menu items dynamically
        final int menuSize = AssetUtils.getAssetFileCount(this, "titles");
        for (int i = 0; i < menuSize; i++) {
            navigationView.getMenu().add(R.id.group01, MENU_ITEM_0 + i, 1, AssetUtils.getAssetFileAsString(this, String.format("titles/%02d.txt", i)));
        }
        navigationView.getMenu().setGroupCheckable(R.id.group01, true, true);

        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_intro));
        navigationView.setNavigationItemSelectedListener(this);

        if (usbMidiSystem == null) {
            usbMidiSystem = new UsbMidiSystem(getApplicationContext());
        }

        if (BleUtils.isBleSupported(getApplicationContext())) {
            if (BleUtils.isBluetoothEnabled(getApplicationContext())) {
                if (bleMidiSystem == null) {
                    bleMidiSystem = new BleMidiSystem(getApplicationContext());
                    bleMidiSystem.initialize();
                }
            }
        }

        registerReceiver(bluetoothStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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

        unregisterReceiver(bluetoothStatusReceiver);
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

    private final BroadcastReceiver bluetoothStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_ON:
                        if (BleUtils.isBleSupported(getApplicationContext())) {
                            if (BleUtils.isBluetoothEnabled(getApplicationContext())) {
                                if (bleMidiSystem == null) {
                                    bleMidiSystem = new BleMidiSystem(getApplicationContext());
                                    bleMidiSystem.initialize();
                                }
                            } else {
                                if (bleMidiSystem != null) {
                                    bleMidiSystem.stopScanDevice();
                                    if (BleUtils.isBlePeripheralSupported(getApplicationContext())) {
                                        bleMidiSystem.stopAdvertising();
                                    }
                                    bleMidiSystem.terminate();
                                    bleMidiSystem = null;
                                }
                            }
                        }
                        applyBluetoothStatusToMenu();
                        break;
                }
            }
        }
    };

    /**
     * apply menu status
     */
    private void applyBluetoothStatusToMenu() {
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        if (BleUtils.isBleSupported(getApplicationContext())) {
            bluetoothMenuItem.setTitle(R.string.enable_bluetooth);
            bluetoothMenuItem.setVisible(!BleUtils.isBluetoothEnabled(getApplicationContext()));
        } else {
            bluetoothMenuItem.setTitle(R.string.ble_not_supported);
            bluetoothMenuItem.setVisible(false);
        }

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

        bluetoothMenuItem = menu.getItem(1);
        bleCentralMenuItem = menu.getItem(2);
        blePeripheralMenuItem = menu.getItem(3);

        applyBluetoothStatusToMenu();
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
            case R.id.action_enable_bluetooth:
                if (!BleUtils.isBluetoothEnabled(getApplicationContext())) {
                    BleUtils.enableBluetooth(this);
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

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        item.setChecked(true);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github))));
                break;
            case R.id.nav_jfugue:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_jfugue))));
                break;
            case R.id.nav_intro:
                fragmentManager.beginTransaction().replace(R.id.frame_container, new IntroductionFragment_()).commit();
                break;

            default:
                try {
                    final String packageName = AbstractExampleFragment.class.getPackage().getName();
                    final String className = String.format(packageName + ".Example%02dFragment_", item.getItemId() - MENU_ITEM_0);
                    fragment = (AbstractExampleFragment)Class.forName(className).newInstance();
                    fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
                } catch (final Exception ignored) {
                }
                break;
        }

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
