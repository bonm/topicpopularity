package com.practicaljava.topicpopularity.controller;

import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
public class ClientSessionBean {
	private String sessionId;
	
	@Lock(LockType.READ)
	public String getSessionId() {
		return sessionId;
	}
	
	@Lock(LockType.WRITE)
	@AccessTimeout(value=360000)
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
