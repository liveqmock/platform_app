package com.apalya.myplex.cache;

import java.util.HashMap;

import com.apalya.myplex.data.CardData;

public interface SearchResult {
		public void searchComplete(HashMap<String, CardData> resultMap);
}
