package solitaire.model;

public class Card {
	private char suit;
	private int value;
	
	public Card(char suit, int value) {
		legalSuit(suit);
		legalValue(value);
		this.suit = suit;
		this.value = value;
	}

	private void legalValue(int value) {
		if (value > 13 || value < 1) {
			throw new IllegalArgumentException("Card value must be number from 1 (ace) to 13 (king)");
		}
		
	}

	private void legalSuit(char suit) {
		if (("CHDS").indexOf(suit) == -1) {
			throw new IllegalArgumentException("Card type must be either S, H, D or C");
		}
	}
	
	public char getSuit() {
		return this.suit;
	}
	
	public int getFace() {
		return this.value;
	}
	
	/**
	 * Returns true if both the suit and face value of this card is equal to card2
	 */
	public boolean equals(Card card2) {
		if (this.getSuit() == card2.getSuit() && this.getFace() == card2.getFace())
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return this.suit + "" + this.value;
	}
}