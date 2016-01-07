package org.squashtest.tm.service.annotation;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface used to implements objects involved in the second phase of coerce process for {@link PreventConcurrents} and {@link BatchPreventConcurrent}.
 * <code>
 * The coercing process is a two phase process :
 * <ol>
 * <li>Retrieve the annotated ({@link Id} and/or {@link Ids}}) arguments of the advised method and convert them to a list of ids. Done by an object realizing {@link IdsCoercer}.</li>
 * <li>(Optional) Do some operation after. Example, make a db request to retrieve node container ids, as we often need to lock them with nodes</li>
 * </ol>
 * </code>
 * @author Julien Thebault
 * @since 1.13
 *
 */
public interface IdsCoercerExtender {

	/**
	 * Do some additional stuff to lock more or less entities, as needed by model and business logic.
	 * If a database request is needed, a fresh transaction should be opened and committed inside this method call, 
	 * as the aspect addressing concurrency concern is executed outside of the main business transaction.
	 * Example : {@link CoercerPathEdgeExtender#doCoerce(Collection)}
	 * @param coercedIds
	 * @return the completed id list
	 */
	public Collection<? extends Serializable> doCoerce (Collection<? extends Serializable> coercedIds);
}
