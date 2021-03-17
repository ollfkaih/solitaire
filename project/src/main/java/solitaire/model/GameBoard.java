package solitaire.model;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import solitaire.model.SolConst.SType;

public class GameBoard {
	//stacks is a map of the boards card stacks: four final stacks, seven play stacks, a deck and a throw stack
	private Map <SolConst.SType, CardContainer> stacks = new TreeMap<>(); 
	
	//Track last move
	private CardContainer lastFromStack;
	private CardContainer lastToStack;
	private int indexOfLastMove;
	
	/**
	 * This constructor initialises a game by putting 1 card in the first play stack,
	 * then 2 in the next and so on until the seventh has six. 
	 * When these 28 cards are dealt, the remaining 24 are left in the deck.
	 * @param deck The deck to use for our game
	 */
	public GameBoard(CardDeck deck) {
		if (deck.size() != SolConst.SUITS * SolConst.CARDSINSUIT) 
			throw new IllegalArgumentException("The deck for our gameboard must have " + SolConst.SUITS * SolConst.CARDSINSUIT + " cards.");
		
		stacks.put(SType.DECK, deck);
		//The throwstack is where we put cards drawn from the deck
		stacks.put(SType.THROWSTACK, new CardStack(SType.THROWSTACK));
		
		//The playstacks are where we put cards for temporary storage while we play the game
		for (int i = SolConst.PLAYSTACKSNUM - 1; i >= 0; i--) {
			CardStack playStack = new CardStack(SType.valueOf("P" + i));
			((CardDeck) stacks.get(SType.DECK)).deal(playStack, i + 1);
			playStack.setHiddenCards(i);
			stacks.put(SType.valueOf("P" + i), playStack);			
		}
		
		//The finalstacks are where we put cards of the same suit from ace to king in order to beat the game
		for (int i = 0; i < SolConst.SUITS; i++) 
			stacks.put(SType.valueOf("F" + i), new CardStack(SType.valueOf("F" + i)));
	}
	
	public GameBoard(Map<SType, CardContainer> map) {
		if (map.size() != 13)
			throw new IllegalArgumentException("The stack map has too many stacks");
		// int totalSize = 0;
		map.entrySet().stream().filter(e -> ! e.getKey().equals(SType.DECK)).forEach(e -> {
			int cardCount = e.getValue().getCardCount();
			// totalSize += cardCount;
			if (cardCount > SolConst.CARDSINSUIT + ((CardStack) e.getValue()).getHiddenCards())
				throw new IllegalArgumentException("The stack map contains stacks that are too big");
		});
		//TODO: Less than 52 cards
		// map.values().stream().collect(Collectors.toCollection(ArrayList::new)).;
		this.stacks = map;
	}

	public CardStack getFinalStack(int i) {
		return (CardStack) stacks.get(SType.valueOf("F" + i));
	}
	
	public CardStack getPlayStack(int i) {
		return (CardStack) stacks.get(SType.valueOf("P" + i));
	}
	
	public CardStack getThrowStack() {
		return (CardStack) stacks.get(SType.THROWSTACK);
	}
	
	private CardDeck getDeck() {
		return (CardDeck) stacks.get(SType.DECK);
	}

	public boolean drawStackEmpty() {
		if (getDeck().isEmpty())
			return true;
		else 
			return false;
	}

	@Override
	public String toString() {
		String game = "";
		for (Map.Entry<SolConst.SType, CardContainer> stack : stacks.entrySet()) {
			String key = stack.getKey().toString();
			if (key.charAt(0) == 'P')
				game += (key + ":" + ((CardStack) stack.getValue()).getHiddenCards() + ":" + stack.getValue() + "\n");
			else 
				game += (key + ":" + ":" + stack.getValue() + "\n");
		}
		return game;
	}
			
	/**
	 * isCardFree is used by legalMove to determine if a card can be moved
	 */
	private boolean isCardFree(Card card, CardStack fromStack) {
		if (!fromStack.contains(card)) 
			throw new IllegalArgumentException("The card " + card + " is not in " + fromStack.toString());
		if (getDeck().contains(card)) {
			System.out.println(getDeck());
			return false;
		} else if (fromStack.getCard(fromStack.getCardCount() - 1).equals(card)) {
			return true;
		} else if (fromStack.getStackName().toString().charAt(0) == 'P') {
			for (int i = fromStack.indexOf(card); i < fromStack.getCardCount() - 1; i++) {
				//Check that the card on top is a different color and face value one less
				//If the cards follow the rules, each switch ends with a break statement, and if we never hit a condition not allowed, we "avoid" 
				//the return statements until the for loop has completed and we end up at the return true statement
				switch (fromStack.getCard(i).getSuit()) {
				case 'D', 'H' -> {
					switch (fromStack.getCard(i + 1).getSuit()) {
					case 'D', 'H' -> {
						return false;
					} 
					case 'S', 'C' -> {
						if (fromStack.getCard(i + 1).getFace() != fromStack.getCard(i).getFace() - 1)
							return false;
						else
							break;
					}
					}
				} case 'S', 'C' -> {
					switch (fromStack.getCard(i + 1).getSuit()) {
					case 'S', 'C' -> {
						return false;
					} 
					case 'D', 'H' -> {
						if (fromStack.getCard(i + 1).getFace() != fromStack.getCard(i).getFace() - 1)
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
		return false;
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
		case  DECK -> { 
				throw new IllegalArgumentException("Cards cannot be moved back to the deck");
			}
		//Applies rules for finalStacks: increasing from ace to king of the same suit
		case F0, F1, F2, F3 -> {
			if (card.getFace() == 1) {
				for (int i = 0; i < SolConst.SUITS; i++) {
					CardContainer currentStack = stacks.get(SType.valueOf("F" + i));
					if (!getFinalStack(i).empty()) {
						if (currentStack.getCard(0).getSuit() == card.getSuit() && currentStack != fromStack) {
							throw new IllegalStateException("The game is in an illegal state: you are attempting to put an ace in a final stack, "
									+ "but there is already a card of the same suit in a final stack.");
						}
					}
				} return true; //No stacks had a card of the same suit, and card is an ace.
			} else {
				for (int i = 0; i < SolConst.SUITS; i++) {
					CardContainer currentStack = stacks.get(SType.valueOf("F" + i));
					if (toStack.equals(currentStack)) {
						if (getFinalStack(i).peek().getSuit() == card.getSuit()) {
							if (getFinalStack(i).peek().getFace() == card.getFace() - 1) {
								return true;
							}
						}
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
			} else if (card.getFace() == toStack.getCard(toStack.size() - 1).getFace() - 1 ) {
				switch (toStack.getCard(toStack.size() - 1).getSuit()) {
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
	 * saveMove updates variables used to undo 
	 */
	private void saveMove(CardContainer from, CardContainer to, int indexInToStack) {
		if (indexInToStack < 0 || indexInToStack > to.getCardCount()) 
			throw new IllegalArgumentException("Index is out of bounds stack " + to + ", index:" + indexInToStack);
		
		lastFromStack = from;
		lastToStack = to;
		indexOfLastMove = indexInToStack;
	}
	
	public void undo() {
		if (lastFromStack == null || lastToStack == null)
			throw new IllegalStateException("The last move is not recorded, or no moves have been made");
		try {
			int tempIndex = lastFromStack.getCardCount();
			if (lastToStack.getClass() == CardStack.class) 
				((CardStack) lastToStack).play(lastFromStack, indexOfLastMove);
			else {
				if (getThrowStack().size() == 0 && getDeck().size() > 0) {
					getDeck().stream().forEach(c -> getThrowStack().addCard(c));
					Collections.reverse(getThrowStack());
					getDeck().clear();
				} else
					((CardDeck) lastToStack).deal((CardStack) lastFromStack, indexOfLastMove);
 			}
			indexOfLastMove = tempIndex;
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("You can only undo the last move");
			}
		
	}
	
	public void redo() {
		if (lastFromStack == null || lastToStack == null)
			throw new IllegalStateException("The last move is not recorded, or no moves have been made");
		if (stacks.get(SType.DECK).equals(lastToStack) || stacks.get(SType.DECK).equals(lastFromStack))
			deal(SolConst.CARDSTODEAL);
		else
			moveCard(indexOfLastMove , (CardStack) lastFromStack, (CardStack) lastToStack);
	}

	/**
	 * moveCard moves the card at position indexOfCard from a stack (param #2) to another (param #3)
	 * given that it is a legal move. It will also move all the cards that are on top of the input card in the fromStack, if any.
	 * @param indexOfCard
	 * @param fromStack
	 * @param toStack
	 */
	public void moveCard(int indexOfCard, CardStack fromStack, CardStack toStack) {
		Card card = fromStack.getCard(indexOfCard);
		if (!legalMove(card, fromStack, toStack)) {
			throw new IllegalArgumentException(String.format("Card: %s in stack %s cannot legally be moved to %s. Input index: %s", card, fromStack, toStack, indexOfCard));
		}
		fromStack.play(toStack, indexOfCard);
		saveMove(fromStack, toStack, toStack.indexOf(card));
	}
			
	/**
	 * deal takes the top n (or the remaining cards if less than n) cards from the drawingStack and puts them on the
	 * throwStack 
	 *
	 */
	public void deal(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("You must deal a positive number of cards");
		if (getDeck().size() == 0) {
			resetDrawStack();
			saveMove(getThrowStack(), getDeck(), 0);
			return;
		}
		getDeck().deal(getThrowStack(), n);
		int index;
		if (getThrowStack().size() <= 3) 
			index = 0;
		else 
			index = getThrowStack().size() - n;
		saveMove(getDeck(), getThrowStack(), index);
	}
	
	/**
	 * resetDrawStack swaps the drawStack and the throwStack, given that drawStack is empty
	 */
	private void resetDrawStack() {
		if (getDeck().size() != 0) {
			throw new IllegalStateException("The drawingStack is not empty, cards should be drawn from it and put on the throwStack first");
		}
		if (getThrowStack().size() > 0) { 
			getDeck().addAll(getThrowStack());
			getThrowStack().clear();
			//Because cards are put "on top" of the throwStack, we want to reverse them back in the drawing stack
			Collections.reverse(getDeck());
		} else if (getThrowStack().size() == 0) {
			return; //Both stacks are empty, so nothing to be done.
		}
	}
	
	/**
	 * Tries to decrement the hiddencards counter of a stack by one
	 * @param cstack
	 */
	public void revealCard(CardStack cstack) {
		if (stacks.entrySet().stream().filter(stack -> stack.getValue().equals(cstack) && stack.getKey().toString().charAt(0) == 'P') != null) {
			cstack.decrementHiddenCards();
			//reveal cannot be undone, setting lastFromStack to null means undo and redo will throw illegal State Exception
			lastFromStack = null;
		}
	}
	
	/**
	 * The isSolved method returns true if the four final stacks are in a solved position (ace-king for all suits),
	 * and no other stacks contains a card. 
	 * @return
	 */
	public boolean isSolved() {
		if (getDeck().size() > 0 || getThrowStack().size() > 0)
			return false;
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++)
			if (getPlayStack(i) != null) 
				if (getPlayStack(i).getCardCount() > 0)
					return false;
		
		String suits = "" ;
		for (int i = 0; i < SolConst.SUITS; i++) {
			char currentSuit = getFinalStack(i).getCard(0).getSuit();
			if (suits.indexOf(currentSuit) == - 1)
				suits += getFinalStack(i).getCard(0).getSuit();
			else 
				return false;
			
			for (int j = 0; j < SolConst.CARDSINSUIT; j++) {
				if ( ! (getFinalStack(i).getCard(j).getFace() == j + 1 && getFinalStack(i).getCard(j).getSuit() == currentSuit)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Loops through all four final stacks to check if a given card can be put there
	 * @param card
	 * @param fromStack
	 */
	public void moveToFinalStacks(Card card, CardStack fromStack) {
		if (!fromStack.contains(card)) throw new IllegalArgumentException("Card is not in given stack");
		for (int i = 0; i < SolConst.SUITS; i++) {
			Card thisStackTopCard = null;
			try {
				thisStackTopCard = this.getFinalStack(i).peek();
			} catch (Exception IllegalArgumentException) {
				if (card.getFace() == 1) {
					moveCard(fromStack.size() - 1, fromStack, getFinalStack(i));
					return;
				}
			}
			if (thisStackTopCard != null) {
				if (thisStackTopCard.getSuit() == card.getSuit()) {
					try {
						moveCard(fromStack.size() - 1, fromStack, getFinalStack(i));
					} catch (IllegalArgumentException e) {
						//Card may not have been one face value higher, do nothing
						e.printStackTrace();
						throw new IllegalArgumentException("Card face is not one more than the card you're attemting to put it on");
					}
				}
			}
		}
	}

	/**
	 * Returns a stack from its stackName
	 * @param stackName
	 * @return
	 */
	public CardContainer getStackbyName(SolConst.SType stackName) {
		return stacks.get(stackName);
	}
}


