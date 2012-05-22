package org.mobicents.servlet.sip.seam.entrypoint.media;

import java.util.HashMap;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsNotificationListener;

/**
 * We need separate class to hold the static objects that is not under Seam management, because 
 * javassist creates another class with another static instance and causes confusion.
 * 
 * Static objects in this case are safe.
 * 
 * @author vralev
 *
 */
public class MediaControllerManager {
	public HashMap<Object, MsNotificationListener> listenerMap = new HashMap<Object, MsNotificationListener>();

	private HashMap<SipSession, MediaController> mediaControllers = new HashMap<SipSession, MediaController>();

	public MediaController getMediaController(SipSession sipSession) {
		MediaController mc = mediaControllers.get(sipSession);
		if (mc == null) {
			mc = new MediaController(sipSession, MsProviderContainer.msProvider
					.createSession());
			putMediaController(sipSession, mc);
		}
		return mc;
	}

	public void putMediaController(SipSession sipSession,
			MediaController mediaController) {
		mediaControllers.put(sipSession, mediaController);
	}

	public void removeMediaController(SipSession sipSession) {
		mediaControllers.remove(sipSession);
	}

	private static MediaControllerManager mediaControllerHolder;

	public synchronized static MediaControllerManager instance() {
		if (mediaControllerHolder == null) {
			mediaControllerHolder = new MediaControllerManager();
		}
		return mediaControllerHolder;
	}
}
