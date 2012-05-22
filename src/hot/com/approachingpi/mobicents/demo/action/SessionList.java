package com.approachingpi.mobicents.demo.action;

import com.approachingpi.mobicents.demo.model.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("sessionList")
public class SessionList extends EntityQuery<Session> {

	private static final String EJBQL = "select session from Session session";

	private static final String[] RESTRICTIONS = {};

	private Session session = new Session();

	public SessionList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public Session getSession() {
		return session;
	}
}
