package uk.org.brindy.android.cineworldgold;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.brindy.android.cineworldgold.FilmListManager.Listener;
import uk.org.brindy.android.cineworldgold.SettingsManager.SortFilmsBy;
import uk.org.brindy.android.cineworldgold.support.Cinema;
import uk.org.brindy.android.cineworldgold.support.Film;
import uk.org.brindy.android.cineworldgold.support.Showing;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
// extends ExpandableListActivity
		implements Listener, OnClickListener {

	private static final int MENU_SELECT_CINEMA = Menu.FIRST;

	private static final int MENU_SORT_TOGGLE = MENU_SELECT_CINEMA + 1;

	private static final int ACTIVITY_SELECT_CINEMA = 1;

	private SimpleDateFormat mShowingsDateFormatter = new SimpleDateFormat(
			"EEE dd MMM");

	public static Map<String, Bitmap> cache = new HashMap<String, Bitmap>();

	private FilmListManager mFilmListManager;

	private SettingsManager mSettingsManager;

	private List<Film> mFilms;

	private MenuItem mToggle;

	private ProgressDialog mProgress;

	private TextView mSortTypeText;

	private ExpandableListView mList;

	private ImageView mLogo;

	private View mEmpty;

	public MainActivity() {
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLogo = (ImageView) findViewById(R.id.logo);

		mList = (ExpandableListView) findViewById(android.R.id.list);
		mList.setVisibility(View.GONE);

		mEmpty = findViewById(android.R.id.empty);
		mEmpty.setVisibility(View.VISIBLE);

		mSettingsManager = new SettingsManager(getDir("data",
				Context.MODE_PRIVATE));

		mFilmListManager = new FilmListManager(getDir("data",
				Context.MODE_PRIVATE), this);

		applySortText();

		// only get the film list if the location has been set
		if (mFilmListManager.getCinema() != null) {
			updateLocation();
			mFilmListManager.getFilmList();
		} else {
			onSelectCinema();
		}
	}

	public void updateStatus(String msg) {
		// no-op
	}

	public void onShowingEntryClick(View view) {
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
				.parse("https://www.cineworld.co.uk/mobile/"
						+ "booking?performance=" + view.getTag()));
		startActivity(myIntent);
	}

	public void onLogoClick(View view) {
		Animation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -120.0f);
		animation.setDuration(500);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mLogo.setVisibility(View.GONE);
			}
		});
		mLogo.setAnimation(animation);
		mLogo.startAnimation(animation);

	}

	public void error() {
		Toast.makeText(
				this,
				"Problem reading information from server."
						+ " Please try again alter.", Toast.LENGTH_LONG).show();
		finish();
	}

	public void setFilmImage(Film film, Bitmap bmp) {

	}

	public void setFilmList(List<Film> films) {
		mFilms = films;

		if (mFilms == null || mFilms.size() == 0) {
			mList.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
		} else {
			mList.setVisibility(View.VISIBLE);
			mEmpty.setVisibility(View.GONE);
		}

		// local variables are more optimal in android
		final List<Film> list = mFilms;

		// sort by order of next showings
		Comparator<Film> c = mSettingsManager.getSortFilmsBy() == SortFilmsBy.NAME ? byNameComparator
				: byDateComparator;
		Collections.sort(list, c);

		// one child per day in the list of showings
		final Context context = this;
		final Date after = new Date();

		runOnUiThread(new Runnable() {
			public void run() {
				applySortText();

				mList.setAdapter(new ExpandableFilmListAdapter(context, after,
						list));

			}
		});

		if (null != mProgress) {
			mProgress.dismiss();
			mProgress = null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_SELECT_CINEMA, 0, R.string.select_cinema);
		if (mSettingsManager.getSortFilmsBy() == SortFilmsBy.NAME) {
			mToggle = menu.add(0, MENU_SORT_TOGGLE, 0, R.string.timeSort);
		} else {
			mToggle = menu.add(0, MENU_SORT_TOGGLE, 0, R.string.azSort);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case MENU_SELECT_CINEMA:
			onSelectCinema();
			break;
		case MENU_SORT_TOGGLE:
			toggleSort();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onClick(View v) {
		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
				.parse("http://www.cineworld.co.uk/mobile/" + "cinemas/"
						+ mFilmListManager.getCinema().id + "?film="
						+ v.getTag()));
		startActivity(myIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case ACTIVITY_SELECT_CINEMA:
			if (Activity.RESULT_OK == resultCode) {
				Cinema cinema = (Cinema) data.getExtras().getSerializable(
						"cinema");
				mFilmListManager.setCinema(cinema);
				updateLocation();
			} else if (mFilmListManager.getCinema() == null) {
				// used to show an error here, but the user will quickly
				// catch on that they have to select a cinema first
				finish();
			}
			break;
		}

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		RadioButton sortByTime = (RadioButton) dialog
				.findViewById(R.id.sortByTime);
		sortByTime
				.setChecked(SettingsManager.SortFilmsBy.TIME == mSettingsManager
						.getSortFilmsBy());

		RadioButton sortByName = (RadioButton) dialog
				.findViewById(R.id.sortByName);
		sortByName
				.setChecked(SettingsManager.SortFilmsBy.NAME == mSettingsManager
						.getSortFilmsBy());
	}

	private void applySortText() {
		mSortTypeText = (TextView) findViewById(R.id.sortTypeText);
		if (mSettingsManager.getSortFilmsBy() == SortFilmsBy.NAME) {
			mSortTypeText.setText(R.string.sortedByName);
		} else if (mSettingsManager.getSortFilmsBy() == SortFilmsBy.TIME) {
			mSortTypeText.setText(R.string.sortedByTime);
		}
	}

	private void onSelectCinema() {
		Intent i = new Intent(this, SelectCinemaActivity.class);
		startActivityForResult(i, ACTIVITY_SELECT_CINEMA);
	}

	private void updateLocation() {
		TextView filmLocation = (TextView) findViewById(R.id.filmLocation);
		filmLocation.setText(mFilmListManager.getCinema().name);
	}

	private void toggleSort() {
		SortFilmsBy sort = mSettingsManager.getSortFilmsBy();

		if (sort == SortFilmsBy.NAME) {
			sort = SortFilmsBy.TIME;
			mToggle.setTitle(R.string.azSort);
		} else {
			sort = SortFilmsBy.NAME;
			mToggle.setTitle(R.string.timeSort);
		}

		mProgress = new ProgressDialog(this);
		mProgress.setTitle("Sorting...");
		mProgress.setIndeterminate(true);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.show();

		mSettingsManager.setSortFilmsBy(sort);

		new Thread() {
			public void run() {
				setFilmList(mFilms);
			}
		}.start();
	}

	private Comparator<Film> byDateComparator = new Comparator<Film>() {
		public int compare(Film object1, Film object2) {
			Showing next1 = object1.nextShowing();
			Showing next2 = object2.nextShowing();

			if (null == next1) {
				return Integer.MIN_VALUE;
			}

			if (null == next2) {
				return Integer.MAX_VALUE;
			}

			return next1.date.compareTo(next2.date);
		}
	};

	private Comparator<Film> byNameComparator = new Comparator<Film>() {
		public int compare(Film object1, Film object2) {
			return object1.name.compareTo(object2.name);
		}
	};

	private final class ShowingsListAdapter extends ArrayAdapter<Showing> {
		private ShowingsListAdapter(Context context, int textViewResourceId,
				List<Showing> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) super.getView(position, convertView,
					parent);
			view.setTag(this.getItem(position).id);
			return view;
		}
	}

	private final class ExpandableFilmListAdapter extends
			BaseExpandableListAdapter {

		private final Context context;
		private final Date after;
		private final List<Film> list;

		private ExpandableFilmListAdapter(Context context, Date after,
				List<Film> list) {
			this.context = context;
			this.after = after;
			this.list = list;
		}

		// group related

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			final Film film = list.get(groupPosition);

			// inflate the view...
			View view = convertView;
			if (null == view) {
				view = View.inflate(context, R.layout.filmlistentry, null);
			}

			// populate the items in the view...
			final ImageView filmImage = (ImageView) view
					.findViewById(R.id.filmImage);
			filmImage.setImageResource(R.drawable.icon);
			filmImage.setTag(film.id);

			new BackgroundImageFetcher(filmImage, film).execute();

			TextView filmName = (TextView) view.findViewById(R.id.filmName);
			filmName.setText(film.name);

			TextView filmInfo = (TextView) view.findViewById(R.id.filmInfo);
			filmInfo.setTag(film.id);
			filmInfo.setOnClickListener(MainActivity.this);

			return view;
		}

		public Object getGroup(int groupPosition) {
			return list.get(groupPosition);
		}

		public int getGroupCount() {
			return list.size();
		}

		public long getGroupId(int groupPosition) {
			return list.get(groupPosition).id;
		}

		private View createShowingDateView(Showing s) {
			TextView textView = (TextView) View.inflate(context,
					R.layout.showingdate, null);
			textView.setText(mShowingsDateFormatter.format(s.date));
			return textView;
		}

		private View createShowingsView(Showing[] showings, int offset) {

			GridView grid = (GridView) View.inflate(context,
					R.layout.showinggrid, null);

			List<Showing> rowShowings = new ArrayList<Showing>();

			int i = 0;
			while (i < 4 && (i + offset) < showings.length) {
				rowShowings.add(showings[i + offset]);
				i++;
			}

			ArrayAdapter<Showing> adapter = new ShowingsListAdapter(context,
					R.layout.showingentry, rowShowings);
			grid.setAdapter(adapter);

			return grid;
		}

		// child related ...
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			int count = 0;
			for (Showing[] showings : list.get(groupPosition).showingsPerDay(
					after)) {

				if (count == childPosition) {
					// text view, current date
					return createShowingDateView(showings[0]);
				}
				count++;

				int showingRows = Math.max(1, (showings.length / 4));

				for (int i = 0; i < showingRows; i++) {
					if (count == childPosition) {
						return createShowingsView(showings, i * 4);
					}
					count++;
				}
			}

			return null;
		}

		public Object getChild(int groupPosition, int childPosition) {
			return list.get(groupPosition).showingsPerDay(after).get(
					childPosition);
		}

		public int getChildrenCount(int groupPosition) {

			int count = 0;

			for (Showing[] showings : list.get(groupPosition).showingsPerDay(
					after)) {
				count++;
				int showingRows = Math.max(1, (showings.length / 4));
				count += showingRows;
			}

			return count;
		}

		public long getChildId(int groupPosition, int childPosition) {
			return (groupPosition + 1) * (childPosition + 1);
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		// other ...

		public boolean hasStableIds() {
			return true;
		}
	}

	private final class BackgroundImageFetcher extends
			AsyncTask<Void, Void, Bitmap> {
		private final ImageView filmImage;
		private final Film film;

		private BackgroundImageFetcher(ImageView filmImage, Film film) {
			this.filmImage = filmImage;
			this.film = film;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			try {

				Bitmap bmp = cache.get(film.image);
				if (null == bmp) {
					bmp = BitmapFactory.decodeStream(new URL(
							"http://www.cineworld.co.uk" + film.image)
							.openStream());
					cache.put(film.image, bmp);
				}

				return bmp;
			} catch (Exception e) {
				return null;
			}
		}

		protected void onPostExecute(Bitmap result) {
			if (null != result) {
				Long tag = (Long) filmImage.getTag();
				if (tag.equals(film.id)) {
					filmImage.setImageBitmap(result);
				}
			}
		}
	}

}