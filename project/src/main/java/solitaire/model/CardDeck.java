package solitaire.model;

import java.util.Random;

import solitaire.model.SolConst.SType;

public class CardDeck extends CardContainer{
	private static final long serialVersionUID = 1L;
	private Random rnd = new Random();

	/** Creates a new CardDeck with cards of four suits up to a face value of n
	 *  @param n the number of cards of each suit (max 13)
	 */
	public CardDeck(int n) {
		if (n > 13 || n < 1)
			throw new IllegalArgumentException("The deck has to be at most 52 cards");
		
		for (char suit: "SHDC".toCharArray()) {
			for (int i = 1; i <= n; i++) {
				this.addCard(new Card(suit, i));
			}
		}
	}
	
	/** Converts a CardStack with any nuimber of cards to a deck 
	 * (without modifying the original CardStack)
	 *  @param stack The CardStack to make a deck of
	 */	
	public CardDeck(CardStack stack) {
		for (Card card : stack)
			this.addCard(card);
	}

	/**
	 * shuffle() reorders this deck into a pseudo-random order
	 */
	public void shuffle() {
		int index;
		int cardCountInitial = this.getCardCount();
		CardDeck tempDeck = (CardDeck) this.clone();

		//pick a pseudorandom index and (if needed) increment it until we find a card in our temporary deck
		for (int i = 0; i < cardCountInitial; i++) {
			index = (rnd.nextInt(tempDeck.size()));
			this.set(i, tempDeck.get(index));
			tempDeck.remove(index);
		}
	}
	
	/**
	 * Deal moves n cards from this stack to another stack 
	 * @param stack The stack to move cards to
	 * @param n Number of cards to deal (must be positive)
	 */
	public void deal(CardStack stack, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("Cannot play a negative number of cards: " + n + ". Stack has " + this.size() + "cards.");
		}
		if (this.isEmpty()) {
			throw new IllegalStateException("This deck is empty and cannot deal any cards");
		}
		for (int i = 0; i < n; i++) {
			if (this.size() - n + i >= 0) { 
				Card card = this.get(this.size() - n + i);
				stack.addCard(card);
				this.remove(card);
			}
		}
	}

	/**
	 * Always returns SType.Deck
	 */
	public final SType getStackName() {
		return SType.DECK ;
	}
}