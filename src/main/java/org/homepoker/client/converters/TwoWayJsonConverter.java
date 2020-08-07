package org.homepoker.client.converters;

import java.io.IOException;
import java.util.Set;

import org.homepoker.client.util.JsonUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

public class TwoWayJsonConverter implements GenericConverter {

	private final Class<?> clazz;
		
	public TwoWayJsonConverter(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Set.of(
				new ConvertiblePair(String.class, clazz),
				new ConvertiblePair(clazz, String.class)
			);
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		try {
			if (sourceType.getType() == String.class) {
				return JsonUtils.readValue((String)source, clazz);
			} else {
				return JsonUtils.toJson((String)source);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return null;
	}

}
