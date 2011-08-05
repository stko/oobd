package org.oobd.ui.android;

import org.oobd.ui.android.application.OOBDApp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import java.util.ArrayList;

/**
 * @author Andreas Budde, Peter Mayer
 * Settings activity that allows users to configure the app (Bluetooth OBD connection, Lua script file selection and simulation mode.
 */
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_LIST_SELECT_PAIRED_OBD2_DEVICE = "PREF_PAIRED_OBD2_DEVICE";
	public static final String KEY_LIST_SELECT_LUA_SCRIPT = "PREF_LUA_SCRIPT";
	public static final String KEY_CHECKBOX_SIMULATION_MODE = "PREF_SIMULATION_MODE";

	private ListPreference mListPreferenceSelectPairedOBD2Device;
	private ListPreference mListPreferenceSelectLuaScript;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load the xml preference file
		addPreferencesFromResource(R.xml.settings);

		mListPreferenceSelectPairedOBD2Device = (ListPreference)getPreferenceScreen().findPreference(KEY_LIST_SELECT_PAIRED_OBD2_DEVICE);
		ArrayList<String> availableDevices = OOBDApp.getInstance().getAvailableBluetoothDevices();
		mListPreferenceSelectPairedOBD2Device.setEntries(availableDevices.toArray(new String[0]));
		mListPreferenceSelectPairedOBD2Device.setEntryValues(availableDevices.toArray(new String[0]));
		
		updateSelectedOBD2Device();
		
		mListPreferenceSelectLuaScript = (ListPreference)getPreferenceScreen().findPreference(KEY_LIST_SELECT_LUA_SCRIPT);
		CharSequence[] luaScripts = OOBDApp.getInstance().getAvailableLuaScript();
		mListPreferenceSelectLuaScript.setEntries(luaScripts);
		mListPreferenceSelectLuaScript.setEntryValues(luaScripts);
		
		updateSelectedScriptLabel();
		
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        // Set up a listener whenever a preference changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
    
    /**
     * Called whenever some setting is changed
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	
    	if (key.equals(KEY_LIST_SELECT_PAIRED_OBD2_DEVICE)) {
    		// TODO do some stuff such as initialization?
    		updateSelectedOBD2Device(); 
    	} else if (key.equals(KEY_LIST_SELECT_LUA_SCRIPT)) {
    		// TODO load scripts?
   			updateSelectedScriptLabel();
        } else if (key.equals(KEY_CHECKBOX_SIMULATION_MODE)) {
        	// TODO do some stuff such as initialization ?
        }
    	
    }

	private void updateSelectedOBD2Device() {
		String selectedOBD2Device = getPreferenceScreen().getSharedPreferences().getString(KEY_LIST_SELECT_PAIRED_OBD2_DEVICE, "none");
		mListPreferenceSelectPairedOBD2Device.setSummary("Selected: " + selectedOBD2Device);
	}
	
	private void updateSelectedScriptLabel() {
		String selectedScript = getPreferenceScreen().getSharedPreferences().getString(KEY_LIST_SELECT_LUA_SCRIPT, "none");
		mListPreferenceSelectLuaScript.setSummary("Selected: " + selectedScript);
	}

}
