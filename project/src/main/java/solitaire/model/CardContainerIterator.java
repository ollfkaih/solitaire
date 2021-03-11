package solitaire.model;

import java.util.Iterator;

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
            return null;
		Card returnCard;
        
		try {
		returnCard = handOfCards.get(currentIndex);
		} catch (IllegalArgumentException e) {
			return null;
		}
        currentIndex++;
		return returnCard;
	}
	
}
