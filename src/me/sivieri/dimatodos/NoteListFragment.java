package me.sivieri.dimatodos;

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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.note_context_edit:
				Intent i = new Intent(getActivity(), NoteActivity.class);
				Uri uri = Uri.parse(NotesContentProvider.CONTENT_URI + "/" + info.id);
				i.putExtra(NOTE_ID, uri);
				startActivity(i);
				return true;
			case R.id.note_context_delete:

				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
}
