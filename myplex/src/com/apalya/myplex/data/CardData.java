package com.apalya.myplex.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardData {
	
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

}