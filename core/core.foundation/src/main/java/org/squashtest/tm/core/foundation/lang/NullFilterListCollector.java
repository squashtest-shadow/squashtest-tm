package org.squashtest.tm.core.foundation.lang;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class NullFilterListCollector<T>
	implements Collector<T, ArrayList<T>, List<T>> {

	@Override
	public Supplier<ArrayList<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<ArrayList<T>, T> accumulator() {
		return (list, element) -> {
			if(Objects.nonNull(element)){
				list.add(element);
			}
		};
	}

	@Override
	public BinaryOperator<ArrayList<T>> combiner() {
		return (ts, ts2) -> {
			ts.addAll(ts2);
			return ts;
		};
	}

	@Override
	public Function<ArrayList<T>, List<T>> finisher() {
		return null;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
	}

	public static <T> Collector<T, ?, List<T>> toNullFilteredList(){
		return new NullFilterListCollector<T>();
	}
}
