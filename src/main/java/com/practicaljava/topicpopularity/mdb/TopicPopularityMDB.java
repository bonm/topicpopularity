package com.practicaljava.topicpopularity.mdb;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.MessageDriven;
import javax.ejb.Schedule;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.practicaljava.topicpopularity.annotations.TWJMSMessage;


@MessageDriven(mappedName = "jms/twQueue")
public class TopicPopularityMDB implements MessageListener {
	private ArrayList<String> tweets;
	private ArrayList<String> tw;

    @Inject
    @TWJMSMessage
    Event<ArrayList<String>> jmsEvent;
	
	TopicPopularityMDB() {
		tweets = new ArrayList<>(30);
	}

    @Override
    public void onMessage(Message msg) {
    	try {
			populateTweetsArray(msg.getBody(String.class));
		} catch (JMSException e) {
			Logger.getLogger(TopicPopularityMDB.class.getName()).log(Level.SEVERE, e.getMessage());
		}  	
    }
    
	@Schedule(hour = "*", minute = "*", second = "*")
	private void sendResponse() {
		ArrayList<String> twitterData = getTweetsData();
		if (!twitterData.isEmpty()) {
			jmsEvent.fire(twitterData);
		}
	}
	
	private void populateTweetsArray(String message) {
		tweets.add(message);
	}

	public ArrayList<String> getTweetsData() {
		tw = new ArrayList<>();
		List<String> tmpList = new ArrayList<>(tweets);
		tweets.clear();
		tw.addAll(tmpList);
		
		return tw;
	}
}