package solitaire.model;

import java.util.Stack;

@SuppressWarnings("serial")
public class CardStack extends Stack<Card> {
	
	private int hiddenCards;
	private SolConst.Stack stackName;
	
	public SolConst.Stack getStackName() {
		return stackName;
	}

	public CardStack(SolConst.Stack stackName) {
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
		else if (this.isHidden(n))
			throw new IllegalArgumentException("This card is hidden");
		else 
			return super.get(n);
	}
	
	/**
	 * Returns true if the card at index n in this stack is hidden
	 * @param n
	 * @return
	 */
	public boolean isHidden(int n) {
		if (n < hiddenCards)
			return true;
		else
			return false;
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
			stack.addCard(tempCard);
		}
		
		//In playStacks, reveal another card if we have removed all visible cards
		if (indexofCard == hiddenCards && hiddenCards > 0)
			hiddenCards--;
	}

}
