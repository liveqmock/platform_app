package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.NumberPicker.OnValueChangeListener;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.utils.NumberPicker;
import com.apalya.myplex.utils.SeasonFetchHelper;

public class TVShowView  {
	private List<CardData> mCardDataList;


	private NumberPicker npEpisode;
	private NumberPicker npSeason;

	private Handler handler = new Handler();
	private View rootView;
	private static int DELAY = 2000;
	public int seasonIndex = 1;
	private SeasonFetchHelper helper = null;
	private List<CardData> episodes = new ArrayList<CardData>();

	public interface TVShowSelectListener{
		public void onEpisodeSelect(CardData carddata);
		public void fetchEpisode(CardData season);
	}

	private TVShowSelectListener tvShowSelectListener;


	public TVShowView(Context context, List<CardData> mCardDataList,View rootView,TVShowSelectListener listener) {
		this.mCardDataList = mCardDataList;
		this.rootView = rootView;
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
		npSeason.setOnValueChangedListener(new OnSeasonchangeListener());

	}

	public void setCardDataList(List<CardData> mCardDataList) {
		this.mCardDataList = mCardDataList;
	}

	public void notifyDataSetChanged(){
		// update ui with mCardDataList if not null
	}

	public void onEpisodeFetchComplete(CardData season, List<CardData> episodes)
	{
		fillEpisode(episodes, npEpisode);	

	}

	public void fillSeasons(List<CardData> contents, NumberPicker npSeason){

		npSeason.invalidate();
		npSeason.requestLayout();

		String seasons[] = new String[contents.size()];

		for (int i = 0; i < contents.size(); i++) {
			CardData content = contents.get(i);

			seasons[i] = content.generalInfo.title;
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

		npSeason.setOnValueChangedListener(new OnSeasonchangeListener());



	}

	private class OnSeasonchangeListener implements OnValueChangeListener {
		@Override
		public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
			
			setProgrammLoading();
			if(mCardDataList.get(newVal).chields == null){
				if(tvShowSelectListener != null){
					tvShowSelectListener.fetchEpisode(mCardDataList.get(newVal));
					return;
				}
			}
			
			handler.postDelayed(new Runnable() {				
				@Override
				public void run() {
					// if no episode
//					if(tvShowSelectListener != null){
//						tvShowSelectListener.fetchEpisode(mCardDataList.get(loc));
//					}

				}
			}, DELAY);


		};
	}

	public void fillEpisode(List<CardData> contents, NumberPicker npEpisode) {

		episodes = contents;
		
		npEpisode.setEnabled(true);
		npEpisode.invalidate();
		npEpisode.requestLayout();

		String programmes[] = new String[contents.size()];
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
		npEpisode.setOnValueChangedListener(new OnEpisodeChangeListener());
	}




	private class OnEpisodeChangeListener implements OnValueChangeListener {
		@Override
		public void onValueChange(android.widget.NumberPicker picker,int oldVal, int newVal) {

			final int location = newVal;
			handler.postDelayed(new Runnable() {				
				@Override
				public void run() {
					if(tvShowSelectListener != null){
						tvShowSelectListener.onEpisodeSelect(episodes.get(location));
					}
				}
			}, DELAY);
		}

	};

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
}
