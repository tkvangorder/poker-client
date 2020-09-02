package org.homepoker.client.util;

import lombok.extern.slf4j.Slf4j;

/**
 * This class can be used to log each element of a Flux while also counting the number of elements.
 * 
 * @author tyler.vangorder
 *
 */
@Slf4j
public class CountingLogger<T> {
	
	int count = 0;
	
	public void logElement(T element) {
		count++;
		log.info(JsonUtils.toJson(element));
	}

	public int getCount() {
		return count;
	}
	
}
