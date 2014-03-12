package com.android.volley.toolbox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.apalya.myplex.utils.EpgContent;
import com.apalya.myplex.utils.EpgParser;
import com.apalya.myplex.utils.EpgResponse;
 
public class EPGRequest extends Request<EpgResponse> {
    
    private final Listener<EpgResponse> listener;
	private EpgParser epgParser;
	private InputStream responseStream;
	
 
  
    public EPGRequest(int method, String url, Listener<EpgResponse> listener, ErrorListener errorListener) {    	
        super(method, url, errorListener);        
        this.listener = listener;
    }    
 
    public EPGRequest( String url, Listener<EpgResponse> listener, ErrorListener errorListener) {    	
        super(Method.GET, url, errorListener);        
        this.listener = listener;
    }  
    
    @Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
        params.put("Accept-Encoding", "epg, deflate");
        return params;
	}
 
    
    @Override
    protected void deliverResponse(EpgResponse response) {
        listener.onResponse(response);
    }
 
    
    @Override
    protected Response<EpgResponse> parseNetworkResponse(NetworkResponse response) 
    {
    	EpgResponse epgResponse;
        try {
            
        	InputStream inputStream = new ByteArrayInputStream( response.data);
//       	    String data = new String(response.data, HttpHeaderParser.parseCharset(response.headers));       	    
//       	    responseStream = new ByteArrayInputStream(response.data);       	
       	    
       	    epgParser = new EpgParser();
       	    List<EpgContent> contents = epgParser.parse(inputStream);
       	    epgResponse = new EpgResponse(contents);
       	    
        } 
        catch (Exception e) {
			return Response.error(new VolleyError(e.getMessage()));
		} 
        return Response.success(epgResponse, HttpHeaderParser.parseCacheHeaders(response));
    }
}