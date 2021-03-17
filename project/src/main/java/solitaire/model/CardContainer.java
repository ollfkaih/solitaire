package solitaire.model;

public interface CardContainer extends Iterable<Card> {
	int getCardCount();
	Card getCard(int n);
	Object getStackName();
	Card get(int currentIndex);
	public void addCard(Card card);	
}
