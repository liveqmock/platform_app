package com.apalya.myplex.utils;

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
					}
					else{
						final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);
						mDownloadData.mCompleted = false;
						mDownloadData.mPercentage = dl_progress;
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

}
