package uk.org.brindy.android.cineworldgold.support;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Showing implements Serializable {

	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");

	private static final long serialVersionUID = 1L;

	public int id;

	public Date date;

	@Override
	public String toString() {
		return dateFormatter.format(date);
	}

}
