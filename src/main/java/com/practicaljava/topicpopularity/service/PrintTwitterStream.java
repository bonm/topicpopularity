package com.practicaljava.topicpopularity.service;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.practicaljava.topicpopularity.controller.SenderSessionBean;

public class PrintTwitterStream {

	private SenderSessionBean senderBean;
	private TwitterStream twitterStream;
	
	@Inject
	public PrintTwitterStream(SenderSessionBean sb) {
		this.senderBean = sb;
	}
	
	private void init() {
		twitterStream = new TwitterStreamFactory().getInstance();
	}
	
	public void tearDown() throws Exception {
		twitterStream.shutdown();
	}
	
	public void print(String toTrack) throws TwitterException {
		StatusListener listener = new StatusListener() {
			
			@Override
            public void onStatus(Status status) {
                if (senderBean != null) {
                	senderBean.sendMessage(status.getText());
                }
            }

			@Override
			public void onException(Exception e) {
				Logger.getLogger(PrintTwitterStream.class.getName()).log(Level.SEVERE, e.getMessage());
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice e) {}

			@Override
			public void onScrubGeo(long arg0, long arg1) {}

			@Override
			public void onStallWarning(StallWarning warning) {
				Logger.getLogger(PrintTwitterStream.class.getName()).log(Level.SEVERE, warning.getMessage());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				Logger.getLogger(PrintTwitterStream.class.getName()).log(Level.SEVERE, "Got track limitation notice:" + numberOfLimitedStatuses);
			}
		};
		
		init();
		twitterStream.addListener(listener);
		
		ArrayList<String> track = new ArrayList<String>();
		track.add(toTrack);
		
		long[] followArray = new long[0];
		String[] trackArray = track.toArray(new String[track.size()]);
		
		twitterStream.filter(new FilterQuery(0, followArray, trackArray));
		
	}
}
