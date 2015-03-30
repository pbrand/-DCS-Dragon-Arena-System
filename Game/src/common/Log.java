package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Log {
	
	public static void log(String tag, String text) {
		String file = "log.txt";
		String string = "[" + tag + "]: " + text;
		System.out.println(string);
		saveLog(file, string);
	}

	private static void saveLog(String file, String text) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(file, true)))) {
			out.println(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
