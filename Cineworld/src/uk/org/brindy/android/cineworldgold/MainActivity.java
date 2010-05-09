package uk.org.brindy.android.cineworldgold;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.org.brindy.android.cineworldgold.FilmListManager.Listener;
import uk.org.brindy.android.cineworldgold.SettingsManager.SortFilmsBy;
import uk.org.brindy.android.cineworldgold.support.BinaryCache;
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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

	private FilmListManager mFilmListManager;

	private SettingsManager mSettingsManager;

	private BinaryCache mBinaryCache;

	private List<Film> mFilms;

	private MenuItem mToggle;

	private ProgressDialog mProgress;

	private TextView mSortTypeText;

	private ExpandableListView mList;

	private View mEmpty;

	public MainActivity() {
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mList = (ExpandableListView) findViewById(android.R.id.list);
		mList.setVisibility(View.GONE);

		mEmpty = findViewById(android.R.id.empty);
		mEmpty.setVisibility(View.VISIBLE);

		mSettingsManager = new SettingsManager(getDir("data",
				Context.MODE_PRIVATE));

		mBinaryCache = new BinaryCache(getCacheDir());
		mFilmListManager = new FilmListManager(getDir("data",
				Context.MODE_PRIVATE), this, mBinaryCache);

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

	public void error() {
		Toast.makeText(
				this,
				"Unable to connect to server.  "
						+ "Please check settings and try again later.",
				Toast.LENGTH_LONG).show();
		finish();
	}

	public void setFilmImage(Film film, Bitmap bmp) {
		ExpandableListView view = (ExpandableListView) findViewById(android.R.id.list);

		int index = mFilms.indexOf(film);
		if (index >= 0 && index < view.getChildCount()) {
			ImageView filmImage = (ImageView) view.getChildAt(index)
					.findViewById(R.id.filmImage);
			filmImage.setImageBitmap(bmp);
			view.getChildAt(index).findViewById(R.id.withoutImage)
					.setVisibility(View.GONE);
			view.getChildAt(index).findViewById(R.id.withImage).setVisibility(
					View.VISIBLE);
		}

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

				mList.setAdapter(new BaseExpandableListAdapter() {

					// group related

					public View getGroupView(int groupPosition,
							boolean isExpanded, View convertView,
							ViewGroup parent) {
						Film film = list.get(groupPosition);

						// inflate the view...
						View view = convertView;
						if (null == view) {
							view = View.inflate(context,
									R.layout.filmlistentry, null);
						}

						// populate the items in the view...
						ImageView filmImage = (ImageView) view
								.findViewById(R.id.filmImage);
						Bitmap bmp = null;
						try {
							bmp = BitmapFactory.decodeStream(mBinaryCache
									.getBinary(String.valueOf(film.id)));

							filmImage.setImageBitmap(bmp);
						} catch (IOException e) {
							Log.e("Cineworld", e.getMessage(), e);
						}

						if (null != bmp) {
							view.findViewById(R.id.withoutImage).setVisibility(
									View.GONE);
							view.findViewById(R.id.withImage).setVisibility(
									View.VISIBLE);
						}

						TextView filmName = (TextView) view
								.findViewById(R.id.filmName);
						filmName.setText(film.name);

						TextView filmInfo = (TextView) view
								.findViewById(R.id.filmInfo);
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

					// child related ...
					public View getChildView(int groupPosition,
							int childPosition, boolean isLastChild,
							View convertView, ViewGroup parent) {

						Showing[] showings = list.get(groupPosition)
								.showingsPerDay(after).get(childPosition);

						// inflate the view...
						LinearLayout view = (LinearLayout) convertView;
						if (null == view) {
							view = (LinearLayout) View.inflate(context,
									R.layout.showingsentry, null);
						}

						// populate the items in the view...
						TextView showingDay = (TextView) view
								.findViewById(R.id.showingDay);
						showingDay.setText(mShowingsDateFormatter
								.format(showings[0].date));

						// add each showing to the grid
						GridView showingTimes = (GridView) view
								.findViewById(R.id.showingTimes);

						int cols = parent.getWidth() / 70;

						showingTimes.setNumColumns(cols);

						showingTimes.setAdapter(new ShowingsListAdapter(
								MainActivity.this, showings));

						return view;
					}

					public Object getChild(int groupPosition, int childPosition) {
						return list.get(groupPosition).showingsPerDay(after)
								.get(childPosition);
					}

					public int getChildrenCount(int groupPosition) {
						return list.get(groupPosition).showingsPerDay(after)
								.size();
					}

					public long getChildId(int groupPosition, int childPosition) {
						return list.get(groupPosition).showingsPerDay(after)
								.get(childPosition).hashCode();
					}

					public boolean isChildSelectable(int groupPosition,
							int childPosition) {
						return true;
					}

					// other ...

					public boolean hasStableIds() {
						return false;
					}

				});

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

}