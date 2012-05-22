package org.mobicents.servlet.sip.seam.media.framework;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.events.MsEventIdentifier;
import org.mobicents.mscontrol.events.dtmf.MsDtmfNotifyEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.mscontrol.events.pkg.MsAudio;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaEvent;

/**
 * This class keeps track of some Media events and produces other events for convenience.
 * It also keeps track on the DTMF numbers entered in the context of every SipSession.
 * 
 * IMPORTANT NOTE FOR REVIEW!
 * The reason this class cannot be Application scoped (and avoid the static dtmfBuffer collection)
 * is that when an event is raised in the context of one SipSession Seam flags the sipSession member
 * in this class as "injected" in Bijection interceptor and it stays like that or the whole 
 * application without ever being updated. This seems wrong as we may want to keep state in finer-grained
 * contexts.
 * 
 * @author vralev
 *
 */

@Name("mediaEventDispatcher")
@Scope(ScopeType.STATELESS)
@Install(precedence = FRAMEWORK)
@AutoCreate
public class MediaEventDispatcher {
	@In(required = false)
	MediaSessionStore mediaSessionStore;
	@Logger
	Log log;
	@In(required = false)
	SipSession sipSession;
	private static ConcurrentHashMap<Object, StringBuffer> dtmfBuffer = new ConcurrentHashMap<Object, StringBuffer>();

	private void addNumber(Object object, String number) {
		if (dtmfBuffer.get(object) == null) {
			dtmfBuffer.put(object, new StringBuffer());
		}
		dtmfBuffer.get(object).append(number);
	}

	/**
	 * Reset the DTMF buffer for give object (SipSession, MsLink or MsConnection)
	 * 
	 * @param object
	 */
	public void reset(Object object) {
		dtmfBuffer.put(object, new StringBuffer());
	}

	/**
	 * Get the DTMF buffer for a given object (SipSession, MsLink or MsConnection)
	 * 
	 * @param object
	 * @return Returns null for unknown objects.
	 */
	public String getDtmfArchive(Object object) {
		return dtmfBuffer.get(object).toString();
	}

	public String getDtmfArchive(Object object, int digits) {
		String stringDigits = getDtmfArchive(object);
		int numDigits = stringDigits.length();
		return stringDigits.substring(numDigits - digits, numDigits);
	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 * @param mediaEvent
	 */
	@Observer("mediaEvent")
	public void doMediaEvent(MediaEvent mediaEvent) {
		MsEventIdentifier identifier = mediaEvent.getMsNotifyEvent()
				.getEventID();
		if (identifier.equals(DTMF.TONE)) {
			MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) mediaEvent
					.getMsNotifyEvent();
			String signal = event.getSequence();
			addNumber(mediaEvent.getLink(), signal);
			addNumber(mediaEvent.getEndpoint(), signal);
			addNumber(mediaEvent.getSipSession(), signal);
			Events.instance().raiseEvent("DTMF", signal);
		} else if (identifier.equals(MsAnnouncement.COMPLETED)) {
			Events.instance().raiseEvent("announcementComplete");
		} else if (identifier.equals(MsAnnouncement.FAILED)) {
			Events.instance().raiseEvent("announcementFailed");
		} else if (identifier.equals(MsAudio.FAILED)) {
			Events.instance().raiseEvent("audioFailed");
		}

	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 */
	@Observer("preLinkConnected")
	public void doLinkConnected(MsLinkEvent linkEvent) {
		mediaSessionStore.setMsLink(linkEvent.getSource());
		MsEndpoint from = linkEvent.getSource().getEndpoints()[0];
		MsEndpoint to = linkEvent.getSource().getEndpoints()[1];
		MsEndpoint prEndpoint = (MsEndpoint) sipSession
				.getAttribute("org.mobicents.servlet.sip.seam.media.prEndpoint");
		MsEndpoint terminatingEndpoint = from;
		if (from.getLocalName().equals(prEndpoint.getLocalName())) {
			terminatingEndpoint = to;
		}
		mediaSessionStore.setMsEndpoint(terminatingEndpoint);
		Events.instance().raiseEvent("linkConnected", linkEvent);
		Events.instance().raiseEvent("storeLinkConnected", linkEvent);
	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 */
	@Observer("connectionOpen")
	public void doConnectionOpen(MsConnectionEvent connectionEvent) {
		mediaSessionStore.setMsConnection(connectionEvent.getConnection());
		mediaSessionStore.setMsEndpoint(connectionEvent.getConnection()
				.getEndpoint());
		Events.instance().raiseEvent("storeConnectionOpen", connectionEvent);
	}

	@Observer("preConnectionHalfOpen")
	public void doConnectionHalfOpen(MsConnectionEvent connectionEvent) {
		log.info("PRECONNECTION for " + sipSession.toString());
		mediaSessionStore.setMsConnection(connectionEvent.getConnection());
		mediaSessionStore.setMsEndpoint(connectionEvent.getConnection()
				.getEndpoint());
		sipSession.setAttribute(
				"org.mobicents.servlet.sip.seam.media.prEndpoint",
				connectionEvent.getConnection().getEndpoint());
		Events.instance().raiseEvent("connectionHalfOpen", connectionEvent);
	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 */
	@Observer("sipSessionDestroyed")
	public void doSipSessionDestroyed(SipSession sipSession) {
		dtmfBuffer.remove(sipSession);
	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 */
	@Observer("linkDisconnected")
	public void doLinkDisconnected(MsLinkEvent linkEvent) {
		dtmfBuffer.remove(linkEvent.getSource());
	}

	/**
	 * You can use this method to simulate some event, BUT THIS IS NOT RECOMMENDED!
	 */
	@Observer("connectionDisconnected")
	public void doConnectionDisconnected(MsConnectionEvent connectionEvent) {
		dtmfBuffer.remove(connectionEvent.getConnection());
	}

}
