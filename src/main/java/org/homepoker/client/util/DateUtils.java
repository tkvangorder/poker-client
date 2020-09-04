package org.homepoker.client.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Date;

public class DateUtils {

	static DateTimeFormatter dateTimeFormatter =  new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd HH:mm")
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
			.parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
			.parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
			.toFormatter()
			.withResolverStyle(ResolverStyle.SMART)
			.withZone(ZoneId.systemDefault());

	
	
	public static Date stringToDate(String stringDate) {		
		 return Date.from(LocalDateTime.parse(stringDate, dateTimeFormatter.withZone(ZoneId.systemDefault())).atZone(ZoneId.systemDefault()).toInstant());
	}

}
