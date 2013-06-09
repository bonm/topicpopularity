package com.practicaljava.topicpopularity.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringEscapeUtils;

import twitter4j.TwitterException;

import com.google.gson.Gson;
import com.practicaljava.topicpopularity.annotations.TWJMSMessage;
import com.practicaljava.topicpopularity.service.PrintTwitterStream;


@ServerEndpoint("/twstream")
public class EndpointController implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PrintTwitterStream twitterStream;
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    Gson gson;
    
    @Inject
    ClientSessionBean clientSessionBean;
        
    @Inject
    public EndpointController(SenderSessionBean sb) {
    	this.gson = new Gson();
    	this.twitterStream = new PrintTwitterStream(sb);
    }
    
    
    @OnOpen
    public void onOpen(final Session session) {
        try {
        	sessions.add(session);
        	setSessionIfNull(session);
        	
        } catch (Exception e) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session client) {
    	setSessionIfNull(client);
    	String input = StringEscapeUtils.escapeXml(message);
    	
    	if (client.getId() == clientSessionBean.getSessionId()) {
    		if (input.equals("stop")) {
	    		closeStream(client);
			}
			else {
				try {
					twitterStream.print(input);
				} catch (TwitterException e) {
					Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
				}
			}
    	}
    }

    @OnClose
    public void onClose(final Session session) {
        try {
            sessions.remove(session);
            closeStream(session);
        } catch (Exception e) {
            Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }
    

    public void onJMSMessage(@Observes @TWJMSMessage ArrayList<String> tweetData) {
        String json = gson.toJson(tweetData);
        if (clientSessionBean.getSessionId() != null) {
	        try {
        		for (Session s : sessions) {
            		if (s.getId() == clientSessionBean.getSessionId()) {
            			s.getBasicRemote().sendText(json);
            		}
            		else {
            			s.getBasicRemote().sendText(gson.toJson("stream is already in use"));
            		}
            		
    			}
			} catch (IOException e) {
				Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
			}
        }
    }
    
    private void closeStream(Session session) {
    	if (session.getId() == clientSessionBean.getSessionId()) {
	    	for (Session s : sessions) {
	    		if (s.getId() != clientSessionBean.getSessionId()) {
	    			try {
						s.getBasicRemote().sendText(gson.toJson("stream is ready"));
					} catch (IOException e) {
						Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
					}
	    		}
	    	} 
	    	clientSessionBean.setSessionId(null);
	    	
	    	try {
				twitterStream.tearDown();
			} catch (Exception e) {
				Logger.getLogger(EndpointController.class.getName()).log(Level.SEVERE, e.getMessage());
			}
    	}
    }
    
    private void setSessionIfNull(Session session) {
    	if (clientSessionBean.getSessionId() == null) {
    		clientSessionBean.setSessionId(session.getId());
    	}
    }
}
