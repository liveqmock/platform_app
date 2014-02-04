package com.apalya.myplex.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.flurry.android.monolithic.sdk.impl.pa;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * This class is a singleton class for accessing Location as well as city name
 * <br>The sample call will be like <br> <b>
 * LocationUtil  lUtil =   LocationUtil.getInstance();</b> <br>
 * for getting Location <br> <b>lUtil.getLocation();</b>
 * <p>Don't forget to close the connection. close the connection by  lUtil.close();
 */
public class LocationUtil 
{
	private LocationClient client;
	private Location location = null;
	private Context context;	
	List<Address> addresses = null;
	private static LocationUtil locationInstance;
	String params = "";
	
	private LocationUtil(Context context) 
	{
		this.context =  context;
		if(isLocationEnabled(context)){
			client =  new LocationClient(context,new ConnectionCallbacks(),new ConnectionFailedCallBack());
			client.connect();
		}else{
			// Location service is not enabled. 
//			Util.showToast("Location access is not enabled. Please enable it.", context);
		}
	}

	public static LocationUtil getInstance(Context context) {
		if(locationInstance==null){
			locationInstance = new LocationUtil(context);
		}
		return locationInstance;
	}
	public void init(){
		if(params.equals("")|| params.length()==0){
			getAddressParams();
		}
//		close();
	}
	public String getVideoUrlParams(){
		return params;
	}
	
	/**
	 * 
	 * @return the String as params  and <b>""(Empty String or in case of )</b> if location service is not enabled. 
	 */
	public String  getAddressParams(){
		if(!isLocationEnabled(context)){
			return params;
		}
		getLocation();
		if(location!=null){
			try {
				new GetAddressTask().execute(new Location[]{location});				
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(params.equals(""))
			{
				new GetAddressTask().execute(new Location[]{location});	
			}
		}
		return params;
	}
	private  Location getLocation() 
	{
		if(client!=null){
			if(client.isConnected())
				location = client.getLastLocation();
		}
		return location;
	}
	
	/**
	 *  Disconnect and close the connection for fetching the location. To be called when you are no more interested to fetch the location.
	 *  If you are not calling this method the location will be fetched although you are not using it. 
	 */
	public void close() 
	{
		if(client!=null)
			client.disconnect();
	}
	
	
	class ConnectionCallbacks implements GooglePlayServicesClient.ConnectionCallbacks {		
		@Override
		public void onDisconnected() {
		}		
		@Override
		public void onConnected(Bundle arg0) {
			location = client.getLastLocation();
			init();
			client.disconnect();
		}
	};
	class ConnectionFailedCallBack implements GooglePlayServicesClient.OnConnectionFailedListener{
		@Override
		public void onConnectionFailed(ConnectionResult cResult) {	
			/**
			 * There is a connection error.so we have to handle the connection problems here.
			 */
		}	
	}
	

	private class GetAddressTask extends AsyncTask<Location, Void, String> {		
	
			@Override
			protected String doInBackground(Location... param) {
				Geocoder geocoder =
						new Geocoder(context, Locale.getDefault());
				Location loc = param[0];
				
				try {
					
					addresses = geocoder.getFromLocation(loc.getLatitude(),
							loc.getLongitude(), 1);
					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);					
							if(address.getCountryCode()!=null)
								params += "&country="+address.getCountryCode();
							if(address.getPostalCode()!=null)
								params += "&postalCode="+address.getPostalCode();
							if(address.getLocality()!=null)
								params += "&area="+address.getLocality();
						}
				} catch (IOException e1) {
					Log.e("LocationSampleActivity",
							"IO Exception in getFromLocation()");
					e1.printStackTrace();
					return ("IO Exception trying to get address");
				} catch (IllegalArgumentException e2) {
					// Error message to post in the log
					String errorString = "Illegal arguments " +
							Double.toString(loc.getLatitude()) +
							" , " +
							Double.toString(loc.getLongitude()) +
							" passed to address service";
					Log.e("LocationSampleActivity", errorString);
					e2.printStackTrace();
					return errorString;
				}catch (NullPointerException e) {
					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}	
			params = params.replaceAll(" ", "%20");
			return params;
				
			}	

		}
	private boolean isLocationEnabled(Context context){
		boolean isLocationEnabled = false;
		LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		/*if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			isLocationEnabled = true;
		}else */if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			isLocationEnabled  =  true;
		}
		return isLocationEnabled;
	}
		
}
