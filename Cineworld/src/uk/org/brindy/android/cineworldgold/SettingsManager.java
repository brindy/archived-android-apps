package uk.org.brindy.android.cineworldgold;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingsManager {

	private SortFilmsBy mSortFilmsBy;

	private File mObjects;

	public SettingsManager(File dataDir) {

		mObjects = new File(dataDir, getClass().getName());
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(mObjects));
			mSortFilmsBy = (SortFilmsBy) in.readObject();
			in.close();
			in = null;
		} catch (Exception e) {
			// noop
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e1) {
					// ignore
				}
			}
		}
	}

	public SortFilmsBy getSortFilmsBy() {
		return mSortFilmsBy;
	}

	public void setSortFilmsBy(SortFilmsBy sort) {
		this.mSortFilmsBy = sort;
		save();
	}

	public enum SortFilmsBy {
		NAME, TIME
	};

	private void save() {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream(mObjects));
			stream.writeObject(mSortFilmsBy);
			stream.close();
		} catch (Exception e) {
			if (null != stream) {
				try {
					stream.close();
				} catch (IOException ex) {
					// noop
				}
			}
			mObjects.delete();
			throw new RuntimeException(e);
		}
	}

}
