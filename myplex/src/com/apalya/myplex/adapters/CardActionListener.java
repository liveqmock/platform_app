package com.apalya.myplex.adapters;

import com.apalya.myplex.data.CardData;

public interface CardActionListener {
	public void loadmore(int value);
	
	public void selectedCard(int index);

	public void addFavourite(CardData data);

	public void removeFavourite(CardData data);

	public void deletedCard(CardData data);

	public void purchase(CardData object);

	public void open(CardData object);
}
