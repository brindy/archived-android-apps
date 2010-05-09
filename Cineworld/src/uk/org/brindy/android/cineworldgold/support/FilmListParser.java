package uk.org.brindy.android.cineworldgold.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilmListParser {

	private Date mToday;

	private Pattern filmDetailPattern = Pattern
			.compile("film=(\\d+)\"><img alt=\"([^\"]+)\" src=\"([^\"]+)\" /></a>");

	private Pattern filmTimePattern = Pattern
			.compile("performance=([^\"]+)\">([^:]+):([^<]+)");

	private SimpleDateFormat filmDateParser = new SimpleDateFormat(
			"'<dt>'EEE dd MMM'</dt>'");

	public FilmListParser(Date today) {
		mToday = today;
	}

	public List<Film> parse(InputStream in) throws IOException, ParseException {

		List<Film> films = new ArrayList<Film>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String line = null;
		while (null != (line = reader.readLine())) {
			line = line.trim();

			if (line.contains("film=")) {
				System.out.println(line);
			}

			Matcher m = filmDetailPattern.matcher(line);
			if (m.find()) {
				Film film = new Film();
				film.id = Integer.parseInt(m.group(1));
				film.name = m.group(2);
				film.image = m.group(3);

				ArrayList<Showing> showings = findShowings(reader);

				Calendar showingDateTime = Calendar.getInstance();
				showingDateTime.setTime(mToday);

				if (showings.size() > 0) {
					film.showings = showings.toArray(new Showing[showings
							.size()]);
					films.add(film);
				}
			}
		}

		return films;
	}

	private ArrayList<Showing> findShowings(BufferedReader reader)
			throws IOException, ParseException {
		ArrayList<Showing> showings = new ArrayList<Showing>();

		String line = "";
		while (!line.trim().startsWith("</div>")) {
			if (line.contains("<dt>")) {
				// we've found the date
				Calendar showingDateTime = Calendar.getInstance();
				showingDateTime.setTime(mToday);

				Calendar date = Calendar.getInstance();
				date.setTime(filmDateParser.parse(line.trim()));

				showingDateTime.set(Calendar.DAY_OF_MONTH, date
						.get(Calendar.DAY_OF_MONTH));
				showingDateTime.set(Calendar.MONTH, date.get(Calendar.MONTH));

				// now find the actual times
				while (!line.contains("</dl>")) {

					Matcher m = filmTimePattern.matcher(line.trim());
					if (m.find()) {
						Showing showing = new Showing();
						showing.id = Integer.parseInt(m.group(1));

						showingDateTime.set(Calendar.HOUR_OF_DAY, Integer
								.parseInt(m.group(2)));
						showingDateTime.set(Calendar.MINUTE, Integer.parseInt(m
								.group(3)));

						showing.date = showingDateTime.getTime();

						showings.add(showing);
					}

					line = reader.readLine();
				}
			} else {
				line = reader.readLine();
			}
		}

		return showings;
	}

}
