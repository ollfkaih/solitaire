package solitaire.model;

import java.util.Collections;

import solitaire.model.SolConst.*;

public class GameBoard {
	private CardStack[] finalStacks = new CardStack[SolConst.SUITS]; //four final stacks 
	private CardStack[] playStacks = new CardStack[SolConst.PLAYSTACKSNUM]; //triangular playing stacks for temporary placement of cards
	private CardStack drawingStack = new CardStack(Stack.DECK); //the stack cards are drawn from, three by three
	private CardStack throwStack = new CardStack(Stack.THROWSTACK); //The stack of drawn cards next to the drawingStack
			
	public CardStack getFinalStack(int i) {
		return finalStacks[i];
	}
	
	public CardStack getPlayStack(int i) {
		return playStacks[i];
	}
	
	public CardStack getThrowStack() {
		return throwStack;
	}

	public boolean drawStackEmpty() {
		if (drawingStack.isEmpty())
			return true;
		else 
			return false;
	}
	
	/**
	 * This constructor takes a deck argument and initialises a game by putting 1 card in the first play stack,
	 * then 2 in the next and so on until the seventh has six. 
	 * When these 28 cards are dealt, the remaining 24 are put into the drawing stack.
	 * @param deck 
	 */
	public GameBoard(CardDeck deck) {
		
		int pos = 0; //Keeps track of how many cards we have drawn
		for (int i = SolConst.PLAYSTACKSNUM - 1; i >= 0; i--) {
			playStacks[i] = new CardStack(Stack.valueOf("P" + i));
			deck.deal(playStacks[i], i + 1);
			pos += i + 1;
			playStacks[i].setHiddenCards(i);
		}
		
		deck.deal(drawingStack, SolConst.CARDSINSUIT * SolConst.SUITS - pos);
		
		for (int i = 0; i < SolConst.SUITS; i++)
			finalStacks[i] = new CardStack(Stack.valueOf("F" + i));
	}
		
	/**
	 * isCardFree is used by legalMove to determine if a card can be moved
	 */
	private boolean isCardFree(Card card, CardStack fromStack) {
		if (!fromStack.contains(card)) 
			throw new IllegalArgumentException("The card " + card + " is not a part of " + fromStack.toString());
		if (drawingStack.contains(card)) {
			System.out.println(drawingStack);
			return false;
		} else if (fromStack.get(fromStack.getCardCount() - 1).equals(card)) {
			return true;
		} else {
			for (int i = fromStack.indexOf(card); i < fromStack.getCardCount() - 1; i++) {
				//Check that the card on top is a different color and face value one less
				//If the cards follow the rules, each switch ends with a break statement, and if we never hit a condition not allowed, we "avoid" 
				//the return statements until the for loop has completed and we end up at the return true statement
				switch (fromStack.get(i).getSuit()) {
				case 'D', 'H' -> {
					switch (fromStack.get(i + 1).getSuit()) {
					case 'D', 'H' -> {
						return false;
					} 
					case 'S', 'C' -> {
						if (fromStack.get(i + 1).getFace() != fromStack.get(i).getFace() - 1)
							return false;
						else
							break;
					}
					}
				} case 'S', 'C' -> {
					switch (fromStack.get(i + 1).getSuit()) {
					case 'S', 'C' -> {
						return false;
					} 
					case 'D', 'H' -> {
						if (fromStack.get(i + 1).getFace() != fromStack.get(i).getFace() - 1)
							return false;
						else
							break;
					}
					}
				}
				}	
			} 
			return true;
		}
	}
	/**
	 * Legalmove(Card, CardStack, CardStack) checks if the card is free (with isCardFree(card, CardStack))
	 * and applies the appropriate rules of Solitaire for what cards are allowed to be put on what other cards
	 * and in what order.
	 * @param card
	 * @param fromStack
	 * @param toStack
	 * @return 
	 */
	private boolean legalMove(Card card, CardStack fromStack, CardStack toStack) {
		if (!isCardFree(card, fromStack)) {
			return false;
		}
		
		//This switch is the main reason for stacknames
		switch (toStack.getStackName()) {
		case DECK -> { 
				throw new IllegalArgumentException("Cards cannot be moved back to the deck");
			}
		//Applies rules for finalStacks: increasing from ace to king of the same suit
		case F0, F1, F2, F3 -> {
			if (card.getFace() == 1) {
				for (CardStack currentStack : this.finalStacks) {
					if (!currentStack.empty()) {
						if (currentStack.get(0).getSuit() == card.getSuit() && currentStack != fromStack) {
							throw new IllegalStateException("The game is in an illegal state: you are attempting to put an ace in a final stack, "
									+ "but there is already a card of the same suit in a final stack.");
						}
					}
				} return true; //No stacks had a card of the same suit, and card is an ace.
			} else {
				int i = 0;
				for (CardStack currentStack : this.finalStacks) {
					if (toStack.equals(currentStack)) {
						if (getFinalStack(i).peek().getSuit() == card.getSuit()) {
							if (getFinalStack(i).peek().getFace() == card.getFace() - 1) {
								return true;
							}
						}
					} else {
						i++;
					}
				}
			}
			return false;
		}
		//Applies rules for playStacks: decreasing from king to ace of alernating colors 
		//Diamonds or hearts (red) go on spades or clubs (black)
		case P0, P1, P2, P3, P4, P5, P6 -> {
			if (card.getFace() == 13 && toStack.size() == 0) {
				return true;
			} else if (card.getFace() == toStack.get(toStack.size() - 1).getFace() - 1 ) {
				switch (toStack.get(toStack.size() - 1).getSuit()) {
				case 'S','C' -> {
					if ("DH".indexOf(card.getSuit()) != -1) {
						return true;
					}
					break;
				}
				case 'D','H' -> {
					if ("SC".indexOf(card.getSuit()) != -1) {
						return true;
					}
					break;
				}
				}
			}
			return false;
		}
		case THROWSTACK -> {
			throw new IllegalArgumentException("Cards cannot be moved to the throwStack");
		}
		
		}
		throw new IllegalStateException(String.format("How did we get here? card: %s fromStack: %s toStack: %s", card, fromStack, toStack));
		//return false;
	}

	/**
	 * moveCard moves a the card at position indexOfCard from a stack (param #2) to another (param #3)
	 * given that it is a legal move. It will also move the cards that are on top of the input card in the fromStack, if any.
	 * @param indexOfCard
	 * @param fromStack
	 * @param toStack
	 */
	public void moveCard(int indexOfCard, CardStack fromStack, CardStack toStack) {
		Card card = fromStack.get(indexOfCard);
		if (!legalMove(card, fromStack, toStack)) {
			throw new IllegalArgumentException(String.format("Card: %s in stack %s cannot legally be moved to %s. Input index: %s", card, fromStack, toStack, indexOfCard));
		}
		fromStack.play(toStack, indexOfCard);
	}
			
	/**
	 * deal takes the top n (or the remaining cards if less than n) cards from the drawingStack and puts them on the
	 * throwStack 
	 *
	 */
	public void deal(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("You must deal a positive number of cards");
		if (drawingStack.size() == 0) {
			resetDrawStack();
			return;
		}
		int drawStart;
		if (drawingStack.getCardCount() >= n) {
			drawStart = drawingStack.getCardCount() - n;
		} else {
			drawStart = 0;
		}
		drawingStack.play(throwStack, drawStart);
	}
	
	/**
	 * resetDrawStack swaps the drawStack and the throwStack, given that drawStack is empty
	 */
	private void resetDrawStack() {
		if (drawingStack.size() != 0) {
			throw new IllegalStateException("The drawingStack is not empty, cards should be drawn from it and put on the throwStack first");
		}
		if (throwStack.size() > 0) { 
			drawingStack.addAll(throwStack);
			throwStack.clear();
			//Because cards are put "on top" of the throwStack, we want to reverse them back in the drawing stack
			Collections.reverse(drawingStack);
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
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++)
			if (this.playStacks[i] != null) 
				if (this.playStacks[i].getCardCount() > 0)
					return false;
		
		String suits = "" ;
		for (int i = 0; i < SolConst.SUITS; i++) {
			char currentSuit = this.finalStacks[i].get(0).getSuit();
			if (suits.indexOf(currentSuit) == - 1)
				suits += this.finalStacks[i].get(0).getSuit();
			else 
				return false;
			
			for (int j = 0; j < SolConst.CARDSINSUIT; j++) {
				if ( ! (this.finalStacks[i].get(j).getFace() == j + 1 && this.finalStacks[i].get(j).getSuit() == currentSuit)) {
					return false;
				}
			}
		}
		return true;
	}

	public void moveToFinalStacks(Card card, CardStack fromStack) {
		if (!fromStack.contains(card)) throw new IllegalArgumentException("Card is not in given stack");
		for (int i = 0; i < SolConst.SUITS; i++) {
			Card thisStackTopCard = null;
			try {
				thisStackTopCard = this.getFinalStack(i).peek();
			} catch (Exception IllegalArgumentException) {
				if (card.getFace() == 1)  
					moveCard(fromStack.size() - 1, fromStack, finalStacks[i]);
				return;
			}
			if (thisStackTopCard.getSuit() == card.getSuit()) {
				try {
					moveCard(fromStack.size() - 1, fromStack, finalStacks[i]);
				} catch (IllegalArgumentException e) {
					//Card may not have been one face value higher, do nothing
					e.printStackTrace();
					throw new IllegalArgumentException("Card face is not one more than the card you're attemting to put it on");
				}
			}
		}
	}

	/**
	 * Returns a stack from its stackName
	 * @param stackName
	 * @return
	 */
	public CardStack getStackbyName(SolConst.Stack stackName) {
		for (CardStack stack: this.playStacks) {
			if (stack.getStackName().equals(stackName))
				return stack;
		}
		for (CardStack stack: this.finalStacks)
			if (stack.getStackName().equals(stackName))
				return stack;
		if (this.drawingStack.getStackName().equals(stackName))
			return this.drawingStack;
		if (this.throwStack.getStackName().equals(stackName))
			return this.throwStack;
		return null;
	}
	
	/*public static void main(String[] args) {
		Card card = new Card('D',2);
		//System.out.println(Card.Stack.valueOf("P" + 0));
		CardDeck deck = new CardDeck(13);
		for (int i = 0; i < 1; i++) {
			deck.shufflePerfectly();
		}
		GameBoard stacks = new GameBoard(deck);
		for (int i = 0; i < 4; i++)
			System.out.println(stacks.getFinalStack(i).getStackName());
			//stacks.dealThree();
		//stacks.resetDrawStack();
		//System.out.println(stacks.playStacks[1].get(1) + " " + stacks.playStacks[1] + " " + stacks.finalStacks[0][0]);
		
		//stacks.moveCard(stacks.playStacks[1].get(1), stacks.playStacks[1], stacks.finalStacks[0]);
		//System.out.println(stacks.isSolved());
		
	}*/
	//TODO: REMOVE
		/**
		 * NOT FOR PRODUCTION; TESTING ONLY
		 */
		/*public GameBoard(CardDeck deck, String cheat) {
			if (cheat == "CHEATER") {
				for (int i = 0; i < SolConst.SUITS; i++) {
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
					for (int j = 0; j < SolConst.CARDSINSUITE; j++) {
						//this.finalStacks[i][j] = new Card(suit, j + 1);
					}
					//this.finalStacks[0][0] = new Card('H', 7);
				}
			} else {
				throw new IllegalArgumentException("GameBoard was called with too many arguments.");
			}
		}*/
}


