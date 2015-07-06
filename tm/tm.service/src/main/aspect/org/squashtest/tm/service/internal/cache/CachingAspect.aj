import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.annotation.CachableType;
import org.squashtest.tm.service.annotation.CacheScope;
import org.squashtest.tm.service.annotation.CacheResult;

public aspect CachingAspect percflow(copy()){

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingAspect.class);
	
	private Map<String, List<CustomFieldBinding>> cache = new HashMap<String, List<CustomFieldBinding>>();
	private Map<String, Boolean> infoListCache = new HashMap<String, Boolean>();

pointcut copy() : execution (@CacheScope * *(..));

pointcut cachedMethod(CacheResult anno) : execution (@CacheResult  * *(..)) && @annotation(anno);


boolean around(CacheResult anno, long projectId, String itemCode) : cachedMethod(anno) && args(projectId, itemCode){

String key = anno.type().toString() + String.valueOf(projectId) + itemCode;
Boolean cached = infoListCache.get(key);
if (cached == null){
 Boolean result = proceed(anno, projectId, itemCode);
 infoListCache.put(key, result);
 return result;
} else {
return cached;
}

}

List<CustomFieldBinding> around(CacheResult anno, BoundEntity entity) : cachedMethod(anno) && if (anno.type() == CachableType.CUSTOM_FIELD) && args(entity){

String key = entity.getBoundEntityType().toString() + entity.getProject().getId().toString();
List<CustomFieldBinding> cached = cache.get(key);

if (cached == null) {
List<CustomFieldBinding> result = proceed(anno, entity);
cache.put(key, result);
return result;
} else {
return cached;
}
}

}