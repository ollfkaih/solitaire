package solitaire.model;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class CardStack extends Stack<Card> implements CardContainer {
	
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
	public Card getCard(int n) {
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
		//card.setParentStack(this.stackName);
		this.push(card);
	}

	/**
	 * play(CardStack, int) will move the top cards including indexOfCard to any other stack (all validation of rules is done in GameBoard)
	 */
	 public void play(CardStack stack, int indexOfCard) {
		if (indexOfCard < 0 || indexOfCard > this.size() - 1) {
			throw new IllegalArgumentException("Cannot play card at index larger than stack size");
		}
		
		if (this == stack)
			throw new IllegalArgumentException("Cannot move a card to itself");
		
		while (indexOfCard < this.getCardCount()) {
			Card tempCard = this.getCard(indexOfCard);
			this.remove(indexOfCard);
			stack.addCard(tempCard);
		}
		
		//In playStacks, reveal another card if we have removed all visible cards
		if (indexOfCard == hiddenCards && hiddenCards > 0)
			hiddenCards--;
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
		
	@Override
	public String toString() {
		String string = this.getStackName().toString();
		Iterator<Card> iterator =  this.iterator();
		while (iterator.hasNext())
			string +=  "," + iterator.next();
		return string;
	}
}
