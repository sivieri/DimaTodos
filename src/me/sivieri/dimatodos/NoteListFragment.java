package me.sivieri.dimatodos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class NoteListFragment extends ListFragment {

	public static final String NOTE_ID = "noteid";

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
				Intent i = new Intent(getActivity(), NoteActivity.class);
				Uri uri = Uri.parse(NotesContentProvider.CONTENT_URI + "/" + info.id);
				i.putExtra(NOTE_ID, uri);
				startActivity(i);
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
}
