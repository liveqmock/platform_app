package com.apalya.myplex.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile implements Serializable{
	private static final long serialVersionUID = 1L;
	private static String useremail="NA";
	private static String userid;
	private static String facebookid;
	private static String googleid;
	private static String profilepic;
	private static String profileDesc="NA";
	private static String name="NA";
	private static boolean loggedInStatus;
	public String joinedDate;
	public String phonenumber="NA";
	public String location="NA";
	public String lastVisitedDate="NA";
	public boolean firstVisitStatus;
	private String userProfile;
	public String likes="NA";
	public List<CardData> lastVisitedCardData=new ArrayList<CardData>();
	public Map<String, Long> downloadMap = new HashMap<String, Long>();
	
	public String getUserProfile(){
		
		
		
		userProfile="User Email: "+useremail+"\n\n"+
				    "Joined on:"+joinedDate+"\n\n"+
				    "Last Visited: "+lastVisitedDate+"\n\n";
		return userProfile;
		
	}
	
	public void setUserEmail(String aUserEmail)
	{
		useremail=aUserEmail;
	}

	public String getUserEmail()
	{
		return useremail;
	}

	public void setUserId(String aUserId)
	{
		userid=aUserId;
	}

	public String getUserId()
	{
		return userid;
	}
	public void setFacebookId(String aFbId)
	{
		facebookid=aFbId;
	}

	public String getFacebookId()
	{
		return facebookid;
	}
	public void setGoogleId(String aGoogleId)
	{
		googleid=aGoogleId;
	}

	public String getGoogleId()
	{
		return googleid;
	}
	public void setProfilePic(String aPicUrl)
	{
		profilepic=aPicUrl;
	}

	public String getProfilePic()
	{
		return profilepic;
	}
	public void setProfileDesc(String aDesc)
	{
		profileDesc=aDesc;
	}
	public String getProfileDesc()
	{
		return profileDesc;
	}
	public void setName(String aName)
	{
		name=aName;
	}

	public String getName()
	{
		return name;
	}
	
	public void setLoginStatus(boolean aLoggedIn)
	{
		loggedInStatus=aLoggedIn;
	}
	public boolean getLoginStatus()
	{
		return loggedInStatus;
	}
	
	public void writeObject(java.io.ObjectOutputStream out) throws IOException{

	    out.writeObject(joinedDate);
	}
	public void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{

		joinedDate=(String)in.readObject();
	}
}
