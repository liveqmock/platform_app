package com.apalya.myplex.adapters;

import com.apalya.myplex.data.CardData;

public interface CardActionListener {
	public void loadmore(int value);
	
	public void viewReady();

	public void selectedCard(int index);
	
	public void favouriteAction(CardData data,int type);

	public void deletedCard(CardData data);

	public void purchase(CardData object);

	public void open(CardData object);
}
