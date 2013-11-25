package me.sivieri.dimatodos;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	public static final String TAG = "dimatodos";

	private static final int PLAY_ERROR = 1;

	private LocationState locationState = LocationState.FIRST;
	private SimpleCursorAdapter mAdapter;
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

		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(this.progressBar);

		String[] fromColumns = { getString(R.string.list_name) };
		int[] toViews = { android.R.id.text1 };

		this.mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
		listFragment.setListAdapter(this.mAdapter);

		this.getSupportLoaderManager().initLoader(0, null, this);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_note:
				Intent i = new Intent(this, NoteActivity.class);
				i.putExtra(NoteActivity.EDIT, true);
				if (this.locationState == LocationState.SET) {
					i.putExtra(NoteActivity.LOCATION, true);
				}
				startActivity(i);
				return true;
			case R.id.settings:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { NotesOpenHelper.ID, NotesOpenHelper.KEY };
		return new CursorLoader(this, NotesContentProvider.CONTENT_URI, projection, null, null, NotesOpenHelper.ID + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		this.mAdapter.swapCursor(arg1);
		if (arg1.getCount() == 0) {
			this.progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.mAdapter.swapCursor(null);
	}

	static enum LocationState {
		FIRST, SET, UNSET
	}

}
