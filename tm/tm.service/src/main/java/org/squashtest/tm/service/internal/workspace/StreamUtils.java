package org.squashtest.tm.service.internal.workspace;

import org.jooq.Record;
import org.squashtest.tm.domain.Identified;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static org.squashtest.tm.core.foundation.lang.NullFilterListCollector.toNullFilteredList;

public class StreamUtils {

	public static <I extends Record, K, V> List<K>  performJoinAggregate(Function<I,K> leftTupleTransformer,
																			   Function<I,V> rightTupleTransformer,
																			   Function<Map.Entry<K,List<V>>, K> injector,
																			   Collection<I> records) {

		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(injector)
			.collect(Collectors.toList());
	}

	public static <I extends Record, K, V> List<K>  performJoinAggregate(Function<I,K> leftTupleTransformer,
																		 Function<I,V> rightTupleTransformer,
																		 BiConsumer<K,List<V>> injector,
																		 Collection<I> records) {

		Function<Map.Entry<K, List<V>>, K> function = entry -> {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			injector.accept(key,value);
			return key;
		};

		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(function)
			.collect(Collectors.toList());
	}

	public static <I extends Record, K extends Identified, V>  Map<Long,K>  performJoinAggregateIntoMap(Function<I,K> leftTupleTransformer,
																										Function<I,V> rightTupleTransformer,
																										BiConsumer<K,List<V>> injector,
																										Collection<I> records) {

		Function<Map.Entry<K, List<V>>, K> function = entry -> {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			injector.accept(key,value);
			return key;
		};

		return records.stream().collect(
			Collectors.groupingBy(
				leftTupleTransformer,
				mapping(
					rightTupleTransformer,
					toNullFilteredList()
				)))
			.entrySet().stream()
			.map(function)
			.collect(Collectors.toMap(Identified::getId, Function.identity()));
	}
}
