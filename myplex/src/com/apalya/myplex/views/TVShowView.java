package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker.OnScrollListener;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.utils.NumberPicker;

public class TVShowView  {
	private List<CardData> mCardDataList;


	private NumberPicker npEpisode;
	private NumberPicker npSeason;

	
	public int seasonIndex = 0;
	private List<CardData> episodes = new ArrayList<CardData>();
	private List<CardData> seasons = new ArrayList<CardData>();

	public interface TVShowSelectListener{
		public void onEpisodeSelect(CardData carddata,CardData season);
		public void onSeasonChange(CardData season);
	}

	private TVShowSelectListener tvShowSelectListener;


	public TVShowView(Context context, List<CardData> mCardDataList,View rootView,TVShowSelectListener listener) {
		this.mCardDataList = mCardDataList;
		tvShowSelectListener =  listener;
		
		npSeason = (NumberPicker) rootView.findViewById(R.id.numberPickerSeason);
		npEpisode = (NumberPicker) rootView.findViewById(R.id.numberPickerEpisode);

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
		fillSeasons(mCardDataList, npSeason);

	}

	public void setCardDataList(List<CardData> mCardDataList) {
		this.mCardDataList = mCardDataList;
	}

	public void notifyDataSetChanged(){
		// update ui with mCardDataList if not null
	}

	public void onEpisodeFetchComplete(CardData season, List<CardData> episodes)
	{
		for(CardData data : mCardDataList){
			if(data.equals(season)){
				data.chields = episodes;
			}
		}
//		mCardData.chields = cardDatas;
		fillEpisode(episodes, npEpisode);	

	}

	public void fillSeasons(List<CardData> contents, NumberPicker npSeason){

		seasons = contents;
		npSeason.invalidate();
		npSeason.requestLayout();

		String seasons[] = new String[contents.size()];

		for (int i = 0; i < contents.size(); i++) {
			CardData content = contents.get(i);

			seasons[i] = content.generalInfo.title;/*+"("+content._id+")";*/
		}
		int max = npSeason.getMaxValue();

		if (seasons.length > max) {
			npSeason.setMinValue(0);
			npSeason.setValue(0);
			npSeason.setDisplayedValues(seasons);
			npSeason.setMaxValue(contents.size() - 1);
		} else {
			npSeason.setMinValue(0);
			npSeason.setValue(0);
			npSeason.setMaxValue(contents.size() - 1);
			npSeason.setDisplayedValues(seasons);
		}		
		
		npSeason.setOnScrollListener(new OnSeasonChangeListener(npSeason.getValue()));
//		npSeason.setOnValueChangedListener(new OnSeasonchangeListener());

	}

	public void fillChilds(List<CardData>  cardDatas){
		mCardDataList.get(0).chields = cardDatas;
	}
	public void fillEpisode(List<CardData> contents, NumberPicker npEpisode) {

		episodes = contents;
		
		
		
		npEpisode.setEnabled(true);
		npEpisode.invalidate();
		npEpisode.requestLayout();

		String programmes[];
		if(contents.size() == 1){
			programmes = new String[3];
			for(int i=0;i<3;i++){
				CardData content = contents.get(0);
				programmes[i] = content.generalInfo.title+"("+content._id+")";
			}
		}else{
			programmes = new String[contents.size()];
		}
		for (int i = 0; i < contents.size(); i++) {
			CardData content = contents.get(i);
			programmes[i] = content.generalInfo.title;
		}
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
	}
	public void fetchEpisodeData(CardData season) {

	}

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
			npEpisode.setMaxValue(3);	
		}else{
			npEpisode.setMinValue(0);
			npEpisode.setValue(0);
			npEpisode.setMaxValue(3);
			npEpisode.setDisplayedValues(episodeValues);
			
		}
	}
	public void initEpisodesWithLoading() {
		
		String seasonValues[]  = new String[] { "Loading...", "Loading...","Loading..." };
		int maxSeason  = npEpisode.getMaxValue();
		if( seasonValues.length > maxSeason){
			npSeason.setMinValue(0);
//			npSeason.setValue(0);
			npSeason.setDisplayedValues(seasonValues);
			npSeason.setMaxValue(3);	
		}else{
			npSeason.setMinValue(0);
//			npSeason.setValue(0);
			npSeason.setMaxValue(3);
			npSeason.setDisplayedValues(seasonValues);
			
		}
	}
	private class OnSeasonChangeListener implements OnScrollListener {
		private int oldValue ;
		
        public OnSeasonChangeListener(int initialValue) {
            oldValue = initialValue;
        }
		
        @Override
		public void onScrollStateChange(android.widget.NumberPicker picker,	int scrollState){
        	final android.widget.NumberPicker picker2 = picker;
			 if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE ){				 
									
						Log.d("amlan","fetched");
						//We get the different between oldValue and the new value
							setProgrammLoading();				 
			                int newVal = picker2.getValue() ;	                
			                if(mCardDataList.get(newVal).chields == null){
			    				if(tvShowSelectListener != null){
			    					tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
			    					return;
			    				}
			    			}else{
			    				tvShowSelectListener.onSeasonChange(mCardDataList.get(newVal));
			    				fillEpisode(mCardDataList.get(newVal).chields, npEpisode);
			    			}                
			                //Update oldValue to the new value for the next scroll
			                oldValue = picker2.getValue();
					
				}			 
			 
		};
	}
	
	private class EpisodeScrollerManager implements OnScrollListener{

		@Override
		public void onScrollStateChange(android.widget.NumberPicker view,int scrollState) {
			if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE ){
				if(tvShowSelectListener != null){					
					tvShowSelectListener.onEpisodeSelect(episodes.get(view.getValue()),seasons.get(seasonIndex));
				}		
			}			
		}
		
	}
}
