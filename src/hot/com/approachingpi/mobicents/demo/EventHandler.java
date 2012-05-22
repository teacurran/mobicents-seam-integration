package com.approachingpi.mobicents.demo;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.persistence.*;
import javax.ejb.ObjectNotFoundException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;
import org.mobicents.servlet.sip.seam.media.framework.IVRHelper;
import org.mobicents.servlet.sip.seam.media.framework.MediaEventDispatcher;
import org.mobicents.servlet.sip.seam.media.framework.MediaSessionStore;
import com.approachingpi.mobicents.demo.model.PhoneCall;
import com.approachingpi.mobicents.demo.model.Session;
import com.approachingpi.mobicents.demo.action.SessionHome;

@Name("eventHandler")
@Scope(ScopeType.STATELESS)
@Transactional
public class EventHandler {
	
	@Logger
	Log log;
	@In
	MediaController mediaController;
	@In
	SipSession sipSession;
	@In
	MediaSessionStore mediaSessionStore;
	@In
	IVRHelper ivrHelper;
	@In
	MediaEventDispatcher mediaEventDispatcher;


	@In
	protected EntityManager entityManager;

	//@In EntityManagerFactory entityManagerFactory;

	//@In(scope = ScopeType.APPLICATION, required = false)
	//@Out(scope = ScopeType.APPLICATION, required = false)
	//String conferenceEndpointName;

	// hard coded for the lazy
	private final String soundBin = "file:///Users/tea/dev/ApproachingPi/mobicents/mobicentsdemo/view/sounds/";
	private final String announcement = soundBin + "fourdigit.wav";

	@Observer("INVITE")
	public void doInvite(SipServletRequest request) throws Exception {
		log.debug("doInvite");

		// Extract SDP from the SIp message
		String sdp = new String((byte[]) request.getContent());

		// Tell the other side to ring (status 180)
		request.createResponse(SipServletResponse.SC_RINGING).send();

		// Store the INVITE request in the sip session
		sipSession.setAttribute("inviteRequest", request);

		// If this is the first INVITE in the app, then we must start a new conference
		//if (conferenceEndpointName == null)
		//	conferenceEndpointName = "media/trunk/Announcement/$";

		// Create a connection between the UA and the conference endpoint
		mediaController.createConnection("media/trunk/Conference/$").modify("$", sdp);

		//mediaController.createConnection("media/trunk/Announcement/$").modify("$", sdp);
		// also updates the SDP in Media Server to match capabilities of UA

		//EntityManagerFactory emf = Persistence.createEntityManagerFactory("mobicentsdemo");
		//entityManager = emf.createEntityManager();
		
	}

	@Observer("storeConnectionOpen")
	public void doConnectionOpen(MsConnectionEvent event) throws IOException {
		log.debug("doConnectionOpen");

		// Save this connection where the framework can read it
		// mediaSessionStore.setMsConnection(event.getConnection());// This is done automatically in STF 2.0

		// The conference endpoint is now assiged after we are connected, so save it too
		//conferenceEndpointName = event.getConnection().getEndpoint().getLocalName();

		// Recall the INVITE request that we saved in doInvite
		SipServletRequest request = (SipServletRequest) sipSession.getAttribute("inviteRequest");

		// Make OK (status 200) to tell the other side that the call is established
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);

		// Put the SDP inside the OK message to tell what codecs and so on we agree with
		response.setContent(event.getConnection().getLocalDescriptor(), "application/sdp");

		try {
			//EntityManagerFactory emf = Persistence.createEntityManagerFactory("mobicentsdemo");
			//EntityManager em = emf.createEntityManager();
			//EntityTransaction et = em.getTransaction();
			//
			//et.begin();

			//EntityManager em = entityManagerFactory.createEntityManager();

			PhoneCall call = new PhoneCall();
			call.setDateStart(new Date());
			call.setFromAddress(request.getFrom().toString());
			call.setToAddress(request.getTo().toString());
			entityManager.persist(call);
			sipSession.setAttribute("phoneCall", call);
			entityManager.flush();

			// refresh to get the id
			//em.refresh(call);
			
			//et.commit();
			//em.close();
			//emf.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Now actually send the message
		response.send();

		// And start listening for DTMF signals
		//ivrHelper.detectDtmf();
		ivrHelper.playAnnouncementWithDtmf(announcement);

	}

	@Observer("DTMF")
	public void dtmf(String button) {
		log.debug("dtmf");

		try {
			//EntityManagerFactory emf = Persistence.createEntityManagerFactory("mobicentsdemo");
			//EntityManager em = emf.createEntityManager();
			//EntityTransaction et = em.getTransaction();
			//
			//et.begin();

			//EntityManager em = entityManagerFactory.createEntityManager();

			String codeEntered = (String)sipSession.getAttribute("codeEntered");
			if (codeEntered == null) {
				codeEntered = button;
			} else {
				codeEntered += button;
			}

			// wait until we get 4 digits entered
			if (codeEntered != null && codeEntered.length() == 4) {
				log.debug("CODE ENTERED:#0", codeEntered);

				int sessionCode = 0;
				try {
					sessionCode = Integer.parseInt(codeEntered);
				} catch (Exception e2) {
					// do nothing;
				}
				log.debug("sessionCode:#0", sessionCode);

				Query query = entityManager.createQuery("SELECT S FROM Session S WHERE S.id = :sessionCode AND S.dateCreated IS NOT NULL");
				query.setParameter("sessionCode", sessionCode);
				Session session = null;
				try {
					session = (Session)query.getSingleResult();
				} catch (NoResultException nre) {
					// do nothing
				}
				if (session == null) {
					log.debug("code not found");
					ivrHelper.playAnnouncementWithDtmf(soundBin + "codenotfoundtryagain.wav");
				} else {
					log.debug("code validated");
					ivrHelper.playAnnouncementWithDtmf(soundBin + "codevalidatedgoodbye.wav");

					// update the database
					PhoneCall callDetached = (PhoneCall)sipSession.getAttribute("phoneCall");
					if (callDetached != null) {
						PhoneCall call = (PhoneCall)entityManager.find(PhoneCall.class, callDetached.getId());
						session.setCall(call);
					}
					session.setDateValidated(new Date());
					entityManager.persist(session);
					entityManager.flush();
		            //sipSession.createRequest("BYE").send();
				}

				codeEntered = "";
			}
			log.debug("here 8");

			sipSession.setAttribute("codeEntered", codeEntered);

			//et.commit();
			//em.close();
			//emf.close();

		} catch (Exception e) {
			log.error("dtmf error", e);
		}

		// If the other side presses the button "0" stop the playback
		//if ("0".equals(button)) {
		//	ivrHelper.endAll();
		//} else {
		//	// otherwise play announcement
		//	ivrHelper.playAnnouncementWithDtmf(announcement);
		//}
		// Also log the DTMF buttons pressed so far in this session
		
		//log.info("Current DTMF Stack for the SIP Session: " + mediaEventDispatcher.getDtmfArchive(sipSession));
	}

	@Observer({"BYE"})
	public void doBye(SipServletRequest request) throws Exception {
		log.debug("doBye");

		try {
			//EntityManagerFactory emf = Persistence.createEntityManagerFactory("mobicentsdemo");
			//EntityManager em = emf.createEntityManager();
			//EntityTransaction et = em.getTransaction();

			//et.begin();

			//EntityManager em = entityManagerFactory.createEntityManager();

			PhoneCall callDetached = (PhoneCall)sipSession.getAttribute("phoneCall");
			if (callDetached != null) {
				PhoneCall call = (PhoneCall)entityManager.find(PhoneCall.class, callDetached.getId());
				//em.refresh(call);
				if (call != null) {
					call.setDateEnd(new Date());
					entityManager.persist(call);
					entityManager.flush();
				}
			}
			
			//et.commit();

			//entityManager.close();
			//emf.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		request.createResponse(200).send();

		// And clean up the connections
		MsConnection connection = mediaSessionStore.getMsConnection();
		connection.release();
	}

	@Observer("REGISTER")
	public void doRegister(SipServletRequest request) throws Exception {
		// allow any number to register, not used.
		request.createResponse(200).send();
	}
}
