package common;

import java.util.Random;

public class Common {
	
	public static String randomString(int len) {
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}
			
	public static String getFormatedTime(long time) {
		return ((time) / (1000 * 60)) % 60 + " m " + ((time) / (1000)) % 60 + " s";
	}

}
