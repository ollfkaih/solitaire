package solitaire.model;

import java.util.Stack;

@SuppressWarnings("serial")
public class CardStack extends Stack<Card> {
	
	private int hiddenCards;
	private SolConst.SType stackName;
	
	public SolConst.SType getStackName() {
		return stackName;
	}

	public CardStack(SolConst.SType stackName) {
		this.stackName = stackName;
	}
	
	public void setHiddenCards(int hiddenCards) {
		if (hiddenCards > this.size() || hiddenCards < 0)
			throw new IllegalArgumentException("There must be fewer or equal hiddencards than cards");
		this.hiddenCards = hiddenCards;
	}
	
	/**
	 * increments the number of hidden cards in this stack.
	 */
	public void incrementHiddenCards() {
		System.out.println(hiddenCards + " count: " + this.getCardCount());
		if (hiddenCards >= this.getCardCount() - 1)
			throw new IllegalStateException("This stack has the maximum number of hidden cards");
		hiddenCards++;
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
	 * play(CardStack, int) will move the top cards including indexOfCard to any other stack (all validation of rules is done in GameBoard)
	 */
	 public void play(CardStack stack, int indexOfCard) {
		if (indexOfCard < 0 || indexOfCard > this.size() - 1) {
			throw new IllegalArgumentException("Cannot play card at index larger than stack size");
		}
		
		while (indexOfCard < this.getCardCount()) {
			Card tempCard = this.get(indexOfCard);
			this.remove(indexOfCard);
			stack.addCard(tempCard);
		}
		
		//In playStacks, reveal another card if we have removed all visible cards
		if (indexOfCard == hiddenCards && hiddenCards > 0)
			hiddenCards--;
	}

}
