package solitaire.model;

public class Card {
	private char suit;
	private int value;
	private SolConst.SType parentStack = SolConst.SType.DECK;
	
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

	public SolConst.SType getParentStack() {
		return parentStack;
	}

	public void setParentStack(SolConst.SType parentStack) {
		//TODO: validate
		this.parentStack = parentStack;
	}
	/**
	 * legalCard checks that the suit and face value of a given card is legal 
	 * @return
	 */
	/*public boolean legalCard(Card c) {
		if (legalSuit(c.getSuit()) && legalValue(c.getFace());;)
		return false;
		
	}*/
	
	public static Card stringToCard(String cardString) {
		Card c;		
		if (cardString.length() != 2 && cardString.length() != 3) {
			throw new IllegalArgumentException("The card should be two or three characters.");
		}
		char suit = cardString.charAt(0);
		int value;
		try {
			value = Integer.parseInt(cardString.substring(1));
		} catch (Exception e) {
			throw new IllegalArgumentException("The card should end with a number from 1-13.");
		}
		try {
			c = new Card(suit, value);
		} catch (Exception IllegalArgumentExecption) {
			throw new IllegalArgumentException("Card could not be created with given string");
		}
		return c;
	}
	
	public boolean equals(Card c2) {
		if (this.suit == c2.suit && this.value == c2.value)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%c%d", this.suit, this.value);
	}
}