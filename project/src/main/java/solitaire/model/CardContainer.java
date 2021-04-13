package solitaire.model;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;

import solitaire.model.SolConst.SType;

public abstract class CardContainer extends Stack<Card>{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns the number of cards in this CardContainer
	 */
	public int getCardCount() {
		return this.size();
	}
	
	/**
	 * Gets the card at index n
	 * @param n index of card to get
	 */
	public Card getCard(int n) {
		if (n < 0 || n > this.size() - 1) {
			throw new IndexOutOfBoundsException("The Deck is smaller than the card number requested");
		}
		return this.get(n);
	}	

	public void addCard(Card card) {
		if (card == null) {
			throw new IllegalArgumentException("Card cannot not be null");
		}
		this.push(card);
	}

	/**
	 * Returns a comma-separated string of each element in this stack
	 */
	@Override
	public final String toString() {
		String string = "";
		Iterator<Card> iterator =  this.iterator();
		while (iterator.hasNext())
			if (string.length() == 0)
				string += iterator.next();
			else
				string += "," + iterator.next();
		return string;
	}

	public SType getStackName() {
		return this.getStackName();
	}
		
	@Override
	public Iterator<Card> iterator() {
		return new CardContainerIterator(this);
	}
		
	long getCardCount(Predicate<Card> predicate) {
		return this.stream().filter(predicate).count();
	}
}
