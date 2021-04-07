package solitaire.model;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Predicate;

public class CardStack extends Stack<Card> implements CardContainer {
	
	private static final long serialVersionUID = 1L;
	private int hiddenCards = 0;
	private final SolConst.SType stackName;
	
	/**
	 * Instantiates this CardStack with the given stackname and adds cards to it (if any given)
	 * @param stackName The name of this stack
	 * @param cards The cards to add
	 */
	public CardStack(SolConst.SType stackName, Card...cards) {
		if (stackName == null)
			throw new IllegalArgumentException("Stackname must be an SType");
		this.stackName = stackName;
		for (Card card: cards)
			this.addCard(card);
	}
	
	/**
	 * Instantiates this CardStack with the given stackname and adds cards to it (if any given)
	 * also sets the number of hidden cards in this stack
	 * @param stackName The name of this stack
	 * @param hiddenCards the number of hidden cards in this stack
	 * @param cards The cards to add
	 */
	public CardStack(SolConst.SType stackName, int hiddenCards, Card...cards) {
		this(stackName, cards);
		setHiddenCards(hiddenCards);
	}

	public SolConst.SType getStackName() {
		return stackName;
	}
	
	private void setHiddenCards(int hiddenCards) {
		if (hiddenCards > this.size() || hiddenCards < 0)
			throw new IllegalArgumentException("Number of hiddencards must be less than or equal the number of cards");
		this.hiddenCards = hiddenCards;
	}
	
	public int getHiddenCards() {
		return this.hiddenCards;
	}
	
	/**
	 * decrement the number of hidden cards in this stack (but only if the top card is hidden)
	 */
	public void decrementHiddenCards() {
		if (hiddenCards > 0 && isHidden(getCardCount() - 1))
			hiddenCards--;
		else if (hiddenCards <= 0)
			throw new IllegalStateException("This stack has no hidden cards");
		else 
			throw new IllegalArgumentException("The top card must be hidden to reveal hidden cards");
	}
	
	public int getCardCount() {
		return this.size();
	}
	
	/**
	 * getCard(int index) returns the card at index n, if it is not hidden
	 * @param n Index of card to get 
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
	
	/**
	 * Puts a card on top of this stack
	 */
	public void addCard(Card card) {		
		this.push(card);
	}

	/**
	 * play(CardStack, int) will move the top cards including indexOfCard to any other stack (all validation of rules is done in GameBoard)
	 */
	 public void play(CardContainer stack, int indexOfCard) {
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
	}
	
	@Override
	public Iterator<Card> iterator() {
		return new CardContainerIterator(this);
	}
	
	boolean hasCard(Predicate<Card> predicate) {
		return this.stream().anyMatch(predicate);
	}
		
	long getCardCount(Predicate<Card> predicate) {
		return this.stream().filter(predicate).count();
	}
		
	@Override
	public String toString() {
		String string = "";
		Iterator<Card> iterator =  this.iterator();
		while (iterator.hasNext()) {
			if (string.length() == 0)
				string += iterator.next();
			else
				string += "," + iterator.next();
		}
		return string;
	}
}
