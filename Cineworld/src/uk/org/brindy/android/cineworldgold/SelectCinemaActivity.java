package uk.org.brindy.android.cineworldgold;

import java.util.List;

import uk.org.brindy.android.cineworldgold.CinemaListManager.Listener;
import uk.org.brindy.android.cineworldgold.support.Cinema;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectCinemaActivity extends ListActivity implements Listener,
		OnClickListener {

	private CinemaListManager mCinemaListManager;

	public void setCinemaList(final List<Cinema> cinemas) {

		final Context context = this;
		setListAdapter(new BaseAdapter() {

			public int getCount() {
				return cinemas.size();
			}

			public Object getItem(int position) {
				return cinemas.get(position);
			}

			public long getItemId(int position) {
				Cinema cinema = cinemas.get(position);
				return cinema.id;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				Cinema cinema = cinemas.get(position);
				View view = View.inflate(context, R.layout.select_cinema_row,
						null);
				TextView cinemaName = (TextView) view
						.findViewById(R.id.cinemaName);

				cinemaName.setClickable(true);
				cinemaName.setOnClickListener(SelectCinemaActivity.this);

				cinemaName.setText(cinema.name);
				return view;
			}

		});
	}

	public void error() {
		Toast.makeText(
				this,
				"Unable to retrieve cinema list.  "
						+ "Please check your data connection settings and try again.",
				Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_cinema);
		mCinemaListManager = new CinemaListManager(this);
		mCinemaListManager.getCinemaList();

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (-1 != getSelectedItemPosition()) {
			Cinema cinema = mCinemaListManager
					.findCinemaById(getSelectedItemId());
			selectCinema(cinema);
		}
	}

	private void selectCinema(Cinema cinema) {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("cinema", cinema);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	public void onClick(View v) {

		String name = ((TextView) v).getText().toString();
		Cinema cinema = mCinemaListManager.findCinemaByName(name);
		if (null != cinema) {
			selectCinema(cinema);
		}

	}

}
