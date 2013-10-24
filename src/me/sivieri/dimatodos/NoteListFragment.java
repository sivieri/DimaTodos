package me.sivieri.dimatodos;

import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;

public class NoteListFragment extends ListFragment {

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater menuInflater = getActivity().getMenuInflater();
		menuInflater.inflate(R.menu.note_context, menu);
	}

}
