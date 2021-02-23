package solitaire.model;

import java.io.Console;
import java.util.*;

public class CardStacks {
	public final static int PLAYSTACKSNUM = 7; // Seven play stacks
	public final static int SUITS = 4;
	public final static int CARDSINSUITE = 13;
//	private final static int PLAYSTACKSSUM = 28; // Seven cards in the seventh position, six ... gives  7+6+5+4+3+2+1=28
	
	private Card[][] finalStacks = new Card[SUITS][CARDSINSUITE]; //four final stacks 
	private List<Card>[] playStacks = (List<Card>[]) new ArrayList[PLAYSTACKSNUM]; //triangular playing stacks for temporary placement of cards
	private List<Card> drawingStack = new ArrayList<Card>(); //the stack cards are drawn from, three by three
	private List<Card> throwStack = new ArrayList<Card>(); //The stack of drawn cards next to the drawingStack
	private int[] hiddenPlayCards = new int[PLAYSTACKSNUM]; //Number of hidden cards in the nth playStack //TODO: make use of
			
	public Card[][] getFinalStacks() {
		return finalStacks;
	}
	public List<Card>[] getPlayStacks() {
		return playStacks;
	}
	public List<Card> getThrowStack() {
		return throwStack;
	}
	//The drawingStack should be secret 
	/*public List<Card> getDrawingStack() {
	return drawingStack;
	}*/
	
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
		for (int i = PLAYSTACKSNUM - 1; i >= 0; i--) {
			for (int j = PLAYSTACKSNUM; j >= PLAYSTACKSNUM - i; j--) {
				playStacks[i].add(deck.getCard(pos));
				hiddenPlayCards[i] = i;
				pos++;
			}
		}
		for (; pos < deckLength; pos++) {
			drawingStack.add(deck.getCard(pos));
		}
		
	}
	//TODO: REMOVE
	/**
	 * NOT FOR PRODUCTION; TESTING ONLY
	 * @param deck
	 * @param cheat
	 */
	public CardStacks(CardDeck deck, String cheat) {
		if (cheat == "CHEATER") {
			for (int i = 0; i < SUITS; i++) {
				char suit;
				switch (i) {
				case 0: {
					suit = 'S';
					break;
				}case 1: {
					suit = 'D';
					break;
				}case 2: {
					suit = 'H';
					break;
				}case 3: {
					suit = 'C';
					break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + i);
				}
				for (int j = 0; j < CARDSINSUITE; j++) {
					this.finalStacks[i][j] = new Card(suit, j + 1);
				}
				//this.finalStacks[0][0] = new Card('H', 7);
			}
		} else {
			throw new IllegalArgumentException("CardStacks was called with too many arguments.");
		}
	}
	
	/**
	 * isCardFree is used by legalMove to determine if a card is on top of a stack (except drawingStack)
	 * and therefore free to be moved
	 */
	private boolean isCardFree(Card Card, List<Card> fromStack) {
		if (this.drawingStack.contains(Card))
			return false;
		else if (fromStack.get(fromStack.size() - 1) == Card) {
			return true;
		}
		return false;
	}
	
	/**
	 * Legalmove(Card, List) takes in a card and a list argument.
	 * It checks if the Card is free and if it can be moved on top of the list, which should be a in the playStacks array.
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
	 * It checks if the Card is free and if it can be moved on top of the list, which should be a in the finalStacks array.
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
	
	
	/**moveCard(Card, fromStack, List toStack) moves a card to a playstack after checking with legalMove.
	 * 
	 */
	private void moveCard(Card topCard, List<Card> fromStack, List<Card> toStack) {
		//System.out.println(topCard + " " + toStack);
		if (!legalMove(topCard, fromStack, toStack)) {
			System.out.println(topCard + " " + fromStack + " " + toStack);
			throw new IllegalArgumentException("The card " + topCard + " cannot be moved on top of " + toStack.get(toStack.size() - 1));
		}
		else {
			//Reveals a playCard when a card is moved from that stack.
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].contains(topCard) && this.hiddenPlayCards[i] > 0) {
					this.hiddenPlayCards[i]--;
				}
			}
			if (this.throwStack.size() > 0) {
				if (this.throwStack.get(this.throwStack.size() - 1) == topCard) {
					this.throwStack.remove(this.throwStack.size() - 1);
				}
			}
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].get(this.playStacks[i].size() - 1) == topCard) {
					this.playStacks[i].remove(this.playStacks[i].size() - 1);
				}
			}
			toStack.add(topCard);
		}
	}
	
	/**moveCard(Card, fromStack, array toStack) moves a card to a finalStack after checking with legalMove.
	 * 
	 */
	private void moveCard(Card topCard, List<Card> fromStack, Card[] toStack) {
		//System.out.println(topCard + " " + toStack);
		if (!legalMove(topCard, fromStack, toStack))
			return;
		else {
			//Reveals a playCard when a card is moved from that stack.
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].contains(topCard) && this.hiddenPlayCards[i] > 0) {
					this.hiddenPlayCards[i]--;
				}
			}
			int j = - 1;
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
			if (this.throwStack.size() > 0) {
				if (this.throwStack.get(this.throwStack.size() - 1) == topCard) {
					this.throwStack.remove(this.throwStack.size() - 1);
				}
			}
			for (int i = 0; i < PLAYSTACKSNUM; i++) {
				if (this.playStacks[i].size() > 0) {
					if (this.playStacks[i].get(this.playStacks[i].size() - 1) == topCard) {
						this.playStacks[i].remove(this.playStacks[i].size() - 1);
					}
				}
			}
		}
	}
	
	/**
	 * moveCard(Card,from stackshortname, to stackshortname) calls the appropriate moveCard depending on the thrird argument: the stack to move to
	 * The stackshortname is either t for throwstack, f[0-3] (e.g. f2) for the final stacks or p[0-6] for the playStacks.
	 */
	public void moveCard(Card card, String from, String to) {
		List<Card> fromStack;
		switch (from.charAt(0)) {
		case 't' -> {
			fromStack = this.throwStack; 
			break;
		}
		case 'p' -> {
			if ("0123456".indexOf(from.charAt(1)) == -1) {
				throw new IllegalArgumentException("Bad stack number given, playStack only has 7 stacks.");
			} else {
				fromStack = this.playStacks[Character.getNumericValue(from.charAt(1))];
			}
		}
		default ->
		throw new IllegalArgumentException("Unexpected value: " + from.charAt(0));
		}
		
		switch (to.charAt(0)) {
		case 'p' -> {
			if ("0123456".indexOf(from.charAt(1)) == -1) {
				throw new IllegalArgumentException("Bad stack number given, playStack only has 7 stacks.");
			} else {
				List<Card> toStack = this.playStacks[Character.getNumericValue(to.charAt(1))];
				moveCard(card, fromStack, toStack);
				break;
			}
		}
		case 'f' -> {
			if ("0123".indexOf(from.charAt(1)) == -1) {
				throw new IllegalArgumentException("Bad stack number given, finalStacks only has 4 stacks.");
			} else {
				Card[] toStack = this.finalStacks[Character.getNumericValue(to.charAt(1))];
				moveCard(card, fromStack, toStack);
				break;
			}
		}
		default ->
		throw new IllegalArgumentException("Unexpected value: " + to);
		}
				
	}
	
	/**
	 * deal takes the top n (or the remaining cards if less than n) cards from the drawingStack and puts them on the
	 * throwStack 
	 */
	public void deal(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("You must deal a positive number of cards");
		if (drawingStack.size() == 0) {
			throw new IllegalStateException("The drawingStack is empty, put the throwStack back onto it first"); //TODO: catch this when called
		}
		for (int i = 1; i <= n && drawingStack.size() > 0; i++ ) {
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
	
	/**
	 * The isSolved method returns true if the four final stacks are in a solved position (ace-king for all suits),
	 * and no other stacks contains a card. 
	 * @return
	 */
	public boolean isSolved() {
		if (this.drawingStack.size() > 0 || this.throwStack.size() > 0)
			return false;
		for (int i = 0; i < PLAYSTACKSNUM; i++)
			if (this.playStacks[i] != null) 
				if (this.playStacks[i].size() > 0)
					return false;
		
		String suits = "" ;
		for (int i = 0; i < SUITS; i++) {
			char currentSuit = this.finalStacks[i][0].getSuit();
			if (suits.indexOf(currentSuit) == - 1)
				suits += this.finalStacks[i][0].getSuit();
			else 
				return false;
			
			for (int j = 0; j < CARDSINSUITE; j++) {
				if ( ! (this.finalStacks[i][j].getFace() == j + 1 && this.finalStacks[i][j].getSuit() == currentSuit)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		CardDeck deck = new CardDeck(13);
		for (int i = 0; i < 1; i++) {
			deck.shufflePerfectly();
		}
		CardStacks stacks = new CardStacks(deck);
		for (int i = 0; i < 4; i++)
			;
			//stacks.dealThree();
		//stacks.resetDrawStack();
		//System.out.println(stacks.playStacks[1].get(1) + " " + stacks.playStacks[1] + " " + stacks.finalStacks[0][0]);
		
		stacks.moveCard(stacks.playStacks[1].get(1), stacks.playStacks[1], stacks.finalStacks[0]);
		System.out.println(stacks.isSolved());
		
	}
}

