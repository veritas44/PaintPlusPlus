/*
 * Copyright 2017 karol-202
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.karol202.paintplus.settings;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import pl.karol202.paintplus.R;

public class SettingsFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
	
	@Override
	public void onDisplayPreferenceDialog(Preference preference)
	{
		DialogFragment fragment;
		if(preference instanceof SeekBarPreference)
		{
			fragment = SeekBarPreferenceDialogFragmentCompat.newInstance(preference);
			fragment.setTargetFragment(this, 0);
			fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
		}
		else super.onDisplayPreferenceDialog(preference);
	}
}