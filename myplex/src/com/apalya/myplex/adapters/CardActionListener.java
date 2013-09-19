package com.apalya.myplex.adapters;

import com.apalya.myplex.data.CardData;

public interface CardActionListener {
	public void loadmore(int value);

	public void selectedCard(int index);

	public void addFavourite(CardData data);

	public void removeFavourite(CardData data);

	public void deletedCard(CardData data);

	public void moreInfo(int index);

	public void purchase(int index);

	public void play(CardData object);
}
