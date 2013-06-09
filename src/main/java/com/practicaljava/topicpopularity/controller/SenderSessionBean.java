package com.practicaljava.topicpopularity.controller;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;


@Stateless
public class SenderSessionBean {

    @Resource(mappedName = "jms/twQueue")
    private Queue queue;
    @Inject
    private JMSContext jmsContext;

    public void sendMessage(String message) {
    	jmsContext.createProducer().send(queue, message);
    }
}
