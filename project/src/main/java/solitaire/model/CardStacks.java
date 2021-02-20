package solitaire.model;

import java.util.*;

class CardStacks {
	private final static int PLAYSTACKSNUM = 7; // Seven play stacks
	private final static int SUITS = 4;
	private final static int CARDSINSUITE = 13;
//	private final static int PLAYSTACKSSUM = 28; // Seven cards in the seventh position, six ... gives  7+6+5+4+3+2+1=28
	
	private Card[][] finalStacks = new Card[SUITS][CARDSINSUITE]; //four final stacks 
	private List<Card>[] playStacks = (List<Card>[]) new ArrayList[PLAYSTACKSNUM]; //triangular playing stacks for temporary placement of cards
	private List<Card> drawingStack = new ArrayList<Card>(); //the stack cards are drawn from, three by three
	private List<Card> throwStack = new ArrayList<Card>(); //The stack of drawn cards next to the drawingStack
	
	/**
	 * This constructor places a CardDeck into a legal starting position by putting one card on every playStacks[],
	 * then one on the six last, one more on the five last, and so on until only one is placed on the top of the seventh.
	 * When these 28 cards are dealt, the remaining 24 are put into the playing stack.
	 * @param deck 
	 */
	public CardStacks(CardDeck deck) {
		int deckLength = deck.getCardCount();
		
		for (int i = 0; i < PLAYSTACKSNUM; i++) {
			playStacks[i] = new ArrayList<>();
		}
		int pos = 0; //Position of the deck to draw from
		for (int i = 0; i < PLAYSTACKSNUM; i++) {
			for (int j = i; j < PLAYSTACKSNUM; j++) {
				playStacks[j].add(deck.getCard(pos));
				pos++;
			}
		}
		for (; pos < deckLength; pos++) {
			drawingStack.add(deck.getCard(pos));
		}
		
//		Remove
//		finalStacks[0][0] = new Card('D',1);
//		finalStacks[0][1] = new Card('D',2);
//		this.drawingStack.set(23, new Card('S',1));
	}
	
	/**
	 * 
	 */
	private boolean isCardFree(Card Card, List<Card> fromStack) {
		//TODO: check if card is in the drawstack, and return false ?
		if (fromStack.get(fromStack.size() - 1) == Card) {
			return true;
		}
		return false;
	}
	
	/**
	 * Legalmove(Card, List) takes in a card and a list argument.
	 * It checks if the Card can be moved on top of the list, which should be a in the playStacks array.
	 * @return
	 */
	private boolean legalMove(Card topCard, List<Card> fromStack, List<Card> toStack) {
		if (!isCardFree(topCard, fromStack)) {
			return false;
		}
		if (topCard.getFace() == 13 && toStack.size() == 0) {
			return true;
		} else if (topCard.getFace() == toStack.get(toStack.size() - 1).getFace() - 1 ) {
			switch (toStack.get(toStack.size() - 1).getSuit()) {
			case 'S','C': {
				if ("DH".indexOf(topCard.getSuit()) != -1) {
					return true;
				}
				break;
			}
			case 'D','H': {
				if ("SC".indexOf(topCard.getSuit()) != -1) {
					return true;
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + toStack.get(toStack.size() - 1).getSuit());
			}
		}
		return false;
	}
	
	
	/**
	 * Legalmove(Card, Array) takes in a card and an array argument.
	 * It checks if the Card can be moved on top of the list, which should be a in the finalStacks array.
	 * @return
	 */
	private boolean legalMove(Card topCard, List<Card> fromStack, Card[] toStack) {
		if (!isCardFree(topCard, fromStack)) {
			return false;
		}
		if (topCard.getFace() == 1) {
			for (Card[] currentStack : this.finalStacks) {
				if (currentStack[0] != null) {
					if (currentStack[0].getSuit() == topCard.getSuit()) {
						throw new IllegalStateException("There is already a card of this suit in the final stacks, therefore we cannot put the ace on top of the other cards.");
						// This exception could also be a return false, because we shouldn't do anything, however the game is in an illegal state.
					}
				}
			} return true; //No stacks had a card of the same suit, and topCard is an ace.
		} else {
			for (Card[] currentStack : this.finalStacks) {
				for (int i = 1; i < currentStack.length; i++) {
					if (currentStack[i] == null && currentStack[i-1] != null) {
						if (currentStack[i-1].getFace() == topCard.getFace() - 1 && currentStack[i-1].getSuit() == topCard.getSuit() && toStack.equals(currentStack)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}		
		/*if (this.drawingStack.contains(bottomCard))
			return false;
		else if (Arrays.asList(finalStacks).contains(bottomCard)) {
			return true;
		}
		Arrays.stream(finalStacks)
			.flatMap(Arrays::stream)
			.anyMatch(card -> card == bottomCard);
		
		System.out.println("" + topCard + bottomCard.getClass());
		return false;*/
	
	/**moveCard(Card, fromStack, List toStack) moves a card to a playstack after checking with legalMove.
	 * 
	 */
	public void moveCard(Card topCard, List<Card> fromStack, List<Card> toStack) {
		//System.out.println(topCard + " " + toStack);
		if (!legalMove(topCard, fromStack, toStack))
			return;
		else {
			toStack.add(topCard);
			if (this.drawingStack.get(this.drawingStack.size() - 1) == topCard) {
				this.drawingStack.remove(this.drawingStack.size() - 1);
				return;
			}
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].get(this.playStacks[i].size() - 1) == topCard) {
					this.playStacks[i].remove(this.playStacks[i].size() - 1);
					return;
				}
			}
		}
	}
	
	/**moveCard(Card, fromStack, array toStack) moves a card to a finalStack after checking with legalMove.
	 * 
	 */
	public void moveCard(Card topCard, List<Card> fromStack, Card[] toStack) {
		//System.out.println(topCard + " " + toStack);
		if (!legalMove(topCard, fromStack, toStack))
			return;
		else {
			int j = -1;
			for (int i = toStack.length - 1; i >= 0; i--) {
				if (toStack[i] == null) {
					j = i;
				}
			}
			if (j >= 0) {
				toStack[j] = topCard;
			} else {
				throw new IllegalArgumentException("The finalStack attempted to add a card to is full");
			}
			if (this.drawingStack.get(this.drawingStack.size() - 1) == topCard) {
				this.drawingStack.remove(this.drawingStack.size() - 1);
				return;
			}
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].get(this.playStacks[i].size() - 1) == topCard) {
					this.playStacks[i].remove(this.playStacks[i].size() - 1);
					return;
				}
			}
		}
	}
	
	/**
	 * dealThree takes the top three (or the remaining cards if less than three) cards from the drawingStack and puts them on the
	 * throwStack 
	 */
	public void dealThree() {
		if (drawingStack.size() <= 0) {
			throw new IllegalStateException("The drawingStack is empty, put the throwStack back onto it first"); //TODO: catch this when called
		}
		for (int i = 1; i <= 3 && drawingStack.size() > 0; i++ ) {
			throwStack.add(drawingStack.get(drawingStack.size() - 1));
			drawingStack.remove(drawingStack.size() - 1);
		}
	}
	
	/**
	 * resetDrawStack swaps the drawStack and the throwStack, given that drawStack is empty
	 */
	public void resetDrawStack() {
		if (drawingStack.size() != 0) {
			throw new IllegalStateException("The drawingStack is not empty, cards should be drawn from it and put on the throwStack first");
		}
		//drawingStack.clear
		if (throwStack.size() > 0) { 
			drawingStack.addAll(throwStack);
			throwStack.clear();
		} else if (throwStack.size() == 0) {
			return; //Both stacks are empty, so nothing to be done.
		}
	}
	
	public static void main(String[] args) {
		CardDeck deck = new CardDeck(13);
		deck.shufflePerfectly();
		deck.shufflePerfectly();
		CardStacks stacks = new CardStacks(deck);
		//Collections.rotate(stacks.drawingStack, 3);
		for (int i = 0; i < 8; i++)
			stacks.dealThree();
		stacks.resetDrawStack();
		//Card testCard = new Card('S',5);
		
//		boolean jack = stacks.legalMove(stacks.drawingStack.get(stacks.drawingStack.size() - 1), stacks.drawingStack , stacks.finalStacks[0]);
//		boolean jack = stacks.legalMove(stacks.drawingStack.get(23), stacks.drawingStack , stacks.playStacks[3]);
//		stacks.moveCard(stacks.drawingStack.get(23), stacks.drawingStack, stacks.finalStacks[1]);
//		stacks.moveCard(stacks.playStacks[0].get(0), stacks.playStacks[0], stacks.finalStacks[0]);
//		
//		System.out.println(" " + stacks.playStacks[3].get(stacks.playStacks[3].size() - 1) + " " + stacks.finalStacks[0][0]);
//		
		/*int k = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = i; j < 7; j++) {
				k++;
				System.out.println(i + " " +  j + " " + stacks.playStacks[j].get(i) + " " + k);
			}
		}
		for (int i = 0; i < 24; i++) {
			System.out.println(i + " " + stacks.drawingStack.get(i));
		}*/
	}
}

