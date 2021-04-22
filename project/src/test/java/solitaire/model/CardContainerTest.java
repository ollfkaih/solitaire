package solitaire.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import solitaire.model.SolConst.SType;

public class CardContainerTest {
	private CardStack cardStack, stackWithHiddenCards;
	private Card[] cards;
	private String initialCards = "C2,D3,H13";

	@BeforeEach
	public void setup() {
		cards = new Card[] {
				new Card('C', 2), new Card('D', 3), new Card('H', 13) 
		};
		cardStack = new CardStack(SType.P0, cards);
		stackWithHiddenCards = new CardStack(SType.P1, 3, cards);
	}

	@Test
	@DisplayName("Test that CardStack constructors works correctly")
	public void testStackConstructor() {
		CardStack stack;
		assertThrows(IllegalArgumentException.class, () -> new CardStack(null));
		for (SType stackType: SType.values()) {
			if (stackType.equals(SType.DECK))
				break;
			stack = new CardStack(stackType);
			assertEquals(stackType, stack.getStackName());
		}
		for (Card card: cards) {
			assertTrue(cardStack.contains(card), "card should have been put in stack");
			cardStack.remove(card);
			assertFalse(cardStack.contains(card), "card should have been removed from stack");
		}
		assertEquals(0, cardStack.size(), "Stack should have been emptied");
		assertEquals(3, stackWithHiddenCards.getHiddenCards(), "We set hiddenCards to 3");
		assertEquals(SType.P1, stackWithHiddenCards.getStackName(), "We set this stacktype to P1");
		assertThrows(IllegalArgumentException.class, () -> new CardStack(SType.F0, 4, cards));
		assertThrows(IllegalArgumentException.class, () -> new CardStack(SType.F0, -1, cards));
		assertThrows(IllegalArgumentException.class, () -> new CardStack(SType.DECK));
	}

	@Test
	@DisplayName("Test that CardDeck constructors works correctly")
	public void testDeckConstructor() {
		CardDeck deck;
		assertThrows(IllegalArgumentException.class, () -> new CardStack(null));
		for (int i = 1; i <= 13; i++) {
			deck = new CardDeck(i);
			var faceValue = i;
			assertEquals(4, deck.getCardCount(card -> card.getFace() == faceValue), "There should be 4 cards of the biggest face value");
			assertEquals(4*i, deck.getCardCount(), "There should be " + 4*i + " cards in this deck, actually was " + deck.getCardCount());
		}
		deck = new CardDeck(stackWithHiddenCards);
		assertEquals(3, deck.getCardCount());
		assertEquals(SType.DECK, deck.getStackName());
		assertEquals(initialCards, deck.toString());
	}

	@Test
	@DisplayName("Test that the CardDeck gets shuffled")
	public void testShuffle(){
		CardDeck deck = new CardDeck(stackWithHiddenCards);
		assertEquals(initialCards, deck.toString());
		int notChangedCounter = 0;
		int loops = 1000000;
		for (int i = 0; i < loops; i++) {
			deck.shuffle();
			if (deck.toString().equals(initialCards))
				notChangedCounter++;
			assertTrue(deck.getCardCount() == 3);
		}
		assertTrue(notChangedCounter < loops / 3d);
	}
	
	@Test
	@DisplayName("Test that decrementHiddenCards only lets us reveal the topmost hidden card, and keeps counter positive")
	public void testDecrementHiddenCards() {
		stackWithHiddenCards.decrementHiddenCards();
		assertThrows(IllegalStateException.class, () -> stackWithHiddenCards.decrementHiddenCards(), "Cannot reveal more than the top card of stack");
		stackWithHiddenCards.remove(2);
		stackWithHiddenCards.decrementHiddenCards();
		stackWithHiddenCards.remove(1);
		stackWithHiddenCards.decrementHiddenCards();
		stackWithHiddenCards.remove(0);
		assertThrows(IllegalStateException.class, () -> stackWithHiddenCards.decrementHiddenCards(), "HiddenCards should stay non-negative");
	}

	@Test
	@DisplayName("Tests that we can add cards to stacks and decks")
	public void testAddCards() {
		cardStack.add(new Card('C', 12));
		assertEquals(initialCards + ",C12", cardStack.toString());
		CardDeck deck = new CardDeck(cardStack);
		deck.add(new Card('D', 5));
		assertEquals(initialCards + ",C12,D5", deck.toString());
	}

	@Test
	@DisplayName("Tests that the deck deals cards correctly")
	public void testDealCards() {
		CardDeck deck = new CardDeck(6);
		assertThrows(IllegalArgumentException.class, () -> deck.deal(cardStack, -1));
		Card firstTopCard = deck.peek();
		Card nextToBottomCard = deck.get(1);
		Card bottomCard = deck.get(0);
		deck.deal(cardStack, 1);
		assertEquals(6*4 - 1, deck.getCardCount(), "We should have 23 cards left in deck");
		assertEquals(initialCards + "," + firstTopCard.toString(), cardStack.toString(), "The cardStack should have been dealt the top card of the deck");
		deck.deal(cardStack, 22);
		assertEquals(bottomCard, deck.peek(), "The deck should only have the bottom card left");
		assertEquals(nextToBottomCard, cardStack.get(4), "The card just above the bottom card of the deck should have been dealt first to the cardstack.");
		deck.deal(cardStack, 1);
		assertEquals(3 + 24, cardStack.getCardCount());
		assertTrue(deck.isEmpty());
		assertThrows(IllegalStateException.class, () -> deck.deal(cardStack, 1), "Cannot deal cards from empty stack");
	}

	@Test
	@DisplayName("Test playing cards from a CardStack")
	public void testPlayCards() {
		Card topCard = cardStack.peek();
		cardStack.play(stackWithHiddenCards, 2);
		assertEquals(2, cardStack.getCardCount());
		assertEquals(4, stackWithHiddenCards.getCardCount());
		assertEquals(topCard, stackWithHiddenCards.peek());
		assertFalse(cardStack.contains(topCard));

		String twoRemainingCardStack = cardStack.toString();
		cardStack.play(stackWithHiddenCards, 0);
		assertTrue(cardStack.isEmpty());
		assertEquals(6, stackWithHiddenCards.getCardCount());
		assertEquals(initialCards + "," + topCard + "," + twoRemainingCardStack, stackWithHiddenCards.toString());

		stackWithHiddenCards.play(cardStack, 3);
		for (Card card : cards)
			assertTrue(cardStack.contains(card));
	}

	@Test
	@DisplayName("Test illegal play moved from a CardStack")
	public void testIllegalPlayCards() {
		assertThrows(IndexOutOfBoundsException.class, () -> cardStack.play(stackWithHiddenCards, -1));
		assertThrows(IndexOutOfBoundsException.class, () -> cardStack.play(stackWithHiddenCards, 10));
		assertThrows(IllegalArgumentException.class, () -> cardStack.play(cardStack, 0), "Cannot play cards to itself");
		cardStack.play(stackWithHiddenCards, 0);
		assertThrows(IndexOutOfBoundsException.class, () -> cardStack.play(stackWithHiddenCards, 0));
	}
	@Test
	@DisplayName("Test toString() for both CardContainers")
	public void testToString() {
		CardStack firstStack = new CardStack(SType.F3);
		assertEquals("", firstStack.toString());
		firstStack.add(new Card('D',3));
		assertEquals("D3", firstStack.toString());
		CardStack newStack = new CardStack(SType.P0, new Card[] {new Card('H', 13), new Card('H',12)});
		assertEquals("H13,H12", newStack.toString());
		CardDeck deck = new CardDeck(13);
		String allCards = allCardsAsString();
		assertEquals(allCards, deck.toString());
	}

	private String allCardsAsString() {
		String allCards = "";
		for (char suit: "SHDC".toCharArray())
			for (int i = 1; i <= 13; i++)
				allCards += suit + "" + i + ",";
		allCards = allCards.substring(0, allCards.length() - 1); //remove trailing comma added above
		return allCards;
	}
}