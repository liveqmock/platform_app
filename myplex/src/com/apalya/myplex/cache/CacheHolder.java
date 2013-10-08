package com.apalya.myplex.cache;

import java.util.HashMap;
import java.util.List;

import com.apalya.myplex.cache.IndexHandler.OperationType;
import com.apalya.myplex.data.CardData;

public class CacheHolder {

	IndexHandler mIndexHandler = null;

	public CacheHolder() {
		mIndexHandler = new IndexHandler();
	}

//	public void InsertData(String cardId, String expiryInterval, Object objectToCache) {
//		mIndexHandler.addToIndex(new IndexObject(cardId, expiryInterval, objectToCache.toString()));
//	}
//
//	public void UpdateData(String cardId, String expiryInterval, Object objectToCache) {
//		JSONObject objToIndex = (JSONObject) objectToCache;
//		mIndexHandler.addToIndex(new IndexObject(cardId, expiryInterval, objToIndex.toString()));
//	}

	public void UpdataData(List<CardData> datatoInsert) {
		mIndexHandler.addToIndex(datatoInsert);
	}
	
	public void UpdataDataAsync(List<CardData> datatoInsert, InsertionResult callBack) {
		mIndexHandler.addToIndexAsync(datatoInsert, callBack);
	}

	/** returns JSON Object for given cardId */
//	public Object GetData(String cardId) {
//		List<String> cardlist = new ArrayList<String>();
//		cardlist.add(cardId);
//		HashMap<String, Object> resultSet = mIndexHandler.searchInIndex(cardlist,OperationType.IDSEARCH);
//		return resultSet.get(cardId);
//	}
//
	/** returns JSON Objects for given cardIds */
	public HashMap<String, Object> GetData(List<CardData> cardIds,OperationType type, SearchResult callBack) {
		switch (type) {
		case IDSEARCH:
			if(callBack == null){
				HashMap<String, Object> resultSet = mIndexHandler.searchInIndex(cardIds,OperationType.IDSEARCH);
				return resultSet;
			}
			mIndexHandler.searchInIndex(cardIds,OperationType.IDSEARCH,callBack);
			break;
		case FTSEARCH:
		default:
			if(callBack ==null)
			{
				HashMap<String, Object> resultSet = mIndexHandler.searchInIndex(cardIds,OperationType.FTSEARCH);
				return resultSet;
			}
			mIndexHandler.searchInIndex(cardIds,OperationType.FTSEARCH,callBack);
			break;
		}
		return null;
	}
//
//	public HashMap<String, Object> search(String textToSearch, SearchResult callBack) {
//		List<String> cardlist = new ArrayList<String>();
//		cardlist.add(textToSearch);
//		
//		if(callBack ==null)
//		{
//			HashMap<String, Object> resultSet = mIndexHandler.searchInIndex(cardlist,OperationType.FTSEARCH);
//			return resultSet;
//		}
//		mIndexHandler.searchInIndex(cardlist,OperationType.FTSEARCH,callBack);
//		return null;
//	}
//
	public HashMap<String, Object> search(List<CardData> multiTextSearch, SearchResult callBack) {
		if(callBack ==null)
		{
			HashMap<String, Object> resultSet = mIndexHandler.searchInIndex(multiTextSearch,OperationType.FTSEARCH);
			return resultSet;
		}
		mIndexHandler.searchInIndex(multiTextSearch,OperationType.FTSEARCH,callBack);
		return null;
	}
//
//	public void CloseCache() {
//		try {
//			mIndexHandler.closeIndexes();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
