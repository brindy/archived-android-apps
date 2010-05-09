package uk.org.brindy.android.cineworldgold;

import java.net.URL;
import java.util.List;

import uk.org.brindy.android.cineworldgold.support.Cinema;
import uk.org.brindy.android.cineworldgold.support.CinemaListParser;
import android.os.AsyncTask;
import android.util.Log;

public class CinemaListManager {

	private Listener mListener;

	private List<Cinema> mCinemas;

	public CinemaListManager(Listener listener) {
		this.mListener = listener;
	}

	@SuppressWarnings("unchecked")
	void getCinemaList() {
		new AsyncTask() {

			private boolean error = false;

			@Override
			protected Object doInBackground(Object... params) {
				try {
					mCinemas = new CinemaListParser().parse(new URL(
							"http://www.cineworld.co.uk/cinemas").openStream());
				} catch (Exception e) {
					Log.e("Cineworld", "Unable to access Cineworld's website",
							e);
					error = true;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Object result) {

				if (error) {
					mListener.error();
				} else {
					mListener.setCinemaList(mCinemas);
				}
			}

		}.execute();

	}

	Cinema findCinemaById(long id) {
		for (Cinema cinema : mCinemas) {
			if (cinema.id == id) {
				return cinema;
			}
		}
		return null;
	}

	Cinema findCinemaByName(String name) {
		for (Cinema cinema : mCinemas) {
			if (cinema.name.equals(name)) {
				return cinema;
			}
		}

		return null;
	}

	interface Listener {

		void setCinemaList(List<Cinema> cinema);

		void error();

	}

}
