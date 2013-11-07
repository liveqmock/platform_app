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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public class GZipRequest extends Request<String> {
	public static final String TAG = "GZipRequest";
	private boolean mShowLogs = false;
	 private final Listener<String> mListener;
	  /**
	     * Creates a new request with the given method.
	     *
	     * @param method the request {@link Method} to use
	     * @param url URL to fetch the string at
	     * @param listener Listener to receive the String response
	     * @param errorListener Error listener, or null to ignore errors
	     */
	    public GZipRequest(int method, String url, Listener<String> listener,
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
	    public GZipRequest(String url, Listener<String> listener, ErrorListener errorListener) {
	        this(Method.GET, url, listener, errorListener);
	    }

	    @Override
	    protected void deliverResponse(String response) {
	        mListener.onResponse(response);
	    }
		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> params = new HashMap<String, String>();
	        params.put("Accept-Encoding", "gzip, deflate");
	        return params;
		}
		public void printLogs(boolean value){
			mShowLogs = value;
		}
	    @Override
	    protected Response<String> parseNetworkResponse(NetworkResponse response) {
	    	if(mShowLogs && response != null && response.headers != null ){
				Log.d(TAG,"Response Headers");
				for(String key:response.headers.keySet()){
					Log.d(TAG,key+":"+response.headers.get(key));
				}
			}
	        String parsed = new String();
	        try {
	        	InputStream responseStream = new ByteArrayInputStream(response.data); 
	        	final int BUF_SIZE_MAX = 20480;
	        	byte[] buf = new byte[BUF_SIZE_MAX];
	        	GZIPInputStream zis;
	        	zis = new GZIPInputStream(responseStream, BUF_SIZE_MAX);
	            while(zis.read(buf )>0){
	            	parsed +=new String(buf, "UTF-8");
	            }
				zis.close();
	        } catch (UnsupportedEncodingException e) {
	            parsed = new String(response.data);
	        }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if(mShowLogs){
	        	Log.d(TAG,"Response :: " +parsed);
	        }
	        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
	    }
}
