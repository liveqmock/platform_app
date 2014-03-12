package com.apalya.myplex.utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.myplexapplication;

public class FetchDownloadProgress {
	public static final String TAG = "FetchDownloadProgress";
	private Context mContext;
	private CardData mCardData;
	private List<CardData> mCardDatalist;
	private DownloadProgressStatus mListener;
	private DownloadManager mDownloadManager;
	private CardDownloadData mDownloadData;
	private boolean mStopPolling = false;
	public FetchDownloadProgress(Context cxt) {
		this.mContext = cxt;
		try {
			myplexapplication.mDownloadList = (CardDownloadedDataList) Util.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

	}

	public interface DownloadProgressStatus {
		public void DownloadProgress(CardData cardData,CardDownloadData downloadData);
	}

	public void setDownloadProgressListener(DownloadProgressStatus listener) {
		this.mListener = listener;
	}

	public void stopPolling(){
		mStopPolling = true;
	}
	public void startPolling(CardData data) {
		if(myplexapplication.mDownloadList == null){
			return;
		}
		Log.d(TAG,"Download startPolling ");
		mStopPolling = false;
		mCardData = data;
		Log.d(TAG,"size of list"+myplexapplication.mDownloadList.mDownloadedList.size());
		mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mCardData._id);
		mPollingHandler.sendEmptyMessage(CONTINUE_POLLING);
	}
	
	public void startPolling(List<CardData> data){
		
		if(myplexapplication.mDownloadList == null){
			return;
		}
		Log.d(TAG,"Download startPolling ");
		mStopPolling = false;
		mCardDatalist = data;
		mPollingHandlerMultiple.sendEmptyMessage(CONTINUE_POLLING);
	}

	private static final int CONTINUE_POLLING = -1;
	private static final int POLLING_ABORT = 0;

	private Handler mPollingHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG,"handleMessage");
			if(mStopPolling){
				return;
			}
			switch (msg.what) {
			case CONTINUE_POLLING: {
				Log.d(TAG,"handleMessage1");
				boolean continuePolling = false;
				try {
					if(mDownloadData == null || mDownloadData.mDownloadId == -1){
						return;
					}
					Log.d(TAG,"handleMessage2");
					DownloadManager.Query q = new DownloadManager.Query();
					Log.d(TAG,"Download information for "+mDownloadData.mDownloadId);
					q.setFilterById(mDownloadData.mDownloadId);
					Cursor cursor = mDownloadManager.query(q);
					if(cursor == null){
						return;
					}
					cursor.moveToFirst();
					long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

					if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
						mDownloadData.mCompleted = true;
						mDownloadData.mPercentage = 100;
						//for Analytics //Util.java startDownload() method the Analytics.downloadStartTime is initialized
						String key = mCardData.generalInfo._id+Analytics.UNDERSCORE+Analytics.downLoadStartTime;
						long startTime = SharedPrefUtils.getLongFromSharedPreference(mContext, key);
						long timetakenForDownload = System.currentTimeMillis() - startTime;
						long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(timetakenForDownload);
						long mb=1024L*1024L;
						long bytesinMB = 0;
						if(bytes_total !=0 ){
							bytesinMB = (bytes_total/mb);
						}
						Analytics.mixPanelDownloadsMovie(mCardData.generalInfo.title,mCardData._id,bytesinMB+"",timeInMinutes+"");
						//Analytics.downloadStartTime = 0;//setting the starting time to zero
						SharedPrefUtils.writeToSharedPref(mContext, Analytics.downLoadStartTime, 0);
						
					}else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
						mDownloadData.mCompleted = true;
						mDownloadData.mPercentage = 0;
					}
					else{
						final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);
						mDownloadData.mCompleted = false;
						mDownloadData.mPercentage = dl_progress;
						long mb=1024L*1024L;
						mDownloadData.mDownloadedBytes=(double)bytes_downloaded/mb;
						mDownloadData.mDownloadTotalSize=(double)bytes_total/mb;
						continuePolling = true;
					}
					Log.d(TAG,"Download information for "+mDownloadData.mDownloadId+" isCompleted = "+mDownloadData.mCompleted+" percentage = "+mDownloadData.mPercentage);
					cursor.close();
					if(mListener != null && !mStopPolling){
						mListener.DownloadProgress(mCardData, mDownloadData);
					}
				} catch (Exception e) {
					Log.d(TAG,"handleMessage4");
					// TODO: handle exception
				}
				if(continuePolling){
					sendEmptyMessage(CONTINUE_POLLING);
				}
				break;
			}
			case POLLING_ABORT: {
				break;
			}
			}
		}
	};

	private Handler mPollingHandlerMultiple = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG,"handleMessage");
			if(mStopPolling){
				return;
			}
			switch (msg.what) {
			case CONTINUE_POLLING: {
				Log.d(TAG,"handleMessage1");
				boolean continuePolling = false;
				try {
					Log.d(TAG,"size of list"+myplexapplication.mDownloadList.mDownloadedList.size());
					for (CardData data : mCardDatalist) {
						CardDownloadData downloadData = myplexapplication.mDownloadList.mDownloadedList.get(data._id);
						if(downloadData == null || downloadData.mDownloadId == -1)
						{
							continue;
						}
						Log.d(TAG,"handleMessageMultiple");
						DownloadManager.Query q = new DownloadManager.Query();
						Log.d(TAG,"Download information for "+downloadData.mDownloadId);
						q.setFilterById(downloadData.mDownloadId);
						Cursor cursor = mDownloadManager.query(q);
						if(cursor == null){
							return;
						}
						cursor.moveToFirst();
						long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
						long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

						if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
							downloadData.mCompleted = true;
							downloadData.mPercentage = 100;
						}else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
							downloadData.mCompleted = true;
							downloadData.mPercentage = 0;
						}
						else{
							final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);
							downloadData.mCompleted = false;
							downloadData.mPercentage = dl_progress;
						long mb=1024L*1024L;
						downloadData.mDownloadedBytes=(double)bytes_downloaded/mb;
						downloadData.mDownloadTotalSize=(double)bytes_total/mb;

							continuePolling = true;
						}
						Log.d(TAG,"Download information for "+downloadData.mDownloadId+" isCompleted = "+downloadData.mCompleted+" percentage = "+downloadData.mPercentage);
						cursor.close();
						if(mListener != null && !mStopPolling){
							mListener.DownloadProgress(data, downloadData);
						}
					}
				} catch (Exception e) {
					Log.d(TAG,"handleMessage4");
					// TODO: handle exception
				}
				if(continuePolling){
					sendEmptyMessage(CONTINUE_POLLING);
				}
				break;
			}
			case POLLING_ABORT: {
				break;
			}
			}
		}
	};

}
