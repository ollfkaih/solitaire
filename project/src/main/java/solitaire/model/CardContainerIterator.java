package solitaire.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CardContainerIterator implements Iterator<Card> {
	private int currentIndex = 0;
	CardContainer handOfCards;
	
	public CardContainerIterator(CardContainer handOfCards) {
		this.handOfCards = handOfCards;
	}

	@Override
	public boolean hasNext() {
		return currentIndex < handOfCards.getCardCount();
	}

	@Override
	public Card next() {
        if (!hasNext())
        	throw new NoSuchElementException(); 
		Card returnCard;
        
		try {
		returnCard = handOfCards.get(currentIndex);
		} catch (Exception e) {
			throw new NoSuchElementException(); 
		}
        currentIndex++;
		return returnCard;
	}
	
}
