package uk.org.brindy.d20tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class InitiativeTrackerTabActivity extends Activity implements
		android.content.DialogInterface.OnClickListener {

	private List<Toon> toons = new LinkedList<Toon>();

	private Adapter adapter;

	private ListView list;

	private WebView browser;

	private MenuItem miCharactersAdd;

	private MenuItem miCharactersRemoveAll;

	private MenuItem miCombatNew;

	private MenuItem miCombatNextRound;

	private Toon selectedToon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inittracker);

		if (savedInstanceState != null) {

			int i = 0;
			while (true) {

				String name = savedInstanceState.getString("toon." + i
						+ ".name");

				if (null == name) {
					break;
				}

				Toon t = new Toon();
				toons.add(t);
				t.name = name;
				t.init = savedInstanceState.getInt("toon." + i + ".init");

				int j = 0;
				while (true) {
					if (!savedInstanceState.containsKey("toon." + i + "." + j
							+ ".effect")) {
						break;
					}

					Effect e = new Effect(savedInstanceState.getInt("toon." + i
							+ "." + j + ".effect"));
					e.rounds = savedInstanceState.getInt("toon." + i + "." + j
							+ ".rounds");
					t.effects.put(e.iconResource, e);

					j++;
				}

				i++;
			}

		}

		list = (ListView) findViewById(android.R.id.list);
		list.setAdapter(adapter = new Adapter());

		browser = (WebView) findViewById(android.R.id.empty);
		browser.loadUrl("file:///android_asset/inittracker.html");

		if (toons.size() > 0) {
			list.setVisibility(View.VISIBLE);
			browser.setVisibility(View.GONE);
		} else {
			list.setVisibility(View.GONE);
			browser.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		int i = 0;
		for (Toon t : toons) {
			outState.putString("toon." + i + ".name", t.name);
			outState.putInt("toon." + i + ".init", t.init);

			int j = 0;
			for (Effect e : t.effects.values()) {
				outState.putInt("toon." + i + "." + j + ".effect",
						e.iconResource);
				outState.putInt("toon." + i + "." + j + ".rounds", e.rounds);
				j++;
			}

			i++;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu characters = menu.addSubMenu("Characters");
		miCharactersAdd = characters.add("Add");
		miCharactersRemoveAll = characters.add("Remove All");

		SubMenu s = menu.addSubMenu("Combat");
		miCombatNew = s.add("New");
		miCombatNextRound = s.add("Next Round");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item == miCharactersAdd) {
			actionCharactersAdd();
		} else if (item == miCharactersRemoveAll) {
			actionCharactersRemoveAll();
		} else if (item == miCombatNew) {
			actionCombatNew();
		} else if (item == miCombatNextRound) {
			actionCombatNextRound();
		}

		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog dlg = new AlertDialog.Builder(this)
				.setView(
						View.inflate(this, R.layout.inittracker_toonedit, null))
				.setPositiveButton("Done", this).setTitle("Update Toon")
				.create();
		return dlg;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		Dialog dlg = (Dialog) dialog;

		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect1),
				R.drawable.anchor);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect2),
				R.drawable.badge);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect3),
				R.drawable.beaker);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect4),
				R.drawable.circle);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect5),
				R.drawable.cloud);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect6),
				R.drawable.cursor);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect7),
				R.drawable.spraycan);
		checkEffect(
				(CheckBox) dlg.findViewById(R.id.inittracker_toonedit_effect8),
				R.drawable.wheelchair);

		selectedToon.name = ((EditText) dlg
				.findViewById(R.id.inittracker_toonedit_name)).getText()
				.toString();

		selectedToon.init = Integer.valueOf(((EditText) dlg
				.findViewById(R.id.inittracker_toonedit_init)).getText()
				.toString());

		Collections.sort(toons);

		adapter.notifyDataSetChanged();

	}

	private void checkEffect(CheckBox cb, int effect) {

		if (cb.isChecked()) {

			if (!selectedToon.effects.containsKey(effect)) {
				selectedToon.effects.put(effect, new Effect(effect));
			}

		} else {

			selectedToon.effects.remove(effect);

		}

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {

		EditText name = (EditText) dialog
				.findViewById(R.id.inittracker_toonedit_name);

		EditText init = (EditText) dialog
				.findViewById(R.id.inittracker_toonedit_init);

		name.setText(selectedToon.name);
		init.setText(String.valueOf(selectedToon.init));

		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect1),
				R.drawable.anchor);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect2),
				R.drawable.badge);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect3),
				R.drawable.beaker);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect4),
				R.drawable.circle);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect5),
				R.drawable.cloud);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect6),
				R.drawable.cursor);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect7),
				R.drawable.spraycan);
		updateEffect(
				(CheckBox) dialog
						.findViewById(R.id.inittracker_toonedit_effect8),
				R.drawable.wheelchair);

	}

	private void updateEffect(CheckBox cb, int effect) {
		cb.setChecked(selectedToon.effects.containsKey(effect));
	}

	private void actionCharactersAdd() {
		toons.add(new Toon());
		adapter.notifyDataSetChanged();
		browser.setVisibility(View.GONE);
		list.setVisibility(View.VISIBLE);
	}

	private void actionCharactersRemoveAll() {
		toons.clear();
		adapter.notifyDataSetChanged();
	}

	private void actionCombatNew() {
		for (Toon t : toons) {
			t.effects.clear();
		}
		adapter.notifyDataSetChanged();
	}

	private void actionCombatNextRound() {
		for (Toon t : toons) {
			for (Effect e : t.effects.values()) {
				e.rounds++;
			}
		}
		adapter.notifyDataSetChanged();
	}

	private void actionToonTapped(Toon t) {
		selectedToon = t;
		showDialog(0);
	}

	private class Adapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return toons.size();
		}

		@Override
		public Object getItem(int position) {
			return toons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				view = View.inflate(InitiativeTrackerTabActivity.this,
						R.layout.inittracker_toonrow, null);
			}

			view.setTag(position);
			view.setClickable(true);
			view.setOnClickListener(this);

			TextView text = (TextView) view
					.findViewById(R.id.init_toonrow_name);
			text.setText(toons.get(position).name);

			text = (TextView) view.findViewById(R.id.init_toonrow_init);
			text.setText(String.valueOf(toons.get(position).init));

			LinearLayout layout = (LinearLayout) view
					.findViewById(R.id.init_toonrow_effects);
			layout.removeAllViews();

			for (Effect e : toons.get(position).effects.values()) {
				View effect = View.inflate(InitiativeTrackerTabActivity.this,
						R.layout.inittracker_toonroweffects, null);

				ImageView effectImage = (ImageView) effect
						.findViewById(R.id.init_tooneffectimage);
				TextView effectText = (TextView) effect
						.findViewById(R.id.init_tooneffecttext);

				effectImage.setImageResource(e.iconResource);
				effectText.setText(String.valueOf(e.rounds));

				layout.addView(effect);
			}

			return view;
		}

		@Override
		public void onClick(View v) {
			actionToonTapped(toons.get((Integer) v.getTag()));
		}

	}

	private class Toon implements Comparable<Toon> {

		public String name = "New Toon";

		public int init = 0;

		public Map<Integer, Effect> effects = new LinkedHashMap<Integer, InitiativeTrackerTabActivity.Effect>();

		@Override
		public int compareTo(Toon another) {
			return another.init - init;
		}

	}

	private class Effect {

		public int iconResource;

		public int rounds;

		public Effect(int iconResource) {
			this.iconResource = iconResource;
		}

	}

}
