package uk.org.brindy.android.cineworldgold.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class BinaryCache {

	private final File mDir;

	private Map<String, InputStream> openHandles = new HashMap<String, InputStream>();

	private Set<String> updated;

	public BinaryCache(File dir) {
		this.mDir = dir;
	}

	/** Clears the cache. */
	public void clear() {
		for (InputStream in : openHandles.values()) {
			try {
				in.close();
			} catch (IOException e) {
				Log.e("Cineworld", e.getMessage(), e);
			}
		}

		for (File f : mDir.listFiles()) {
			if (!f.delete()) {
				Log.w(getClass().getName() + "#clear", "Failed to delete "
						+ f.getName() + " from cache");
			}
		}

	}

	/**
	 * Start a cache update. Anything not updated via #addBinary will be deleted
	 * when endUpdate is called.
	 */
	public void startUpdate() {
		if (null != updated) {
			throw new IllegalStateException("Update already started");
		}
		updated = new HashSet<String>();
	}

	/** End an update. Anything not updated via #addBinary will be deleted. */
	public void endUpdate() {

		if (null != updated) {
			for (File file : mDir.listFiles()) {
				if (!updated.contains(file.getName())) {
					if (!file.delete()) {
						Log.w("Cineworld", "Failed to delete " + file);
					}
				}
			}

			updated = null;
		}

	}

	/** Add some named binary to the cache. */
	public void addBinary(String name, InputStream in) throws IOException {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(new File(mDir, name));
			byte[] buffer = new byte[1024];

			int read = 0;
			while (-1 != (read = in.read(buffer))) {
				out.write(buffer, 0, read);
			}

			if (updated != null) {
				updated.add(name);
			}

		} finally {
			try {
				out.close();
			} catch (IOException e) {
				Log.e("Cineworld", e.getMessage(), e);
			}
		}
	}

	/**
	 * Get a named binary.
	 * 
	 * @param name
	 *            the name of the binary
	 * @return the binary as an input stream
	 * @throws IllegalStateException
	 *             if this cache is currently being updated
	 */
	public InputStream getBinary(String name) throws IOException {

		if (updated != null) {
			throw new IllegalStateException("Update in progress");
		}

		try {
			return new FileInputStream(new File(mDir, name)) {
				@Override
				public void close() throws IOException {
					super.close();
					openHandles.remove(this);
				}
			};
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/** Get the number of items in this cache. */
	public int size() {
		return mDir.list().length;
	}

}
