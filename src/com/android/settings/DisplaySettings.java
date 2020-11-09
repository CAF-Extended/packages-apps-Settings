/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.cafex.support.preference.SystemSettingMasterSwitchPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.BrightnessLevelPreferenceController;
import com.android.settings.display.CameraGesturePreferenceController;
import com.android.settings.display.LiftToWakePreferenceController;
import com.android.settings.display.PocketJudgePreferenceController;
import com.android.settings.display.ScreenSaverPreferenceController;
import com.android.settings.display.ShowOperatorNamePreferenceController;
import com.android.settings.display.TapToWakePreferenceController;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.display.VrDisplayPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DisplaySettings extends DashboardFragment implements
        OnPreferenceChangeListener {
        
    private static final String TAG = "DisplaySettings";
    private static final String KEY_EDGE_LIGHTNING = "pulse_ambient_light";
        
    private SystemSettingMasterSwitchPreference mEdgeLightning;
    
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        final ContentResolver resolver = getActivity().getContentResolver();
                
        mEdgeLightning = (SystemSettingMasterSwitchPreference)
                findPreference(KEY_EDGE_LIGHTNING);
        boolean enabled = Settings.System.getIntForUser(resolver,
                KEY_EDGE_LIGHTNING, 0, UserHandle.USER_CURRENT) == 1;
        mEdgeLightning.setChecked(enabled);
        mEdgeLightning.setOnPreferenceChangeListener(this);        
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEdgeLightning) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(resolver, KEY_EDGE_LIGHTNING,
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
                    
    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new CameraGesturePreferenceController(context));
        controllers.add(new LiftToWakePreferenceController(context));
        controllers.add(new PocketJudgePreferenceController(context));
        controllers.add(new ScreenSaverPreferenceController(context));
        controllers.add(new TapToWakePreferenceController(context));
        controllers.add(new VrDisplayPreferenceController(context));
        controllers.add(new ShowOperatorNamePreferenceController(context));
        controllers.add(new ThemePreferenceController(context));
        controllers.add(new BrightnessLevelPreferenceController(context, lifecycle));
        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_settings) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };
}
