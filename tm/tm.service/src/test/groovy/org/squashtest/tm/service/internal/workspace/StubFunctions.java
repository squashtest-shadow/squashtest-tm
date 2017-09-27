package org.squashtest.tm.service.internal.workspace;

import com.sun.prism.impl.Disposer;
import org.jooq.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StubFunctions {

	public static Function<Record, Map<String,Object>> getLeftTupleTransformer(){
		return record -> {
			Map<String, Object> map = new HashMap<>();
			map.put("LEFT_ID", record.get("LEFT_ID"));
			map.put("LEFT_NAME", record.get("LEFT_NAME"));
			map.put("LEFT_ATTR", record.get("LEFT_ATTR"));
			return map;
		};
	}

	public static Function<Record, Map<String,Object>> getRightTupleTransformer(){
		return record -> {
			if(record.get("RIGHT_ID") == null){
				return null;
			}
			Map<String, Object> map = new HashMap<>();
			map.put("RIGHT_ID", record.get("RIGHT_ID"));
			map.put("RIGHT_NAME", record.get("RIGHT_NAME"));
			return map;
		};
	}

	public static BiConsumer<Map<String,Object>,List<Map<String,Object>>> injector (){
		return (keyMap, valueMaps) -> {
			keyMap.put("RIGHT_ELEMENTS", valueMaps);
		};
	}


}
