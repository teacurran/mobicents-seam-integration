package com.approachingpi.mobicents.demo.action;

import com.approachingpi.mobicents.demo.model.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("sessionHome")
public class SessionHome extends EntityHome<Session> {

	@In(create = true)
	CallHome callHome;

	public void setSessionSession(Integer id) {
		setId(id);
	}

	public Integer getSessionSession() {
		return (Integer) getId();
	}

	@Override
	protected Session createInstance() {
		Session session = new Session();
		return session;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		PhoneCall call = callHome.getDefinedInstance();
		if (call != null) {
			getInstance().setCall(call);
		}
	}

	public boolean isWired() {
		return true;
	}

	public Session getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
