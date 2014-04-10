package com.android.volley.toolbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache.Entry;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardData.HTTP_SOURCE;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class GZipRequest extends Request<CardResponseData> {
	public static final String TAG = "GZipRequest";
	private boolean mShowLogs = false;
	public int mStartIndex = 1;
	
	 private final Listener<CardResponseData> mListener;
	  /**
	     * Creates a new request with the given method.
	     *
	     * @param method the request {@link Method} to use
	     * @param url URL to fetch the string at
	     * @param listener Listener to receive the String response
	     * @param errorListener Error listener, or null to ignore errors
	     */
	    public GZipRequest(int method, String url, Listener<CardResponseData> listener,
	            ErrorListener errorListener) {
	        super(method, url, errorListener);
	        mListener = listener;
	    }

	    /**
	     * Creates a new GET request.
	     *
	     * @param url URL to fetch the string at
	     * @param listener Listener to receive the String response
	     * @param errorListener Error listener, or null to ignore errors
	     */
	    public GZipRequest(String url, Listener<CardResponseData> listener, ErrorListener errorListener) {
	        this(Method.GET, url, listener, errorListener);
	    }

	    @Override
	    protected void deliverResponse(CardResponseData response) {
	        mListener.onResponse(response);
	    }
		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> params = new HashMap<String, String>();
	        params.put("Accept-Encoding", "gzip, deflate");
	        params.put("clientKey",ConsumerApi.DEBUGCLIENTKEY);
	        return params;
		}
		public void printLogs(boolean value){
			mShowLogs = value;
		}
	    @Override
	    protected Response<CardResponseData> parseNetworkResponse(NetworkResponse response) {
	    	if(mShowLogs && response != null && response.headers != null ){
				Log.d(TAG,"Response Headers");
				for(String key:response.headers.keySet()){
					Log.d(TAG,key+":"+response.headers.get(key));
				}
			}
	        String parsed = new String();
	        try {
	        	boolean gzipped = true;
	        	if(response != null && response.headers != null && response.headers.size() >0)
	        	{
		        	String encoding = response.headers.get("Content-Encoding");
		        	gzipped = encoding!=null && encoding.toLowerCase().contains("gzip");
	        	}
	        	
	        	if(gzipped)
	        	{
		        	InputStream responseStream = new ByteArrayInputStream(response.data); 
		        	final int BUF_SIZE_MAX = 20480;
		        	byte[] buf = new byte[BUF_SIZE_MAX];
		        	GZIPInputStream zis;
		        	zis = new GZIPInputStream(responseStream, BUF_SIZE_MAX);
		        	boolean stop = false;
		        	int bytesRead = 0;
					while (stop == false) {
						int pos = 0;
						do {
							bytesRead = zis.read(buf, pos, (BUF_SIZE_MAX - pos));
							if (bytesRead == -1) {
								stop = true;
								break;
							} else {
								pos += bytesRead;
							}
						} while (pos < BUF_SIZE_MAX);
						parsed +=new String(buf, "UTF-8");
					}
					zis.close();
	        	}
	        	else
	        	{
	        		Log.e(TAG,"Response is not gzip");
	        		parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
	        	}
	        } catch (UnsupportedEncodingException e) {
	            parsed = new String(response.data);
	        }catch (IOException e) {
	        	parsed = new String(response.data);
			}
	        if(mShowLogs){
	        	Log.d(TAG,"Response :: " +parsed);
	        }
	        
	        CardResponseData minResultSet=null;
	        try {
	        	Log.d("volley", "CardResponseData received");
	        	minResultSet =(CardResponseData) Util.fromJson(parsed, CardResponseData.class);	       
	        	minResultSet.responseHeaders = response.headers;	  
	        	minResultSet.mStartIndex = mStartIndex;
	        	if(minResultSet == null || minResultSet.code != 200 || minResultSet.results == null || minResultSet.results.isEmpty()){
	        		// remove cache entry if response is is not valid
	        		return Response.success(minResultSet, null);
	        	}
	        	if(response.headers != null && response.headers.containsKey(ConsumerApi.HEADER_RESPONSE_HTTP_SOURCE)){
	        		for (CardData cardData : minResultSet.results) {
						cardData.httpSource=HTTP_SOURCE.valueOf(response.headers.get(ConsumerApi.HEADER_RESPONSE_HTTP_SOURCE));
					}
	        	}
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        return Response.success(minResultSet, HttpHeaderParser.parseCacheHeaders(response));
	    }
}
