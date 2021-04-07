package solitaire.model;

import java.util.List;

public interface CardContainer extends List<Card>{
	int getCardCount();
	Card getCard(int n);
	SolConst.SType getStackName();
	Card get(int currentIndex);
	public void addCard(Card card);	
}
