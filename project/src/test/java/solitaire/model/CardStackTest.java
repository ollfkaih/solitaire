
package solitaire.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import solitaire.model.SolConst.SType;

public class CardStackTest {
	private CardStack cardStack, stackWithHiddenCards;
	private Card[] cards;

	@BeforeEach
	public void setup() {
		cards = new Card[] {
				new Card('C', 2), new Card('D', 3), new Card('H', 13) 
		};
		cardStack = new CardStack(SType.P0, cards);
		stackWithHiddenCards = new CardStack(SType.P1, 3, cards);
	}

	@Test
	@DisplayName("Test that constructor works correctly")
	public void testStackConstructor() {
		CardStack stack;
		assertThrows(IllegalArgumentException.class, () -> new CardStack(null));
		for (SType stackType: SType.values()) {
			stack = new CardStack(stackType);
			assertEquals(stackType, stack.getStackName());
		}
		for (Card card: cards) {
			assertEquals(true, cardStack.contains(card), "card should have been put in stack");
			cardStack.remove(card);
			assertEquals(false, cardStack.contains(card), "card should have been removed from stack");
		}
		assertEquals(0, cardStack.size(), "Stack should have been emptied");
		assertEquals(3, stackWithHiddenCards.getHiddenCards(), "We set hiddenCards to 3");
		assertThrows(IllegalArgumentException.class, () -> new CardStack(SType.F0, 4, cards));
		assertThrows(IllegalArgumentException.class, () -> new CardStack(SType.F0, -1, cards));
	}
	
	@Test
	@DisplayName("Test that decrementHiddenCards only lets us reveal the topmost hidden card, and keeps counter positive")
	public void testDecrementHiddenCards() {
		stackWithHiddenCards.decrementHiddenCards();
		assertThrows(IllegalArgumentException.class, () -> stackWithHiddenCards.decrementHiddenCards(), "Cannot reveal more than the top card of stack");
		stackWithHiddenCards.remove(2);
		stackWithHiddenCards.decrementHiddenCards();
		stackWithHiddenCards.remove(1);
		stackWithHiddenCards.decrementHiddenCards();
		stackWithHiddenCards.remove(0);
		assertThrows(IllegalStateException.class, () -> stackWithHiddenCards.decrementHiddenCards(), "HiddenCards should stay non-negative");
	}
}