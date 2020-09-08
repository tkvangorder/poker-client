package org.homepoker.client.util;

public class StringUtils {
	private StringUtils() {
	}

	/**
	 * Convert a string value to a boolean.
	 * @param value
	 * @return
	 */
	public static boolean stringToboolean(String value) {
		if (value == null) {
			return false;
		}
		switch (value.toLowerCase()) {
			case "t":
			case "true":
			case "y":
			case "yes": 
				return true;
			default:
				return false;
		}
	}
}
