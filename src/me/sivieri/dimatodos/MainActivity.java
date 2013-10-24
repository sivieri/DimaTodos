package me.sivieri.dimatodos;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.notesfragment);

		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		progressBar.setIndeterminate(true);
		listFragment.getListView().setEmptyView(progressBar);

		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		String[] fromColumns = { getString(R.string.list_name) };
		int[] toViews = { android.R.id.text1 };

		this.mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
		listFragment.setListAdapter(this.mAdapter);

		this.getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { NotesOpenHelper.ID, NotesOpenHelper.KEY };
		return new CursorLoader(this, NotesContentProvider.CONTENT_URI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		this.mAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.mAdapter.swapCursor(null);
	}

}
