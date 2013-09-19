package com.apalya.myplex.data;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class CardData implements Parcelable {
	public String title;
	public String imageUrl;
	public boolean isFavorite = false;
	public boolean applyFavoriteInProgress = false;
	public String filterName;
	public int resId;
	public Rect ImageRect = new Rect();
	public int ImageX;
	public int ImageY;

	public CardData(String title, String imageUrl, int resId) {
		this.imageUrl = imageUrl;
		this.title = title;
		this.resId = resId;
	}

	public CardData(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		if(in == null){
			return;
		}
		resId = in.readInt();
		title = in.readString();
		imageUrl = in.readString();
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
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(resId);
		dest.writeString(title);
		dest.writeString(imageUrl);
	}
}