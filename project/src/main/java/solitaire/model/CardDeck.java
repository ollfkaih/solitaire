package solitaire.model;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import solitaire.model.SolConst.SType;

public class CardDeck extends Stack<Card> implements CardContainer{

	private static final long serialVersionUID = 1L;

	public CardDeck(int n) {
		if (n > 13)
			throw new IllegalArgumentException("The deck has to be at most 52 cards");
		
		for (char suit: "SHDC".toCharArray()) {
			for (int i = 1; i <= n; i++) {
				this.add(new Card(suit, i));
			}
		}
	}
	
	//Converts a stack to a deck
	public CardDeck(CardStack stack) {
		for (Card card : stack)
			this.add(card);
	}
	
	@Override
	public String toString() {
		// String string = SType.DECK.toString();
		String string = "";
		Iterator<Card> iterator =  this.iterator();
		while (iterator.hasNext())
			if (string.length() == 0)
				string += iterator.next();
			else
				string += "," + iterator.next();
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
	 * Deal moves n cards from this stack to another stack 
	 * @param stack The stack to move cards to
	 * @param n Number of cards to deal
	 */
	public void deal(CardStack stack, int n) {
		if (n < 0) {
			throw new IllegalArgumentException("Cannot play a negative number of cards: " + n + ". Stack has " + this.size() + "cards.");
		}
		for (int i = 0; i < n; i++) {
			if (this.size() - n + i >= 0) { 
				Card card = this.get(this.size() - n + i);
				stack.addCard(card);
				this.remove(card);
			}
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
	
	public void addCard(Card card) {		
		this.push(card);
	}	
}