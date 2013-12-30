package me.sivieri.dimatodos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class MainFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	public static final String TAG = "dimatodos";
	public static final String NOTE_ID = "noteid";

	private SimpleCursorAdapter mAdapter;
	private OnNoteSelectedListener noteSelectedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		String[] fromColumns = { getString(R.string.list_name) };
		int[] toViews = { android.R.id.text1 };

		this.mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
		setListAdapter(this.mAdapter);

		getActivity().getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.noteSelectedListener = (OnNoteSelectedListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { NotesOpenHelper.ID, NotesOpenHelper.KEY };
		return new CursorLoader(getActivity(), NotesContentProvider.CONTENT_URI, projection, null, null, NotesOpenHelper.ID + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		this.mAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.mAdapter.swapCursor(null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater menuInflater = getActivity().getMenuInflater();
		menuInflater.inflate(R.menu.note_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.note_context_edit:
				this.noteSelectedListener.onNoteSelected(Uri.parse(NotesContentProvider.CONTENT_URI + "/" + info.id));
				return true;
			case R.id.note_context_delete:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(getString(R.string.delete_dialog_title));
				builder.setMessage(getString(R.string.delete_dialog_content));
				builder.setPositiveButton(getString(R.string.delete_dialog_yes), new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Uri uri = Uri.parse(NotesContentProvider.CONTENT_URI + "/" + info.id);
						getActivity().getContentResolver().delete(uri, null, null);
					}

				});
				builder.setNegativeButton(getString(R.string.delete_dialog_no), new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// don't do anything...
					}

				});
				builder.show();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		this.noteSelectedListener.onNoteSelected(Uri.parse(NotesContentProvider.CONTENT_URI + "/" + id));
	}

}
