package me.sivieri.dimatodos;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements OnNoteSelectedListener {

	public static final String TAG = "dimatodos";

	private static final int PLAY_ERROR = 1;

	private LocationState locationState = LocationState.FIRST;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.notesfragment);
		this.progressBar = new ProgressBar(this);
		this.progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		this.progressBar.setIndeterminate(true);
		listFragment.getListView().setEmptyView(this.progressBar);
		ViewGroup root = (ViewGroup) this.findViewById(android.R.id.content);
		root.addView(this.progressBar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS && this.locationState == LocationState.FIRST) {
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_ERROR).show();
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		switch (arg0) {
			case PLAY_ERROR:
				switch (arg1) {
					case Activity.RESULT_OK:
						this.locationState = LocationState.SET;
						break;
					default:
						this.locationState = LocationState.UNSET;
						break;
				}
				break;
			default:
				// really?
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_note:
				NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
				if (noteFragment == null) {
					Intent i = new Intent(this, NoteActivity.class);
					i.putExtra(NoteActivity.EDIT, true);
					if (this.locationState == LocationState.SET) {
						i.putExtra(NoteActivity.LOCATION, true);
					}
					startActivity(i);
				}
				else {
					Bundle extras = new Bundle();
					extras.putBoolean(NoteActivity.EDIT, true);
					if (this.locationState == LocationState.SET) {
						extras.putBoolean(NoteActivity.LOCATION, true);
					}
					noteFragment.updateContent(null, null, extras);
				}
				return true;
			case R.id.settings:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public LocationState getLocationState() {
		return this.locationState;
	}

	static enum LocationState {
		FIRST, SET, UNSET
	}

	@Override
	public void onNoteSelected(Uri uri) {
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		if (noteFragment == null) {
			Intent i = new Intent(this, NoteActivity.class);
			i.putExtra(MainFragment.NOTE_ID, uri);
			if (this.locationState == LocationState.SET) {
				i.putExtra(NoteActivity.LOCATION, true);
			}
			startActivity(i);
		}
		else {
			Bundle extras = new Bundle();
			extras.putParcelable(MainFragment.NOTE_ID, uri);
			if (this.locationState == LocationState.SET) {
				extras.putBoolean(NoteActivity.LOCATION, true);
			}
			noteFragment.updateContent(null, null, extras);
		}
	}

	@Override
	public void finish() {
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		if (noteFragment == null || noteFragment.exitingEdit()) {
			super.finish();
		}
	}

}
