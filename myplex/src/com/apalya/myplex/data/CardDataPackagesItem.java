package com.apalya.myplex.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagesItem {
	public List<CardDataPackagePriceDetailsItem> priceDetails;
	public String contentType;
	public boolean couponFlag;
	public String contentId;
	public String packageName;
	public List<CardDataPromotionDetailsItem> promotionDetails;
	public String bbDescription;
	public String packageId;
	public String duration;
	public String commercialModel;
	public boolean packageIndicator;
	public boolean renewalFlag;
	public String validityPeriod;
	public CardDataPackagesItem(){}
}
