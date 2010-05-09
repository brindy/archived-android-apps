package uk.org.brindy.android.cineworldgold;

import java.text.SimpleDateFormat;

import uk.org.brindy.android.cineworldgold.support.Showing;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ShowingsListAdapter extends BaseAdapter implements OnClickListener {

	private SimpleDateFormat mShowingsTimeFormatter = new SimpleDateFormat(
			"HH:mm");

	private Context mContext;

	private Showing[] mShowings;

	public ShowingsListAdapter(Context context, Showing[] showings) {
		this.mContext = context;
		this.mShowings = showings;
	}

	public int getCount() {
		return mShowings.length;
	}

	public Object getItem(int position) {
		return mShowings[position];
	}

	public long getItemId(int position) {
		return mShowings[position].id;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		final Showing showing = mShowings[position];

		TextView showingTime = (TextView) View.inflate(parent.getContext(),
				R.layout.showingtime, null);

		// we're going to re-use the showing id as the id for the
		// item so that when the user clicks the text it can jump to
		// the booking page without having to do a lookup
		showingTime.setId(showing.id);
		showingTime.setTag(showing.id);
		showingTime.setText(mShowingsTimeFormatter.format(showing.date));
		showingTime.setClickable(true);
		showingTime.setOnClickListener(this);

		return showingTime;
	}

	@Override
	public void onClick(View v) {

		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
				.parse("https://www.cineworld.co.uk/mobile/"
						+ "booking?performance=" + v.getTag()));
		mContext.startActivity(myIntent);
	}
}
