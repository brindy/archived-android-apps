package uk.org.brindy.android.cineworldgold;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.org.brindy.android.cineworldgold.support.Cinema;
import uk.org.brindy.android.cineworldgold.support.Film;
import uk.org.brindy.android.cineworldgold.support.Showing;
import uk.org.brindy.android.cineworldgold.support.WidgetState;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

/**
 * 
 * @author brindy
 */
public class CineworldWidgetProvider extends AppWidgetProvider {

	public CineworldWidgetProvider() {
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		WidgetState state = new WidgetState(context.getDir("data",
				Context.MODE_PRIVATE));
		if (state.needsUpdate()) {
			state.setPosition(state.getPosition() + 1);
			context.startService(new Intent(context, UpdateService.class));
		}
	}

	@SuppressWarnings("unchecked")
	private static void doUpdate(final Context context, WidgetState state,
			final RemoteViews views) {

		FilmListManager mgr = new FilmListManager(context.getDir("data",
				Context.MODE_PRIVATE));

		List<Film> films = mgr.getFilmList();
		if (films.isEmpty()) {
			views.setViewVisibility(R.id.filmInfo, View.GONE);
			views.setViewVisibility(R.id.widgetHelp, View.VISIBLE);
		} else {
			views.setTextViewText(R.id.helpText, "Please wait...");

			// cycle through the list
			int position = state.getPosition();

			if (position >= films.size()) {
				position = 0;
			} else if (position < 0) {
				position = films.size() - 1;
			}
			state.setPosition(position);

			Cinema c = mgr.getCinema();
			views.setTextViewText(R.id.filmLocation, c.name);

			final Film f = films.get(position);
			views.setTextViewText(R.id.filmName, f.name);

			// views.setImageViewBitmap(R.id.filmImage, bmp);
			Bitmap bmp = MainActivity.cache.get(f.image);
			if (null == bmp) {
				new AsyncTask() {

					@Override
					protected Object doInBackground(Object... params) {
						try {
							return BitmapFactory.decodeStream(new URL(
									"http://www.cineworld.co.uk" + f.image)
									.openStream());
						} catch (Exception e) {
							return null;
						}
					}

					@Override
					protected void onPostExecute(Object result) {

						if (null != result) {
							views.setImageViewBitmap(R.id.filmImage,
									(Bitmap) result);
							updateWidget(context, views);
						}

					}

				}.execute();
			} else {
				views.setImageViewBitmap(R.id.filmImage, bmp);
			}

			List<Showing> nextShowings = f.nextShowings(new Date(), 2);
			views.setTextViewText(R.id.showingNext1, "");
			views.setTextViewText(R.id.showingNext2, "");

			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE HH:mm");
			switch (nextShowings.size()) {

			case 2:
				// fall through
				views.setTextViewText(R.id.showingNext2, dateFormatter
						.format(nextShowings.get(1).date));
			case 1:
				views.setTextViewText(R.id.showingNext1, dateFormatter
						.format(nextShowings.get(0).date));
				break;

			case 0:
				views.setTextViewText(R.id.showingNext1, "No more showings");
				break;

			}
			views.setViewVisibility(R.id.filmInfo, View.VISIBLE);
			views.setViewVisibility(R.id.widgetHelp, View.GONE);
		}

		// add a click handler - Open the app
		addOpenAppHandler(context, views);

		// add a click handler - Move back a position
		addPreviousFilmHandler(context, views);

		// add a click handler - Skip to next film
		addNextFilmHandler(context, views);

		// update the app widget
		updateWidget(context, views);

		state.setLastUpdateTime(System.currentTimeMillis());
	}

	private static void updateWidget(Context context, RemoteViews views) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		appWidgetManager.updateAppWidget(new ComponentName(context,
				CineworldWidgetProvider.class), views);
	}

	private static void addOpenAppHandler(Context context, RemoteViews views) {
		Intent defineIntent = new Intent(Intent.ACTION_MAIN, null, context,
				MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				defineIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		views.setOnClickPendingIntent(R.id.widget_go, pendingIntent);
	}

	private static void addNextFilmHandler(Context context, RemoteViews views) {
		Intent i = new Intent(context, NextFilmService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);
	}

	private static void addPreviousFilmHandler(Context context,
			RemoteViews views) {
		Intent i = new Intent(context, PreviousFilmService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_prev, pendingIntent);
	}

	public static class UpdateService extends Service {

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public void onStart(Intent intent, int startId) {
			Context context = this;
			WidgetState state = new WidgetState(context.getDir("data",
					Context.MODE_PRIVATE));
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			doUpdate(context, state, views);
			stopSelf();
		}

	}

	public static class NextFilmService extends ModifyFilmPosition {

		@Override
		int modifier() {
			return 1;
		}

	}

	public static class PreviousFilmService extends ModifyFilmPosition {

		@Override
		int modifier() {
			return -1;
		}
	}

	public abstract static class ModifyFilmPosition extends Service {

		@Override
		public void onStart(Intent intent, int startId) {

			RemoteViews views = new RemoteViews(getPackageName(),
					R.layout.widget);
			views.setViewVisibility(R.id.filmInfo, View.GONE);
			views.setViewVisibility(R.id.widgetHelp, View.VISIBLE);
			views.setTextViewText(R.id.helpText, "Please wait...");
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			appWidgetManager.updateAppWidget(new ComponentName(this,
					CineworldWidgetProvider.class), views);

			WidgetState state = new WidgetState(getDir("data",
					Context.MODE_PRIVATE));
			state.setPosition(state.getPosition() + modifier());
			state.setLastUpdateTime(0);
			startService(new Intent(this, UpdateService.class));
			stopSelf();
		}

		abstract int modifier();

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

	}

}
