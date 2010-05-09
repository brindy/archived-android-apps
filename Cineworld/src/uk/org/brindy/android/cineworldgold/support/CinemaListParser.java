package uk.org.brindy.android.cineworldgold.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CinemaListParser {

	public List<Cinema> parse(InputStream in) throws IOException {
		List<Cinema> list = new ArrayList<Cinema>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String line = "";
		while (!line.contains("<select id=\"cinema\"")) {
			line = reader.readLine();
			if (null == line) {
				throw new RuntimeException("Page format has changed.");
			}
		}
		
		// great site, gives you regex in java string format:
		// http://www.fileformat.info/tool/regex.htm
		Pattern p = Pattern
				.compile("<option value=\"([^\"]+)\"(?: selected=\"selected\")?>([^<]+)</option>");
		while (!line.contains("</select>")) {
			line = reader.readLine().trim();

			Matcher m = p.matcher(line);
			if (m.matches()) {
				Cinema cinema = new Cinema();
				cinema.id = Long.parseLong(m.group(1));
				cinema.name = m.group(2);
				list.add(cinema);
			}
		}

		return list;
	}
}
