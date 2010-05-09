package uk.org.brindy.android.cineworldgold.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WidgetState {

	public enum UpdateType {
		REFRESH, CLICK
	};

	private final File file;

	private int position;

	private long lastUpdateTime;

	private UpdateType updateType = UpdateType.REFRESH;

	public WidgetState(File dir) {
		this.file = new File(dir, getClass().getName());

		// load the information
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			position = (Integer) in.readObject();
			lastUpdateTime = (Long) in.readObject();
			updateType = (UpdateType) in.readObject();
		} catch (Exception ex) {
			// could happen
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
		save();
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
		save();
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
		save();
	}

	public boolean needsUpdate() {
		return (System.currentTimeMillis() - lastUpdateTime) > 15 * 1000;
	}

	private void save() {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject((Integer) position);
			out.writeObject((Long) lastUpdateTime);
			out.writeObject(updateType);
		} catch (Exception ex) {
			// err... ?
			ex.printStackTrace();
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
