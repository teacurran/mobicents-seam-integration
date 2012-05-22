package com.approachingpi.mobicents.demo.action;

import com.approachingpi.mobicents.demo.model.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("callHome")
public class CallHome extends EntityHome<PhoneCall> {

	public void setCallId(Integer id) {
		setId(id);
	}

	public Integer getCallId() {
		return (Integer) getId();
	}

	@Override
	protected PhoneCall createInstance() {
		PhoneCall call = new PhoneCall();
		return call;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public PhoneCall getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<Session> getSessions() {
		return getInstance() == null ? null : new ArrayList<Session>(
				getInstance().getSessions());
	}

}
