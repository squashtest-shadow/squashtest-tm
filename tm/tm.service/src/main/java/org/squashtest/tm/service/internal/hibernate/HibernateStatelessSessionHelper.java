package org.squashtest.tm.service.internal.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * Helper class used to factorise the unwrapping of HibernateSessionFactory and
 * the creation of a stateless session.
 * Created by jthebault on 11/04/2016.
 */
@Component
public class HibernateStatelessSessionHelper {

	@Inject
	EntityManagerFactory entityManagerFactory;

	/**
	 * Create a new Hibernate stateless session, by unwrapping the HibernateSessionFactory from EntityManagerFactory and return the session.
	 * Don't forget to close it after usage.
	 * @return
     */
	public StatelessSession openStatelessSession(){
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		return sessionFactory.openStatelessSession();
	}
}
