package solitaire.model;

import java.util.Stack;

@SuppressWarnings("serial")
public class CardDeck extends Stack<Card>{
	
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
		String string = new String();
		for (int i = 0; i < this.size(); i++) {
			//System.out.println(deck.get(i));
			string += this.get(i);
		}
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
	
	public Card getCard(int n) {
		if (n < 0 || n > this.size() - 1) {
			throw new IllegalArgumentException("The Deck is smaller than the card number requested");
		}
		return this.get(n);
	}
	
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
	
	public void deal(CardStack stack, int n) {
		if (n < 0 || n > this.size()) {
			throw new IllegalArgumentException("The Deck is smaller than the card number requested" + n + " " + this.size());
		}
		for (; n > 0; n--) {
			stack.addCard(this.get(this.size() - 1));
			this.remove(this.size() - 1);
		}
	}
}