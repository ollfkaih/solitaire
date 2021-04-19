package solitaire.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CardContainerIterator implements Iterator<Card> {
	private int currentIndex = 0;
	CardContainer cardContainer;
	
	public CardContainerIterator(CardContainer cardContainer) {
		this.cardContainer = cardContainer;
	}

	@Override
	public boolean hasNext() {
		return currentIndex < cardContainer.getCardCount();
	}

	@Override
	public Card next() {
        if (!hasNext())
        	throw new NoSuchElementException(); 
		Card returnCard;
        
		try {
		returnCard = cardContainer.get(currentIndex);
		} catch (Exception e) {
			throw new NoSuchElementException(); 
		}
        currentIndex++;
		return returnCard;
	}
	
}
