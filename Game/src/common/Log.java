package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
	private static String file;

	public static void log(String tag, String text, boolean display) {
		if (file == null)
			file = "log" + getDate() + ".txt";
		String string = "[" + tag + "]: " + text;
		if (display) {
			System.out.println(string);
		}
		saveLog(file, string);
	}

	public static void log(String tag, String text) {
		log(tag, text, true);
	}

	public static void logMetric(String tag, String text) {
		String file = "log_metric.txt";
		String string = "[" + tag + "]: " + text;
		System.out.println(string);
		saveLog(file, string);
	}

	private static void saveLog(String file, String text) {
		File log = new File("log");
		if (!log.exists()) {
			log.mkdir();
		}

		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(log.getAbsolutePath() + "/" + file),
						true)))) {
			out.println(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Calendar cal = Calendar.getInstance();
		String res = dateFormat.format(cal.getTime());
		return res.replace(" ", "_");
	}

}
