package com.apalya.myplex.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class IndexHandler {

	private static final String TAG = "IndexHandler";
	 
	private static final String INDEX_DIR = "aptv-database";
	private Version LuceneVersion = Version.LUCENE_36;
	public static final String LUCENE_CONTENT_ID = "cardId";
	public static final String LUCENE_CONTENT_EXPIRY = "expiresAt";
	public static final String LUCENE_CONTENT_INFO = "cardInfo";
	
	public enum OperationType{IDSEARCH,FTSEARCH,UPDATEDB,SEARCHDB,DONTSEARCHDB}


	File mIndexDir = null;
	Directory mDirectory = null;
	IndexWriter mIndexWriter = null;
	IndexReader mIndexReader = null;

	public IndexHandler() {

        //Replace internalPath with appDirectory to store in memory card.
        //Remember to add WRITE_EXTERNAL_STORAGE permission in Manifest file
        String filePath = myplexapplication.getApplicationConfig().indexFilePath + File.separator + INDEX_DIR;

		mIndexDir = new File(filePath);
		if(mIndexDir.exists())
			Log.i(TAG,"file exists" + ":: dirSize: "+ Util.dirSize(mIndexDir)+" bytes");
			else
				Log.i(TAG,"file not present");
		try {
			mDirectory = FSDirectory.open(mIndexDir);
			Log.i(TAG, mIndexDir.getPath());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void initIndexWriter() {
		if(mIndexWriter != null)
			return;
		try {
			Analyzer analyzer = new StandardAnalyzer(LuceneVersion);
			IndexWriterConfig config = new IndexWriterConfig(LuceneVersion, analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			mIndexWriter = new IndexWriter(mDirectory, config);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public void closeIndexes() throws IOException {
		if (mIndexWriter != null)
			mIndexWriter.close(); // Close is a costly operation, Should do it
									// once per app session
		if (mIndexReader != null)
			mIndexReader.close();
	}

	public void addToIndex(CardData indexableObj) {
		try {
			if (mIndexWriter == null)
				initIndexWriter();
			long startTime = System.currentTimeMillis();
			Log.i(TAG, "Indexing singleobj Start");
			mIndexWriter.addDocument(createCardDoc(indexableObj));
			mIndexWriter.commit();
			Log.i(TAG, "Indexing singleobj time to Complete: "+(System.currentTimeMillis()-startTime)+" millisec");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public void addToIndex(List<CardData> indexableObj) {
		updateDatabase(indexableObj);
	}

	public void addToIndexAsync(List<CardData> indexableObj, InsertionResult callBack)
	{
		BgUpdateAsyncTask task = new BgUpdateAsyncTask(indexableObj, callBack);
		task.execute(OperationType.UPDATEDB);
	}
	
	private void updateDatabase(List<CardData> indexableObj)
	{
		if(indexableObj ==null || indexableObj.size() <=0)
			return ;
		try {
			Set<String> words = new HashSet(Arrays.asList(StandardAnalyzer.STOP_WORDS_SET));
			initIndexWriter();
			long startTime = System.currentTimeMillis();
            List<String> stopWrods = new ArrayList<String>();
			Log.i(TAG, "Indexing " +indexableObj.size() +"documents " +"Start");
			for (CardData indexObject : indexableObj) {
				
				if(indexObject != null &&
						indexObject.generalInfo.type != null 
							&& indexObject.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_YOUTUBE)){
					Log.i(TAG, "Indexing skip for youtube content");
					return;
				}
							
				if (indexObject != null) {
					JSONObject temp = new JSONObject(Util.toJson(indexObject, false));
					stopWrods.clear();
					collectJsonKeys(temp, stopWrods);
					words.addAll(stopWrods);
					Document doc = createCardDoc(indexObject);
					if(doc != null)
					{
//						mIndexWriter.addDocument(doc, new StandardAnalyzer(LuceneVersion, words));
						Term idTerm = new Term(LUCENE_CONTENT_ID,indexObject._id);
						mIndexWriter.updateDocument(idTerm,doc, new StandardAnalyzer(LuceneVersion, words));
						doc = null;
						temp = null;
					}
				}
			}
			Log.i(TAG, "Indexing " +indexableObj.size() +"documents " +"Complete");
			mIndexWriter.commit();
			Log.i(TAG, "time to complete Commit: " +(System.currentTimeMillis()-startTime)+" milliseconds");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	/** Recursive Function to get all Json keys from JsonObject in keyCollection */
	private void collectJsonKeys(JSONObject object, List<String> keyCollection) {
		Iterator<?> keys = object.keys();
		while (keys.hasNext()) {
			String keyName = (String) keys.next();
			keyCollection.add(keyName);
			JSONObject innerObj = object.optJSONObject(keyName);
			if (innerObj != null)
				collectJsonKeys(innerObj, keyCollection);
		}
	}

	private Document createCardDoc(CardData indexableObj) {
		Document document = null;
		try {
			document =  new Document();
			document.add(new Field(LUCENE_CONTENT_ID, indexableObj._id, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
			// We want to store the expire data, but we don't want to index it because it is useless searching through these
			document.add(new Field(LUCENE_CONTENT_EXPIRY, indexableObj._expiresAt, Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			document.add(new Field(LUCENE_CONTENT_INFO, Util.toJson(indexableObj, false), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
		} catch (JsonMappingException e) {
			document = null;
			Log.e(TAG, "JsonMappingException");
		} catch (JsonGenerationException e) {
			document = null;
			Log.e(TAG, "JsonGenerationException");
		} catch (IOException e) {
			document = null;
			Log.e(TAG, "IOException");
		}
		catch (Exception e)
		{
			document = null;
			Log.e(TAG, "Exception");
		}
		return document;
	}

	private void initIndexReader() throws CorruptIndexException, IOException {

		if(mIndexReader == null)
			mIndexReader = IndexReader.open(mDirectory);
		else if(!mIndexReader.isCurrent())
		{
//			IndexReader changedReader = IndexReader.open(mDirectory);
			IndexReader changedReader = IndexReader.openIfChanged(mIndexReader);
			if(changedReader != null)
			{
				Log.i(TAG,"oldIndexReader"+"numDocs:"+mIndexReader.numDocs() +":: maxDocs:"+mIndexReader.maxDoc() );
				if(changedReader.numDocs() > mIndexReader.numDocs())
				{
					mIndexReader.close(); // close the old Reader.
					mIndexReader = changedReader;//Assign new reader for further operations.
				}
				else
				{
					changedReader.close();
					changedReader = null;
				}
				Log.i(TAG,"newIndexReader"+"numDocs:"+mIndexReader.numDocs() +":: maxDocs:"+mIndexReader.maxDoc());
			}
			else
				Log.i(TAG, "changedReader is null");
		}
	}

	public HashMap<String, Object> searchInIndex(List<CardData> cardIds,OperationType searchType) {
		
		return doSearch(cardIds, searchType);
	}

	public void searchInIndex(List<CardData> cardIds,OperationType searchType, SearchResult callBack)
	{
		BgSearchAsyncTask bgSearchTask = new BgSearchAsyncTask(cardIds, searchType, callBack);
		bgSearchTask.execute(OperationType.SEARCHDB);
	}
	
	private HashMap<String, Object> doSearch(List<CardData> cardIds,OperationType searchType)
	{
		IndexSearcher indexSearcher = null; 
		try {
			initIndexReader();
			indexSearcher = new IndexSearcher(mIndexReader);
			QueryParser qp =null;
			switch (searchType) {
			case IDSEARCH:
				qp= new QueryParser(LuceneVersion, LUCENE_CONTENT_ID, new StandardAnalyzer(LuceneVersion));
				break;
			case FTSEARCH:
				qp= new QueryParser(LuceneVersion, LUCENE_CONTENT_INFO, new StandardAnalyzer(LuceneVersion));
				break;
			default:
				break;
			}
			
			qp.setDefaultOperator(QueryParser.Operator.OR);
			String queryString = "";
			for (CardData data : cardIds) {
				if(data != null && data._id !=null)
				{
					queryString += data._id;
					queryString += " ";
				}
			}
			if(queryString.length() == 0)
				return null;
			Query query = qp.parse(queryString);
			
			Log.i(TAG,"searching for "+queryString +" in index");
			long startTime = System.currentTimeMillis();
			
			TopDocs topDocs = indexSearcher.search(query, mIndexReader.maxDoc());
			Log.i(TAG, "time to get indexSearcher.search : "+searchType+"::"+(System.currentTimeMillis()-startTime)+" milliseconds");
			ScoreDoc[] scoreDoc = topDocs.scoreDocs;
			int totalHits = topDocs.totalHits;
			Log.i(TAG,"no. of search result: "+totalHits);
			HashMap<String, Object> searchResult = new HashMap<String, Object>();
			for (int i = 0; i < totalHits; i++) {
				Document document = indexSearcher.doc(scoreDoc[i].doc);
                JSONObject resultObj = new JSONObject(document.get(LUCENE_CONTENT_INFO));
				searchResult.put(document.get(LUCENE_CONTENT_ID), resultObj);
			}
			Log.i(TAG, "time to get Search Results From : "+searchType+"::"+(System.currentTimeMillis()-startTime)+" milliseconds");
			return searchResult;
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			try {
				if(indexSearcher != null)
					indexSearcher.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	
	}
	public void deleteDocuments() throws CorruptIndexException, IOException
	{
		initIndexWriter();
		mIndexWriter.deleteDocuments(new Term(""));
		mIndexWriter.commit();
	}
	
	public class BgUpdateAsyncTask extends AsyncTask<OperationType, Void, Void> {

		private List<CardData> mLocalcopy;
		private InsertionResult mCallback = null;


		public BgUpdateAsyncTask(List<CardData> indexObjs, InsertionResult callback) {
			mLocalcopy = indexObjs;
			mCallback = callback;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mCallback.updateComplete(true);
		}

		@Override
		protected Void doInBackground(OperationType... params) {
			updateDatabase(this.mLocalcopy);
			return null;
		}
	}

	public class BgSearchAsyncTask extends AsyncTask<OperationType,Void,Void>
	{
		private List<CardData> mCardIds = null;
		private OperationType mSearchType;
		private SearchResult mCallback = null;
		private LinkedHashMap<String, CardData> mSearchResult = new LinkedHashMap<String, CardData>();
		
		public BgSearchAsyncTask(List<CardData> cardIds, OperationType searchType, SearchResult callBack) {
			this.mCardIds = cardIds;
			this.mSearchType = searchType;
			this.mCallback = callBack;
		}
		@Override
		protected Void doInBackground(OperationType... params) {

			IndexSearcher indexSearcher = null; 
			try {
				initIndexReader();
				if(mIndexReader.numDocs() <= 0)
				{
					Log.e(TAG," numDocs is -1");
					return null;
				}
				Log.i(TAG,"numDocs:"+mIndexReader.numDocs() +":: maxDocs:"+mIndexReader.maxDoc() +":: numDeletedDocs:"+mIndexReader.numDeletedDocs());
				indexSearcher = new IndexSearcher(mIndexReader);
				QueryParser qp =null;
				switch (this.mSearchType) {
				case IDSEARCH:
					qp= new QueryParser(LuceneVersion, LUCENE_CONTENT_ID, new StandardAnalyzer(LuceneVersion));
					qp.setDefaultOperator(QueryParser.Operator.OR);
					break;
				case FTSEARCH:
					qp= new QueryParser(LuceneVersion, LUCENE_CONTENT_INFO, new StandardAnalyzer(LuceneVersion));
					qp.setDefaultOperator(QueryParser.Operator.OR);
					break;
				default:
					break;
				}
				
				if(mCardIds == null || mCardIds.size() ==0)
				{
					Log.e(TAG," mCardIds is null || size is zero");
					return null;
				}
				
				String queryString = "";
				for (CardData card : mCardIds) {
//					queryString += "\""+string1+"\"";
					if(card!=null && card._id !=null)
					{
						queryString += card._id;
						if(mSearchType == OperationType.FTSEARCH)
							queryString += "~";
						queryString += " ";
					}
				}
				
				Query query = qp.parse(queryString);
				
				Log.i(TAG,"searching for "+queryString +" in index");
				long startTime = System.currentTimeMillis();
				
				TopDocs topDocs = indexSearcher.search(query, mIndexReader.maxDoc());
				ScoreDoc[] scoreDoc = topDocs.scoreDocs;
				int totalHits = topDocs.totalHits;
				Log.i(TAG,"no. of search result: "+totalHits);
				Log.i(TAG, "time to get indexSearcher.search : "+params[0]+"::"+(System.currentTimeMillis()-startTime)+" milliseconds");
				for (int i = 0; i < totalHits; i++) {
					Document document = indexSearcher.doc(scoreDoc[i].doc);
					CardData data = (CardData) Util.fromJson(document.get(LUCENE_CONTENT_INFO), CardData.class);
//	                JSONObject resultObj = new JSONObject(document.get(LUCENE_CONTENT_INFO));
//					data.similarContent=null;
	                mSearchResult.put(document.get(LUCENE_CONTENT_ID), data);
				}
				Log.i(TAG, "time to get Search Results From : "+params[0]+"::"+(System.currentTimeMillis()-startTime)+" milliseconds");
			} catch (ParseException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				try {
					if(indexSearcher != null)
						indexSearcher.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e.getMessage());
				}
			}
		
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.e(TAG,"searchComplete callback :" +mSearchResult.size()+" results");
			mCallback.searchComplete(mSearchResult);
		}
	}
}
