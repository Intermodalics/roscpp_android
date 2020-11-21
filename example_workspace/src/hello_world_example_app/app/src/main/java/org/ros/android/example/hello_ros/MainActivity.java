package org.ros.android.example.hello_ros;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Runnable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("native-activity");
    }

    private static final String IP_REGEX_EXPRESSION =
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private final String TAG = "HELLO-WORLD-EXAMPLE";

    private enum Status {
        WAITING, RUNNING
    }

    private class RosThread implements Runnable {
        public RosThread() {
            Log.i(TAG, "calling __RosThread");
            __RosThread();
            Log.i(TAG, "ended __RosThread");
        }
        @Override
        public native void run();
        public native void stop();
        public native boolean checkRosMaster(List<Pair<String, String>> remappings);
        private native void __RosThread();
    }

    private RosThread mainThread;
    private EditText masterIP;
    private EditText masterPort;
    private CheckBox myIPCheckBox;
    private Spinner myIP;
    private EditText remappingArgs;
    private Button runButton;
    private TextView statusText;
    private Status status;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logging);

        masterIP = (EditText) findViewById(R.id.master_ip);
        masterPort = (EditText) findViewById(R.id.master_port);
        myIPCheckBox = (CheckBox) findViewById(R.id.my_ip_checkbox);
        myIP = (Spinner) findViewById(R.id.my_ip);
        remappingArgs = (EditText) findViewById(R.id.remapping_args);
        runButton = (Button) findViewById(R.id.run_button);
        statusText = (TextView) findViewById(R.id.status);
        status = Status.WAITING;

        mainThread = new RosThread();

        // Handle shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String masterIPPreference = sharedPreferences.getString(getString(R.string.pref_master_ip_key),
                getResources().getString(R.string.pref_master_ip_default));
        masterIP.setText(masterIPPreference);

        String masterPortPreference = sharedPreferences.getString(getString(R.string.pref_master_port_key),
                getResources().getString(R.string.pref_master_port_default));
        masterPort.setText(masterPortPreference);

        String remappingsPreference = sharedPreferences.getString(getString(R.string.pref_remappings_key),
                getResources().getString(R.string.pref_remappings_default));
        remappingArgs.setText(remappingsPreference);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runButtonCallback();
            }
        });

        List<String> ipAddressList = getLocalIpAddresses();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, ipAddressList);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        myIP.setAdapter(adapter);
        onCheckboxClicked(myIPCheckBox);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status == Status.RUNNING) {
            new Thread(mainThread).start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainThread.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainThread.stop();
    }

    private void runButtonCallback() {
        switch (status) {
            case WAITING:
                // Remapping arguments.
                List<Pair<String, String>> remappings = new ArrayList();

                // Master IP.
                String sMasterIP = masterIP.getText().toString();
                if (!sMasterIP.matches(IP_REGEX_EXPRESSION)) {
                    statusText.setText(R.string.status_masterIP_error);
                    break;
                }
                Log.i(TAG, "master ip is fine: " + sMasterIP);

                // Master port
                String sMasterPort = masterPort.getText().toString();
                int iMasterPort = -1;
                try {
                    iMasterPort = Integer.parseInt(sMasterPort);
                } catch (Exception e) {
                    Log.w(TAG, "invalid master port.");
                }

                if (iMasterPort < 0 || iMasterPort > 65535) {
                    statusText.setText(R.string.status_master_port_error);
                    break;
                }
                Log.i(TAG, "master port is fine: " + sMasterPort);
                Pair<String, String> master = new Pair<String, String>("__master", "http://" + sMasterIP + ":" + sMasterPort);
                remappings.add(master);

                // Device's IP.
                if (myIPCheckBox.isChecked()) {
                    String sMyIP = String.valueOf(myIP.getSelectedItem());
                    if (!sMyIP.matches(IP_REGEX_EXPRESSION)) {
                        statusText.setText(R.string.status_myIP_error);
                        break;
                    }
                    Log.i(TAG, "my ip is fine: " + sMyIP);
                    Pair<String, String> ip = new Pair<String, String>("__ip", sMyIP);
                    remappings.add(ip);
                }

                // Parse remappings - basic error handling.
                String sRemappingArgs = remappingArgs.getText().toString();
                if (!"".equals(sRemappingArgs)) {
                    try {
                        String[] remappingExtras = remappingArgs.getText().toString().split(" ");
                        for (String remapping : remappingExtras) {
                            String[] remappingBits = remapping.split(":=");
                            Pair<String, String> remappingPair = new Pair<String, String>(remappingBits[0], remappingBits[1]);
                            remappings.add(remappingPair);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Could not parse remappings properly.", e);
                    }
                } else {
                    Log.i(TAG, "No remapping arguments provided.");
                }

                if (!mainThread.checkRosMaster(remappings)) {
                    statusText.setText(R.string.status_check_master_error);
                    break;
                }
                Log.i(TAG, "Master is ready");

                statusText.setText(R.string.status_running);
                runButton.setText(R.string.button_stop);

                new Thread(mainThread).start();
                status = Status.RUNNING;

                Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putString(getString(R.string.pref_master_ip_key), sMasterIP);
                sharedPreferencesEditor.putString(getString(R.string.pref_master_port_key), sMasterPort);
                sharedPreferencesEditor.putString(getString(R.string.pref_remappings_key), sRemappingArgs);
                sharedPreferencesEditor.apply();
                break;

            case RUNNING:
                statusText.setText(R.string.status_waiting);
                runButton.setText(R.string.button_run);

                mainThread.stop();
                status = Status.WAITING;
                break;
        }
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.my_ip_checkbox:
                myIP.setEnabled(checked);
                myIP.setClickable(checked);
                break;
            default:
                break;
        }
    }

    private List<String> getLocalIpAddresses() {
        List<String> availableIPAddresses = new ArrayList<String>();
        try {
            for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(netInterface.getInetAddresses())) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        availableIPAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return availableIPAddresses;
    }
}
