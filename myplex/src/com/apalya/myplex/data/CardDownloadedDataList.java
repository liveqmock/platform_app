package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardDownloadedDataList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7402056199262714763L;

	public HashMap<String,CardDownloadData> mDownloadedList = new HashMap<String,CardDownloadData>();
}
