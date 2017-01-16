/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.liberario.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.fragments.AboutMainFragment;
import de.grobox.liberario.fragments.SettingsFragment;
import de.grobox.liberario.ui.TransportrChangeLog;
import de.grobox.liberario.utils.TransportrUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static de.grobox.liberario.activities.MainActivity.CHANGED_NETWORK_PROVIDER;

abstract class DrawerActivity extends TransportrActivity {

	private Drawer drawer;
	private AccountHeader accountHeader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Accounts aka TransportNetworks
		accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.account_header_background)
				.withSelectionListEnabled(false)
				.withThreeSmallProfileImages(true)
				.withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
					@Override
					public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
						if(currentProfile) {
							openPickNetworkProviderActivity();
							return true;
						} else if(profile != null && profile instanceof ProfileDrawerItem) {
							TransportNetwork network = (TransportNetwork) ((ProfileDrawerItem) profile).getTag();
							if(network != null) {
								// save new network
								Preferences.setNetworkId(DrawerActivity.this, network.getId());

								// notify everybody of this change
								onNetworkProviderChanged();
							}
						}
						return false;
					}
				})
				.withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
					@Override
					public boolean onClick(View view, IProfile profile) {
						openPickNetworkProviderActivity();
						return true;
					}
				})
				.build();

		// Drawer
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withAccountHeader(accountHeader)
				.addDrawerItems(
						new DividerDrawerItem(),
						getDrawerItem(SettingsFragment.TAG, R.drawable.ic_action_settings),
						getDrawerItem(TransportrChangeLog.TAG, R.drawable.ic_action_changelog),
						getDrawerItem(AboutMainFragment.TAG, R.drawable.ic_action_about)
				)
				.withOnDrawerListener(new Drawer.OnDrawerListener() {
					@Override
					public void onDrawerOpened(View drawerView) {
						KeyboardUtil.hideKeyboard(DrawerActivity.this);
					}

					@Override
					public void onDrawerClosed(View drawerView) {
					}

					@Override
					public void onDrawerSlide(View drawerView, float slideOffset) {
					}
				})
				.withFireOnInitialOnClick(false)
				.withSavedInstance(savedInstanceState)
				.build();

		// add transport networks to header
		addAccounts();
	}

	private void addAccounts() {
		// TODO async
		TransportNetwork network = Preferences.getTransportNetwork(this);
		if(network != null) {
			ProfileDrawerItem item1 = new ProfileDrawerItem()
					.withName(network.getName())
					.withEmail(network.getDescription())
					.withIcon(ContextCompat.getDrawable(this, network.getLogo()));
			item1.withTag(network);
			accountHeader.addProfile(item1, accountHeader.getProfiles().size());
		}

		TransportNetwork network2 = Preferences.getTransportNetwork(this, 2);
		if(network2 != null) {
			ProfileDrawerItem item2 = new ProfileDrawerItem()
					.withName(network2.getName())
					.withEmail(network2.getDescription())
					.withIcon(ContextCompat.getDrawable(this, network2.getLogo()));
			item2.withTag(network2);
			accountHeader.addProfile(item2, accountHeader.getProfiles().size());
		}

		TransportNetwork network3 = Preferences.getTransportNetwork(this, 3);
		if(network3 != null) {
			ProfileDrawerItem item3 = new ProfileDrawerItem()
					.withName(network3.getName())
					.withEmail(network3.getDescription())
					.withIcon(ContextCompat.getDrawable(this, network3.getLogo()));
			item3.withTag(network3);
			accountHeader.addProfile(item3, accountHeader.getProfiles().size());
		}
	}

	private PrimaryDrawerItem getDrawerItem(final String tag, final int icon) {
		Drawer.OnDrawerItemClickListener onClick;
		String name;

		if(tag.equals(TransportrChangeLog.TAG)) {
			onClick = new Drawer.OnDrawerItemClickListener() {
				@Override
				public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
					new TransportrChangeLog(DrawerActivity.this, Preferences.darkThemeEnabled(DrawerActivity.this)).getFullLogDialog().show();
					return true;
				}
			};
			name = getString(R.string.drawer_changelog);
		}
		else {
			onClick = new Drawer.OnDrawerItemClickListener() {
				@Override
				public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
					drawer.closeDrawer();
					// TODO
					return true;
				}
			};
			name = getFragmentName(tag);
		}
		return new PrimaryDrawerItem()
				.withName(name)
				.withTag(tag)
				.withIcon(TransportrUtils.getTintedDrawable(this, icon))
				.withSelectable(false)
				.withOnDrawerItemClickListener(onClick);
	}

	private String getFragmentName(String tag) {
		if(tag.equals(SettingsFragment.TAG)) return getString(R.string.drawer_settings);
		if(tag.equals(AboutMainFragment.TAG)) return getString(R.string.drawer_about);
		throw new IllegalArgumentException("Could not find fragment name");
	}

	private void openPickNetworkProviderActivity() {
		Intent intent = new Intent(this, PickNetworkProviderActivity.class);
		ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(getCurrentFocus(), 0, 0, 0, 0);
		ActivityCompat.startActivityForResult(this, intent, CHANGED_NETWORK_PROVIDER, options.toBundle());
	}

	protected void onNetworkProviderChanged() {
		// create an intent for restarting this activity
		Intent intent = new Intent(this, NewMapActivity.class);
//		intent.setAction(getCurrentFragmentTag());
		intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);

		finish();
		startActivity(intent);
	}

	protected void openDrawer() {
		drawer.openDrawer();
	}

}
