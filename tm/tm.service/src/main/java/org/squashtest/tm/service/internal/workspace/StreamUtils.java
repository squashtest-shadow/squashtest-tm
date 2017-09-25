package org.squashtest.tm.service.internal.workspace;

import org.jooq.Record;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.mapping;
import static org.squashtest.tm.core.foundation.lang.NullFilterListCollector.toNullFilteredList;

public class StreamUtils {

	public static <I extends Record, K, V> List<? extends V> performJoinAggregate(Function<? super I, ? extends K> keyCreator,
																			   Function<? super I, ? extends V> valueCreator,
																			   Function<? super Map.Entry<? extends K, ? extends List<? extends V>>, ? extends V> injector,
																			   Collection<? extends I> records) {

		return records.stream().collect(
			Collectors.groupingBy(
				keyCreator,
				mapping(
					valueCreator,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(injector)
			.collect(Collectors.toList());
	}
}
