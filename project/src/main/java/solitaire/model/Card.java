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
	
	/**
	 * Returns the face value of a card from 1-13 as int
	 * @return
	 */
	public int getFace() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return String.format("%c%d",this.suit,this.value);
	}
	
}