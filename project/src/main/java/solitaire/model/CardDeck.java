package solitaire.model;

public class CardDeck {
	private Card[] deck;
	
	/**
	 * Initializes a CardDeck with 4 suits with n cards
	 */
	public CardDeck(int n) {
		if (n > 13)
			throw new IllegalArgumentException("The deck has to be at most 52 cards");
		// Man kunne eventuelt tatt "to kortstokker" sammen, men justert slik at kortene gikk fra f.eks. 1-8 i hver 
		
		this.deck = new Card[4*n];		
		int i = 0;
		for (char suit: "SHDC".toCharArray()) {
			for (int j = 1; j <= n; j++) {
				this.deck[i*n + j - 1] = new Card(suit, j); //i*n er kort som allerede er trekt, j er verdien vi vil ha på kortet (som teller fra 1, ikke 0)
			}
		i++;
		}
	}
	
	@Override
	public String toString() {
		String string = new String();
		for (int i = 0; i < this.deck.length; i++) {
			System.out.println(deck[i]);
			string += deck[i];
		}
		return string;
	}

	public int getCardCount() {
		return this.deck.length;
	}
	
	public Card getCard(int n) {
		if (n < 0 || n > this.deck.length - 1) {
			throw new IllegalArgumentException("The Deck is smaller than the card number requested");
		}
		return deck[n];
	}
	
	public void shufflePerfectly() {
		int deckLength = this.deck.length;
		
		Card[] bottomDeck = new Card[deckLength/2];
		Card[] topDeck = new Card[deckLength/2];
		
		for (int i = 0; i < deckLength/2; i ++) {
			bottomDeck[i] = this.deck[deckLength/2 + i];
			topDeck[i] = this.deck[i];
		}
		
		for (int i = 0; i < deckLength; i+=2) {
			this.deck[i] = topDeck[i/2];
			this.deck[i+1] = bottomDeck[i/2];
		}		
	}
}