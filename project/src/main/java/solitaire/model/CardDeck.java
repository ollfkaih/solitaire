package solitaire.model;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import solitaire.model.SolConst.SType;

@SuppressWarnings("serial")
public class CardDeck extends Stack<Card> implements CardContainer{
	
	public CardDeck(int n) {
		if (n > 13)
			throw new IllegalArgumentException("The deck has to be at most 52 cards");
		
		for (char suit: "SHDC".toCharArray()) {
			for (int i = 1; i <= n; i++) {
				this.add(new Card(suit, i));
			}
		}
	}
	
	@Override
	public String toString() {
		// String string = SType.DECK.toString();
		String string = "";
		Iterator<Card> iterator =  this.iterator();
		while (iterator.hasNext())
			string +=  "," + iterator.next();
		return string;
	}

	public int getCardCount() {
		int i = 0;
		int count = 0;
		while (i < this.size()) {
			if (this.get(i) != null) {
				i++;
				count++;
			} else 
				i++;
		}
		return count;	
	}
	
	//TODO: Review removal ? (Execption is thrown by .get() anyways if the index is out of bounds)
	public Card getCard(int n) {
		if (n < 0 || n > this.size() - 1) {
			throw new IllegalArgumentException("The Deck is smaller than the card number requested");
		}
		return this.get(n);
	}
	/**
	 * Shuffle perfectly puts the stack in an order where every second card comes from the top half of 
	 * the deck, and the other cards come from the bottom half (not really a shuffle)
	 */
	public void shufflePerfectly() {
		int deckLength = this.size();
		
		Card[] bottomDeck = new Card[deckLength/2];
		Card[] topDeck = new Card[deckLength/2];
		
		for (int i = 0; i < deckLength/2; i ++) {
			bottomDeck[i] = this.get(deckLength/2 + i);
			topDeck[i] = this.get(i);
		}
		
		for (int i = 0; i < deckLength; i+=2) {
			this.set(i, topDeck[i/2]);
			this.set(i+1,bottomDeck[i/2]);
		}		
	}

	/**
	 * shuffle puts this deck into a pseudo-random order
	 */
	public void shuffle() {
		Random rnd = new Random();
		int index;
		int cardCountNow = this.getCardCount();
		CardDeck tempDeck = (CardDeck) this.clone();

		for (int i = 0; i < cardCountNow; i++) {
			index = (rnd.nextInt(SolConst.SUITS*SolConst.CARDSINSUIT));
			while (tempDeck.get(index) == null) {
				if (index == 51)
					index = 0;
				else
					index++;
			}
			this.set(i, tempDeck.get(index));
			tempDeck.set(index, null);
		}
	}
	
	/**
	 * Deal moves n cards from this stack to another stack (the first method parameter)
	 * @param stack
	 * @param n
	 */
	public void deal(CardStack stack, int n) {
		if (n < 0) {
			throw new IllegalArgumentException("Cannot play a negative number of cards: " + n + ". Stack has " + this.size() + "cards.");
		}
		for (; n > 0; n--) {
			stack.addCard(this.get(this.size() - 1));
			this.remove(this.size() - 1);
		}
	}
	
	@Override
	public Iterator<Card> iterator() {
		return new CardContainerIterator(this);
	}
	
	boolean hasCard(Predicate<Card> predicate) {
		return this.stream().anyMatch(predicate);
	}
	
	int getCardCount(Predicate<Card> predicate) {
		return this.stream().filter(predicate).collect(Collectors.toList()).size();
	}
	
	List<Card> getCards(Predicate<Card> predicate) {
		return this.stream().filter(predicate).collect(Collectors.toList());
	}

	@Override
	public SType getStackName() {
		return SType.DECK ;
	}
	
	

	
}