package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.utils.NumberPicker;
import com.apalya.myplex.utils.SoundUtils;

public class TVShowView  {
	
	private static final String TAG = "TVShowView";
	private List<CardData> mCardDataList;


	private NumberPicker npEpisode;
	private NumberPicker npSeason;
    private Context mContext;
	private SoundUtils mSoundUtils;	
	public int seasonIndex = 0;
	private List<CardData> episodes = new ArrayList<CardData>();
	private List<CardData> seasons = new ArrayList<CardData>();
	private Handler handler = new Handler();


	public interface TVShowSelectListener{
		public void onEpisodeSelect(CardData carddata,CardData season);
		public void onSeasonChange(CardData season);
	}

	private TVShowSelectListener tvShowSelectListener;


	public TVShowView(Context context, List<CardData> mCardDataList,View rootView,TVShowSelectListener listener) {
		this.mCardDataList = mCardDataList;
//		Collections.sort(mCardDataList, new EpisodeComparator());
		tvShowSelectListener =  listener;
		
		npSeason = (NumberPicker) rootView.findViewById(R.id.numberPickerSeason);
		npEpisode = (NumberPicker) rootView.findViewById(R.id.numberPickerEpisode);
		mContext = context;
		npSeason.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		npEpisode.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		

		initEpisodesWithLoading();
		setProgrammLoading();

	}

	public void setTvShowSelectListener(
			TVShowSelectListener tvShowSelectListener) {
		this.tvShowSelectListener = tvShowSelectListener;
	}


	public void createTVShowView()

	{
		if(mCardDataList!=null && mCardDataList.size()>0)
			fillSeasons(mCardDataList, npSeason);
		
		if(mCardDataList.size()  == 1){
			npSeason.setVisibility(View.GONE);			
		}

	}

	public void setCardDataList(List<CardData> mCardDataList) {
		this.mCardDataList = mCardDataList;
	}

	public void notifyDataSetChanged(){
		// update ui with mCardDataList if not null
	}

	public boolean onEpisodeFetchComplete(CardData season, List<CardData> episodes)
	{
		
		for(CardData data : mCardDataList){
			if(data.equals(season)){
				data.chields = episodes;
			}
		}
		if(season._id.equalsIgnoreCase(mCardDataList.get(seasonIndex)._id)){
			fillEpisode(episodes, npEpisode);	
			return true;
		}else{
			return false;
		}
//		mCardData.chields = cardDatas;

	}

	public void fillSeasons(List<CardData> card_datas, NumberPicker npSeason){

		seasons = card_datas;
		npSeason.invalidate();
		npSeason.requestLayout();

		String seasons[] = new String[card_datas.size()];

		for (int i = 0; i < card_datas.size(); i++) {
			CardData season = card_datas.get(i);
			String appendString ="";
			
			if(season.content.serialNo==null || season.content.serialNo.length()==0){
				Log.i(TAG,"contents.serialNo is Null");
				seasons[i] = "Se00";
				//seasons[i] = content.generalInfo.title;
			}
			else if(season.content.serialNo.length()>0)
			{ 
				if(Integer.parseInt(season.content.serialNo)< 10)
				{
					appendString = "Se0";
				}else{
					appendString = "Se";					
				}
				seasons[i] = appendString.concat((season.content.serialNo));
				
				Log.i(TAG,"contents.serialNo is :"+ season.content.serialNo);
				
			
			}				
	
		}
//		Arrays.sort(seasons);
		
		int max = npSeason.getMaxValue();

		if (seasons.length > max) {
			npSeason.setMinValue(0);
			npSeason.setValue(0);
			npSeason.setDisplayedValues(seasons);
			npSeason.setMaxValue(card_datas.size() - 1);
		} else {
			npSeason.setMinValue(0);
			npSeason.setValue(0);
			npSeason.setMaxValue(card_datas.size() - 1);
			npSeason.setDisplayedValues(seasons);
		}		
		
		npSeason.setOnScrollListener(new OnSeasonChangeListener(npSeason.getValue()));
		npSeason.setOnValueChangedListener(new OnSeasonChangeListener(npSeason.getValue()));
	}

	public void fillChilds(List<CardData>  cardDatas){
		mCardDataList.get(0).chields = cardDatas;
	}
	public void fillEpisode(List<CardData> contents, NumberPicker npEpisode) {

		episodes = contents;
		
		
		
		npEpisode.setEnabled(true);
		npEpisode.invalidate();
		npEpisode.requestLayout();

		String programmes[],appends[];
		if(contents.size() == 1){
			programmes = new String[3];
			appends = new String[3];
			for(int i=0;i<3;i++){
				CardData content = contents.get(0);
				programmes[i] = content.generalInfo.title+"("+content._id+")";
				appends[i]= "";
			}
		}else{
			programmes = new String[contents.size()];
			appends = new String[contents.size()];
		}
		for (int i = 0; i < contents.size(); i++) {
			CardData episode = contents.get(i);
			programmes[i] = "\""+episode.generalInfo.title+"\"";
			String appendString ="";
			//		programmes[i] = content.generalInfo.title ;
		
			if(episode.content.serialNo==null || episode.content.serialNo.length()==0){
				Log.i(TAG,"contents.serialNo is Null");
				appends[i] = "Ep 00";
				//seasons[i] = content.generalInfo.title;
			}
			else if(episode.content.serialNo.length()>0)
			{ 
				if(Integer.parseInt(episode.content.serialNo)< 10)
				{
					appendString = "Ep 0";
				}else{
					appendString = "Ep ";					
				}
				appends[i] = appendString.concat((episode.content.serialNo));
				
				Log.i(TAG,"contents.serialNo is :"+ episode.content.serialNo);
				
			
			} 
			appends[i]= appends[i].concat(" ");
			programmes[i]= appends[i].concat(programmes[i]);
		}
		
//		Arrays.sort(programmes);
		
		int max = npEpisode.getMaxValue();

		if (programmes.length > max) {
			npEpisode.setMinValue(0);
			npEpisode.setValue(0);
			npEpisode.setDisplayedValues(programmes);
			npEpisode.setMaxValue(contents.size() - 1);
		} else {
			npEpisode.setMinValue(0);
			npEpisode.setValue(0);
			npEpisode.setMaxValue(contents.size() - 1);
			npEpisode.setDisplayedValues(programmes);
		}
//		npEpisode.setOnValueChangedListener(new OnEpisodeChangeListener());
		npEpisode.setOnScrollListener(new EpisodeScrollerManager());
		npEpisode.setOnValueChangedListener(new EpisodeScrollerManager());
	}
	public void fetchEpisodeData(CardData season) {

	}
	//private void playSound() {}

	public void setProgrammLoading() 
	{
		
		npEpisode.setEnabled(false);
		npEpisode.invalidate();
		npEpisode.requestLayout();
		
		String episodeValues[]  = new String[] { "Loading...", "Loading...","Loading..." };
		int maxEpisodes  = npEpisode.getMaxValue();
		
		if( episodeValues.length > maxEpisodes){
			npEpisode.setMinValue(0);
			npEpisode.setValue(0);
			npEpisode.setDisplayedValues(episodeValues);
			npEpisode.setMaxValue(2);	
		}else{
			npEpisode.setMinValue(0);
			npEpisode.setValue(0);
			npEpisode.setMaxValue(2);
			npEpisode.setDisplayedValues(episodeValues);
			
		}
	}
	public void initEpisodesWithLoading() {
		
		String seasonValues[]  = new String[] { "Loading...", "Loading..","Loading..." };
		int maxSeason  = npEpisode.getMaxValue();
		if( seasonValues.length > maxSeason){
			npSeason.setMinValue(0);
//			npSeason.setValue(0);
			npSeason.setDisplayedValues(seasonValues);
			npSeason.setMaxValue(2);	
		}else{
			npSeason.setMinValue(0);
//			npSeason.setValue(0);
			npSeason.setMaxValue(2);
			npSeason.setDisplayedValues(seasonValues);
			
		}
		if(mSoundUtils!=null){mSoundUtils= null;}
		mSoundUtils = new SoundUtils(mContext,R.raw.keypress);
		mSoundUtils.playSound();
		
	}
	private class OnSeasonChangeListener implements OnScrollListener,OnValueChangeListener {
		private int oldValue ;
		boolean scrollingSeason = false;

		
        public OnSeasonChangeListener(int initialValue) {
            oldValue = initialValue;
        }
		
        @Override
		public void onScrollStateChange(android.widget.NumberPicker picker,	int scrollState){
        	final android.widget.NumberPicker picker2 = picker;
        	if(scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
        		scrollingSeason =true;
			}
			 if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE){				 
									
						Log.d("amlan","fetched "+scrollState);
						//We get the different between oldValue and the new value
						setProgrammLoading();				 
						handler.postDelayed(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
				                int newVal = picker2.getValue() ;	
				                seasonIndex = newVal;
				                if(mCardDataList.get(newVal).chields == null){
				    				if(tvShowSelectListener != null){
				    					tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
				    					return;
				    				}
				    			}else{
				    				tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
				    				fillEpisode(mCardDataList.get(newVal).chields, npEpisode);
				    			}                
							}
						},300);
						scrollingSeason =false;
					
				}			 
			 
		}

		@Override
		public void onValueChange(final android.widget.NumberPicker picker,int oldVal, int newVal) {
			if(mSoundUtils!=null){mSoundUtils= null;}
			mSoundUtils = new SoundUtils(mContext,R.raw.keypress);
			mSoundUtils.playSound();
			// TODO Auto-generated method stub
			
			if(scrollingSeason ==false){
				setProgrammLoading();	
				Handler h = new Handler();
				h.postDelayed(new Runnable(){
					@Override
					public void run() {
						handler.postDelayed(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
				                int newVal = picker.getValue() ;	
				                seasonIndex = newVal;
				                if(mCardDataList.get(newVal).chields == null){
				    				if(tvShowSelectListener != null){
				    					tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
				    					return;
				    				}
				    			}else{
				    				tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
				    				fillEpisode(mCardDataList.get(newVal).chields, npEpisode);
				    			}                
							}
						},300);
						
					}
				},700);			
				
			}			
		}
	}
	
	private class EpisodeScrollerManager implements OnScrollListener,OnValueChangeListener{
	private	int mScrollState = NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;
		@Override
		public void onScrollStateChange(final android.widget.NumberPicker view,int scrollState) {
			if(scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
				mScrollState = NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL ;
			}
			if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE ){
				if(tvShowSelectListener != null){		
					
					mScrollState = NumberPicker.OnScrollListener.SCROLL_STATE_IDLE ;
					handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							tvShowSelectListener.onEpisodeSelect(episodes.get(view.getValue()),seasons.get(seasonIndex));
						}
					},300);
					
				}		
			}			
		}

		@Override
		public void onValueChange(final android.widget.NumberPicker picker,
				int oldVal, int newVal) {
			Handler h = new Handler();
			h.postDelayed(new Runnable(){
				@Override
				public void run(){
					if(mScrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE){
						if(tvShowSelectListener != null){
						handler.postDelayed(new Runnable(){

							@Override
							public void run() {
						tvShowSelectListener.onEpisodeSelect(episodes.get(picker.getValue()),seasons.get(seasonIndex));
							}	
						},300);
						
						}	
					}
					
				}
				
			}
			,700);
			
			/*
			if(mSoundUtils!=null){mSoundUtils= null;}
			mSoundUtils = new SoundUtils(mContext,R.raw.keypress);
			mSoundUtils.playSound();*/
			// TODO Auto-generated method stub
			
		}
		
	}
	private class EpisodeComparator implements Comparator<CardData>{
		@Override
		public int compare(CardData lhs, CardData rhs) {			
			return rhs._id.compareTo(lhs._id);
		}
		
	}
}

