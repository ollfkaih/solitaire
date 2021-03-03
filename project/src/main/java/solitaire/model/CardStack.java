package solitaire.model;

import java.util.Stack;

public class CardStack extends Stack<Card> {
		
	private int hiddenCards;
	private Card.Stack stackName;
	
	public Card.Stack getStackName() {
		return stackName;
	}

	public CardStack(Card.Stack stackName) {
		this.stackName = stackName;
	}
	
	public void setHiddenCards(int hiddenCards) {
		if (hiddenCards > this.size() || hiddenCards < 0)
			throw new IllegalArgumentException("There must be fewer or equal hiddencards than cards");
		this.hiddenCards = hiddenCards;
	}
	
	public int getCardCount() {
		return this.size();
	}
	
	@Override
	/**
	 * get(int index) returns the card in the stack at the index if it is not hidden
	 */
	public Card get(int n) {
		if (n < 0 || n > this.size() - 1) 
			throw new IllegalArgumentException("The stack is smaller than the card number requested");
		else if (n < hiddenCards)
			throw new IllegalArgumentException("This card is hidden");
		else 
			return super.get(n);
	}
		
	public void addCard(Card card) {		
		card.setParentStack(this.stackName);
		this.push(card);
	}

	/**
	 * play(CardStack, int) will move the n top cards to any other stack (all validation of rules is done in GameBoard)
	 */
	 public void play(CardStack stack, int indexofCard) {
		if (indexofCard < 0 || indexofCard > this.size() - 1) {
			throw new IllegalArgumentException("Cannot play card at index larger than stack size");
		}
		
		while (indexofCard < this.getCardCount()) {
			Card tempCard = this.get(indexofCard);
			this.remove(indexofCard);
			if (hiddenCards > 0)
				hiddenCards--;
			stack.addCard(tempCard);
		}
 		//return true;
	}

}
