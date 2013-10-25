package com.apalya.myplex.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardData {
	
	public static final String TAG = "CardData";
	public static final int ESTDOWNLOADINPROGRESS = 1;
	public static final int ESTDOWNLOADCOMPLETE = 2;
	public static final int ESTDOWNLOADFAILED = 3;
	
	public CardDataContent content;
	public CardDataUserReviews userReviews;
	public CardDataVideos videos;
	public CardDataRelatedCast relatedCast;
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
	public CardDataSimilarContent similarContent;
	public String _id;
	public CardDownloadData downloadData;
	public CardData(){
		
	}

}