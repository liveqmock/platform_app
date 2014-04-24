package com.apalya.myplex.utils;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.apalya.myplex.LoginActivity;

public class SearchTweets {

	private String queryString;
	private SearchTweetsListener searchTweetsListener;
	private static final String TAG = "SearchTweets";

	static Twitter twitter = null;

	public static interface SearchTweetsListener {
		public void onSearchComplete(List<twitter4j.Status> tweets);
	}

	public SearchTweets(String queryString) {
		this.queryString = queryString;

	}

	public void setSearchTweetsListener(
			SearchTweetsListener searchTweetsListener) {
		this.searchTweetsListener = searchTweetsListener;
	}

	public void search() {
		new SearchTweetsTask().execute();
	}

	private void initTwitter() {

		if (twitter != null)
			return;

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(LoginActivity.TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(LoginActivity.TWITTER_CONSUMER_SECRET);
		builder.setApplicationOnlyAuthEnabled(true);

		twitter = new TwitterFactory(builder.build()).getInstance();

		try {
			OAuth2Token token = twitter.getOAuth2Token();
		} catch (TwitterException e) {
			twitter = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class SearchTweetsTask extends
			AsyncTask<String, Void, List<twitter4j.Status>> {

		private String errmsg;

		public SearchTweetsTask() {

		}

		protected List<twitter4j.Status> doInBackground(String... urls) {
			try {

				long startTime = System.currentTimeMillis();

				initTwitter();

				if (twitter == null) {
					return null;
				}
				
				Query query = new Query(queryString);
				query.setCount(20);
				query.lang("en");
				QueryResult result;
				// do {
				result = twitter.search(query);
				List<twitter4j.Status> tweets = result.getTweets();
				for (twitter4j.Status tweet : tweets) {
					Log.i(TAG, "@" + tweet.getUser().getScreenName() + " - "
							+ tweet.getText());
				}
				// } while ((query = result.nextQuery()) != null);
				Log.i(TAG,
						"time to complete search: "
								+ (System.currentTimeMillis() - startTime)
								+ " milliseconds");

				return tweets;

			} catch (TwitterException e) {
				errmsg = e.getMessage();
				Log.e("Twitter Login Error", "> " + errmsg);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> tweets) {

			if (tweets != null && !tweets.isEmpty()
					&& searchTweetsListener != null) {
				searchTweetsListener.onSearchComplete(tweets);
			}
		}

	}

}
