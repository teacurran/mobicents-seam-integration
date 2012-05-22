package com.approachingpi.mobicents.demo.action;

import javax.ejb.Local;

@Local
public interface Authenticator {

	boolean authenticate();

}
