package uk.org.brindy.android.cineworldgold.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Film implements Serializable {

	private static final long serialVersionUID = 1L;

	public long id;

	public String name;

	public String image;

	public Showing[] showings;

	public List<Showing[]> showingsPerDay(Date after) {
		List<Showing[]> showingsPerDay = new LinkedList<Showing[]>();

		List<Showing> showings = new ArrayList<Showing>();

		for (Showing showing : this.showings) {
			if (showing.date.after(after)) {
				// is it the same day as the last one stored?
				if (showings.size() > 0
						&& !sameDay(showings.get(0).date, showing.date)) {
					showingsPerDay.add(showings.toArray(new Showing[showings
							.size()]));
					showings = new ArrayList<Showing>();
				}
				showings.add(showing);
			}
		}

		if (showings.size() > 0) {
			showingsPerDay.add(showings.toArray(new Showing[showings.size()]));
		}

		return showingsPerDay;
	}

	public List<Showing> nextShowings(Date after, int limit) {
		List<Showing> results = new LinkedList<Showing>();

		List<Showing[]> showings = showingsPerDay(after);
		for (Showing[] day : showings) {
			for (Showing show : day) {
				results.add(show);
				if (results.size() == limit) {
					return results;
				}
			}
		}

		return results;
	}

	public Showing nextShowing() {

		List<Showing> results = nextShowings(new Date(), 1);
		if (null != results && results.size() > 0) {
			return results.get(0);
		}

		return null;
	}

	private boolean sameDay(Date d1, Date d2) {
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		int day1 = c.get(Calendar.DATE);
		c.setTime(d2);
		int day2 = c.get(Calendar.DATE);
		return day1 == day2;
	}
}
