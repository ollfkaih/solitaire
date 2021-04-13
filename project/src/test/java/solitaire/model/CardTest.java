package solitaire.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CardTest {

	//THE FIRST PART IS BASED ON CODE FROM THE EXCERCISES, AS THE CARD CLASS IS ALSO BASED ON EXCERCISE CODE
	
	private void checkCard(Card card, char suit, int face) {
		Assertions.assertEquals(card.getSuit(), suit);
		Assertions.assertEquals(card.getFace(), face);
	}

	@Test
	@DisplayName("Checks that the constructor creates cards with legal values")
	public void testConstructor() {
		checkCard(new Card('S', 1), 'S', 1);
		checkCard(new Card('S', 13), 'S', 13);
		checkCard(new Card('H', 1), 'H', 1);
		checkCard(new Card('H', 13), 'H', 13);
		checkCard(new Card('D', 1), 'D', 1);
		checkCard(new Card('D', 13), 'D', 13);
		checkCard(new Card('C', 1), 'C', 1);
		checkCard(new Card('C', 13), 'C', 13);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new Card('E', 1);
		}, "Creating a card of suit E should not be possible");

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new Card('S', 0);
		}, "Creating a card with face value 0 should not be possible");

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new Card('C', 14);
		}, "Creating a card with face value 14 should not be possible");
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new Card('C', -2);
		}, "Creating a card with face value -2 should not be possible");
	}

	@Test
	@DisplayName("Checks that toString() returns expected value")
	public void testToString() {
		Assertions.assertEquals("S6", new Card('S', 6).toString());
		Assertions.assertEquals("D12", new Card('D', 12).toString());
		Assertions.assertEquals("C1", new Card('C', 1).toString());
	}
	
	@Test
	public void testEquals() {
		Card c13 = new Card('C', 13);
		Card c12 = new Card('C', 12);
		Card d1 = new Card('D', 1);
		Card anotherc13 = new Card('C', 13);
		Assertions.assertFalse(c13.equals(c12));
		Assertions.assertFalse(c13.equals(d1));
		Assertions.assertFalse(d1.equals(anotherc13));
		Assertions.assertTrue(c13.equals(anotherc13));
		Assertions.assertTrue(c13.equals(c13));
		
	}
	
}
