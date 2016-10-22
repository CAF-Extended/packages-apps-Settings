/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.dashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.SettingsActivity;
import com.android.settings.TestConfig;
import com.android.settings.core.PreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.testutils.FakeFeatureFactory;
import com.android.settingslib.drawer.CategoryKey;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class DashboardFragmentTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Context mContext;
    @Mock
    private DashboardCategory mDashboardCategory;
    @Mock
    private FakeFeatureFactory mFakeFeatureFactory;
    @Mock
    private ProgressiveDisclosureMixin mDisclosureMixin;
    private TestFragment mTestFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        FakeFeatureFactory.setupForTest(mContext);
        mFakeFeatureFactory = (FakeFeatureFactory) FeatureFactory.getFactory(mContext);
        mDashboardCategory.tiles = new ArrayList<>();
        mDashboardCategory.tiles.add(new Tile());
        mTestFragment = new TestFragment(ShadowApplication.getInstance().getApplicationContext());
        when(mFakeFeatureFactory.dashboardFeatureProvider
                .getProgressiveDisclosureMixin(any(Context.class), eq(mTestFragment)))
                .thenReturn(mDisclosureMixin);
        when(mFakeFeatureFactory.dashboardFeatureProvider.getTilesForCategory(anyString()))
                .thenReturn(mDashboardCategory);
        mTestFragment.onAttach(ShadowApplication.getInstance().getApplicationContext());
    }

    @Test
    public void testPreferenceControllerGetterSetter_shouldAddAndGetProperly() {
        final TestPreferenceController controller = new TestPreferenceController(mContext);
        mTestFragment.addPreferenceController(controller);

        final TestPreferenceController retrievedController = mTestFragment.getPreferenceController
                (TestPreferenceController.class);

        assertThat(controller).isSameAs(retrievedController);
    }

    @Test
    public void displayTilesAsPreference_shouldAddTilesWithIntent() {
        when(mFakeFeatureFactory.dashboardFeatureProvider.getTilesForCategory(anyString()))
                .thenReturn(mDashboardCategory);
        when(mFakeFeatureFactory.dashboardFeatureProvider.getDashboardKeyForTile(any(Tile.class)))
                .thenReturn("test_key");
        mTestFragment.onCreatePreferences(new Bundle(), "rootKey");

        verify(mDisclosureMixin).addPreference(any(PreferenceScreen.class),
                any(DashboardTilePreference.class));
    }

    @Test
    public void displayTilesAsPreference_shouldNotAddTilesWithoutIntent() {
        mTestFragment.onCreatePreferences(new Bundle(), "rootKey");

        verify(mTestFragment.mScreen, never()).addPreference(any(DashboardTilePreference.class));
    }

    @Test
    public void displayTilesAsPreference_withEmptyCategory_shouldNotAddTiles() {
        mDashboardCategory.tiles = null;
        mTestFragment.onCreatePreferences(new Bundle(), "rootKey");

        verify(mTestFragment.mScreen, never()).addPreference(any(DashboardTilePreference.class));
    }

    @Test
    public void bindPreference_shouldBindAllData() {
        final Preference preference = new Preference(
                ShadowApplication.getInstance().getApplicationContext());
        final Tile tile = new Tile();
        tile.title = "title";
        tile.summary = "summary";
        tile.icon = Icon.createWithBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));
        tile.metaData = new Bundle();
        tile.metaData.putString(SettingsActivity.META_DATA_KEY_FRAGMENT_CLASS, "HI");
        tile.priority = 10;
        mTestFragment.bindPreferenceToTile(mContext, preference, tile, "123");

        assertThat(preference.getTitle()).isEqualTo(tile.title);
        assertThat(preference.getSummary()).isEqualTo(tile.summary);
        assertThat(preference.getIcon()).isNotNull();
        assertThat(preference.getFragment())
                .isEqualTo(tile.metaData.getString(SettingsActivity.META_DATA_KEY_FRAGMENT_CLASS));
        assertThat(preference.getOrder()).isEqualTo(-tile.priority);
    }

    public static class TestPreferenceController extends PreferenceController {

        public TestPreferenceController(Context context) {
            super(context);
        }

        @Override
        public boolean handlePreferenceTreeClick(Preference preference) {
            return false;
        }

        @Override
        protected boolean isAvailable() {
            return false;
        }

        @Override
        public String getPreferenceKey() {
            return null;
        }

        @Override
        public void updateNonIndexableKeys(List<String> keys) {

        }
    }

    public static class TestFragment extends DashboardFragment {

        private final Context mContext;
        public PreferenceScreen mScreen;

        public TestFragment(Context context) {
            mContext = context;
            mScreen = mock(PreferenceScreen.class);
        }

        @Override
        public Context getContext() {
            return mContext;
        }

        @Override
        public int getMetricsCategory() {
            return 0;
        }

        @Override
        protected String getCategoryKey() {
            return CategoryKey.CATEGORY_HOMEPAGE;
        }

        @Override
        public PreferenceScreen getPreferenceScreen() {
            return mScreen;
        }

        @Override
        protected String getLogTag() {
            return "TEST_FRAG";
        }

        @Override
        protected int getPreferenceScreenResId() {
            return 0;
        }

        @Override
        protected List<PreferenceController> getPreferenceControllers(Context context) {
            return null;
        }
    }

}