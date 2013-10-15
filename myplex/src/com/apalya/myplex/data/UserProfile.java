package com.apalya.myplex.data;

public class UserProfile {
	
	private static String useremail;
	private static String userid;
	private static String facebookid;
	private static String googleid;
	private static String profilepic;
	private static String profileDesc;
	private static String name;
	private static boolean loggedInStatus;
	
	
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
}
