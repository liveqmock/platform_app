package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3114232287673074578L;
	public static final String TAG = "CardData";
	public static final int ESTDOWNLOADINPROGRESS = 1;
	public static final int ESTDOWNLOADCOMPLETE = 2;
	public static final int ESTDOWNLOADFAILED = 3;
	
	public CardDataContent content;
	public String categoryName;
	public CardDataUserReviews userReviews;
	public CardDataVideos videos;
	public CardDataRelatedCast relatedCast;
	public CardDataCurrentUserData currentUserData;
	public CardDataRelatedContent relatedContent;
	public VideoInfo videoInfo ;
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
	public MatchInfo matchInfo;
	public List<CardData> chields;
	public String promoText;
	public enum HTTP_SOURCE { CACHE , CACHE_REFRESH_NEEDED , ONLINE };
	public HTTP_SOURCE httpSource = HTTP_SOURCE.ONLINE;
	public CardData(){
		
	}

}