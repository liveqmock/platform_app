package com.apalya.myplex.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardData implements Parcelable,Serializable {
	private static final long serialVersionUID = 3818882949413524664L;
	
	public static final String TAG = "CardData";
	public CardDataContent content;
	public CardDataUserReviews userReviews;
	public CardDataVideos videos;
	public List<CardDataRelatedCastItem> relatedCast;
	public CardDataCurrentUserData currentUserData;
	public CardDataRelatedContent relatedContent;
	public CardDataComments comments;
	public List<CardDataPackages> packages;
	public String _expiresAt;
	public CardDataRelatedMultimedia relatedMultimedia;
	public String liveTv;
	public List<CardDataAwards> awards;
	public CardDataCriticReviews criticReviews;
	public String _lastModifiedAt;
	public CardDataGenralInfo generalInfo;
	public CardDataImages images;
	public String _id;

	
	public CardData(){
		
	}
	public CardData(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		if(in == null){
			return;
		}
//		contentId = in.readInt();
//		contentObj = in.readString();
//		title = in.readString();
//		imageUrl = in.readString();
//		comments = in.readString();
//		reviews = in.readString();
//		isFavourite = in.readInt();
//		filterName = in.readString();
//		categoryType = in.readString();
//		defaultResId = in.readInt();
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public CardData createFromParcel(Parcel in) {
			return new CardData(in);
		}

		public CardData[] newArray(int size) {
			return new CardData[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeInt(contentId);
//		dest.writeString(contentObj);
//		dest.writeString(title);
//		dest.writeString(imageUrl);
//		dest.writeString(comments);
//		dest.writeString(reviews);
//		dest.writeInt(isFavourite);
//		dest.writeString(filterName);
//		dest.writeString(categoryType);
//		dest.writeInt(defaultResId);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static void saveObject(CardData data,String contentID,String path) {
		try {
			String filepath = path+"/"+contentID;
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filepath))); 
			oos.writeObject(data); // write the class as an 'object'
			oos.flush(); // flush the stream to insure all of the information
			oos.close();// close the stream
		} catch (Exception ex) {
			Log.v("CardData", ex.getMessage());
			ex.printStackTrace();
		}
	}
	public static CardData loadObject(String contentID,String path) {
		try {
			String filepath = path+"/"+contentID;
			File f = new File(filepath);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			Object o = ois.readObject();
			if(o instanceof CardData){
				return (CardData)o;
			}
			return null;
		} catch (Exception ex) {
			Log.v("CardData", ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}
}