package solitaire.model;

public interface CardContainer extends Iterable<Card> {
	int getCardCount();
	Card get(int n);
}
