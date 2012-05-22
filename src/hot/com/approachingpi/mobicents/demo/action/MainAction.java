package com.approachingpi.mobicents.demo.action;

import org.jboss.seam.annotations.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.log.Log;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.approachingpi.mobicents.demo.model.Session;
import com.approachingpi.mobicents.demo.model.PhoneCall;

import java.util.Date;
import java.util.Random;

@Name("main")
@Scope(ScopeType.CONVERSATION)
public class MainAction {

	@Logger
	Log log;
	
	Session session;

	@In
	EntityManager entityManager;

	@Begin
	public void begin() {
		log.debug("begin()");

	}

	public Session getSession() {
		log.debug("getSession()");

		//EntityManagerFactory emf = Persistence.createEntityManagerFactory("mobicentsdemoLocal");
		//EntityManager em = emf.createEntityManager();
		//EntityTransaction et = entityManager.getTransaction();
		//et.begin();

		if (session == null) {

			// count all the available codes
			Long count = (Long)entityManager.createQuery("SELECT COUNT(S.id) FROM Session S WHERE S.dateCreated IS NULL").getSingleResult();
			Query query = entityManager.createQuery("SELECT S FROM Session S WHERE S.dateCreated IS NULL");
			// select a random code
			query.setFirstResult(new Random().nextInt(count.intValue()));
			query.setMaxResults(1);
			session = (Session)query.getSingleResult();
			session.setDateCreated(new Date());
			entityManager.persist(session);
			entityManager.flush();
		} else {
			return session;
		}

		PhoneCall call = session.getCall();

		//et.commit();
		//em.close();
		//emf.close();

		return session;
	}

	public void refreshSession() {
		log.debug("refreshSession()");
		if (session != null) {
			session = entityManager.find(Session.class, session.getId());
			entityManager.refresh(session);
		}
	}

	public void resetSession() {
		session = null;
	}
}
