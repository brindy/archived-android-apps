package uk.org.brindy.android.cineworldgold;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.org.brindy.android.cineworldgold.support.BinaryCache;
import uk.org.brindy.android.cineworldgold.support.Cinema;
import uk.org.brindy.android.cineworldgold.support.Film;
import uk.org.brindy.android.cineworldgold.support.FilmListParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class FilmListManager {

	private static final int TIMEOUT = 1000 * 60 * 60 * 24;

	private static final NullListener NULL_LISTENER = new NullListener();

	private BinaryCache mBinaryCache;

	private Listener mListener;

	private Cinema mCinema;

	private long mLastModified = 0L;

	private List<Film> mFilms = Collections.emptyList();

	private File mObjects;

	public FilmListManager(File dataDir, BinaryCache binaryCache) {
		this(dataDir, NULL_LISTENER, binaryCache);
	}

	@SuppressWarnings("unchecked")
	public FilmListManager(File dataDir, Listener listener,
			BinaryCache binaryCache) {
		this.mBinaryCache = binaryCache;
		this.mListener = listener;

		mObjects = new File(dataDir, getClass().getName());
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(mObjects));
			mLastModified = (Long) in.readObject();
			mCinema = (Cinema) in.readObject();
			mFilms = (List<Film>) in.readObject();
			in.close();
			listener.setFilmList(mFilms);
		} catch (Exception e) {
			Log.d("Cineworld", e.toString(), e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e("Cineworld", e.toString(), e);
				}
			}
		}
	}

	/**
	 * Get the film list. This returns the current film list which could be
	 * empty. If you want the most up to date list construct this with a
	 * listener as requests are done asynchronously.
	 * 
	 * @return the current film list
	 */
	List<Film> getFilmList() {
		// if the last time we retrieved a list was more than one day ago, it's
		// time to do it again
		if (null == mFilms || mFilms.size() == 0
				|| System.currentTimeMillis() - mLastModified > TIMEOUT) {
			// query the currently selected cinema's schedule
			query();
		} else {
			mListener.setFilmList(mFilms);
		}

		return mFilms;
	}

	void setCinema(Cinema cinema) {
		mCinema = cinema;
		query();
	}

	Cinema getCinema() {
		return mCinema;
	}

	@SuppressWarnings("unchecked")
	private void query() {
		mListener.setFilmList(Collections.EMPTY_LIST);
		if (null != mCinema) {
			new FilmListBackgroundLoader(mListener).execute();
		}
	}

	private static final class NullListener implements Listener {
		public void error() {
		}

		public void setFilmImage(Film arg0, Bitmap arg1) {
		}

		public void setFilmList(List<Film> arg0) {
		}

		public void updateStatus(String arg0) {
		}
	}

	@SuppressWarnings("unchecked")
	private final class FilmListBackgroundLoader extends AsyncTask {

		private final Listener listener;

		public FilmListBackgroundLoader(Listener listener) {
			this.listener = listener;
		}

		@Override
		protected Object doInBackground(Object... params) {

			try {
				URL u = new URL("http://www.cineworld.co.uk/cinemas/"
						+ mCinema.id);

				HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setRequestProperty("User-Agent", "Mozilla/4.0  ( compatible ) ");

				int code = conn.getResponseCode();
				String msg = conn.getResponseMessage();
				System.out.println("response (" + code + ") : " + msg);

				mFilms = new FilmListParser(new Date()).parse(conn
						.getInputStream());
			} catch (Exception e) {
				Log.e("Cineworld", "Unable to access Cineworld's website", e);
				// if we're not online, this could happen
				return null;
			}

			try {
				mLastModified = System.currentTimeMillis();

				ObjectOutputStream stream = new ObjectOutputStream(
						new FileOutputStream(mObjects));
				stream.writeObject(new Long(mLastModified));
				stream.writeObject(mCinema);
				stream.writeObject(mFilms);
				stream.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return mFilms;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (null == result || ((List) result).isEmpty()) {
				listener.error();
				return;
			}

			mListener.setFilmList(mFilms);
			mBinaryCache.clear();
			new FilmImageBackgroundLoader(mFilms).execute();
		}
	}

	@SuppressWarnings("unchecked")
	private final class FilmImageBackgroundLoader extends AsyncTask {
		private final List<Film> films;

		private FilmImageBackgroundLoader(List<Film> films) {
			this.films = films;
		}

		@Override
		protected Object doInBackground(Object... params) {
			for (Film film : films) {
				try {
					mBinaryCache.addBinary(String.valueOf(film.id), new URL(
							"http://www.cineworld.co.uk" + film.image)
							.openStream());
				} catch (Exception e) {
					// ignore
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			try {
				for (Film film : films) {
					mListener.setFilmImage(film, BitmapFactory
							.decodeStream(mBinaryCache.getBinary(String
									.valueOf(film.id))));
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	interface Listener {

		/**
		 * Update the status, if this is called then this class is a little busy
		 * and the message explains why.
		 * 
		 * @param msg
		 *            the reason why we're busy
		 */
		void updateStatus(String msg);

		/** Called when there was an error - the listener should alert the user. */
		void error();

		/**
		 * The films are ready. Any busy state that has been set should be
		 * reset.
		 * 
		 * @param films
		 */
		void setFilmList(List<Film> films);

		/**
		 * Set the image of a film.
		 * 
		 * @param film
		 *            the film
		 * @param bmp
		 *            the image
		 */
		void setFilmImage(Film film, Bitmap bmp);

	}

}
