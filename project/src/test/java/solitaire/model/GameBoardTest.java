package solitaire.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import solitaire.model.SolConst.SType;

public class GameBoardTest {
    
    private String gameBoardString = "P0:0:H12\nP1:1:H13,D1\nP2:2:D2,D3,D4\nP3:3:D5,D6,D7,D8\nP4:4:D9,D10,D11,D12,D13\nP5:5:C1,C2,C3,C4,C5,C6\nP6:6:C7,C8,C9,C10,C11,C12,C13\nF0::\nF1::\nF2::\nF3::\nDECK::S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,S11,S12,S13,H1,H2,H3,H4,H5,H6,H7,H8,H9,H10,H11\nTHROWSTACK::\n";
    private CardDeck deck;
    private GameBoard board;

	@BeforeEach
    private void setup() {
        deck = new CardDeck(13);
        board = new GameBoard(deck);
    }
	
    @Test
    @DisplayName("Tests the GameBoard constructor and toString")
    void testConstructor() {
        CardDeck cleanDeck = new CardDeck(13);
        for (int i = 0; i < 4; i++) {
            assertTrue(board.getFinalStack(i).isEmpty());
        }
        CardStack seventhStack = new CardStack(SType.P6);
        cleanDeck.deal(seventhStack, 7);
        assertEquals(seventhStack.toString(), board.getPlayStack(6).toString());
        for (int i = 0; i < 7; i++) {
            assertEquals(i + 1, board.getPlayStack(i).getCardCount());
        }
        assertTrue(board.getThrowStack().isEmpty());
        assertEquals(gameBoardString, board.toString());
    }

    @Test
    @DisplayName("Test moving cards")
    void testMoveCards() {
        assertTrue(board.getPlayStack(0).get(0).equals(new Card('H', 12)));
        board.moveCard(0, board.getPlayStack(0), board.getPlayStack(6));
        assertTrue(board.getPlayStack(0).isEmpty());
        assertTrue(board.getPlayStack(6).peek().equals(new Card('H', 12)));
        assertThrows(IllegalArgumentException.class, () -> board.moveCard(0, board.getPlayStack(0), board.getPlayStack(2)));
        assertThrows(IllegalArgumentException.class, () -> board.moveCard(5, board.getPlayStack(2), board.getPlayStack(2)));
        assertThrows(IllegalArgumentException.class, () -> board.moveCard(3, board.getPlayStack(4), board.getPlayStack(2)));

        for (int i = 0; i < 3; i++)
            board.deal(3);
        assertTrue(board.getThrowStack().peek().equals(new Card('H', 5)));
        board.moveCard(board.getThrowStack().size() - 1, board.getThrowStack(), board.getPlayStack(5));
        assertTrue(board.getPlayStack(5).peek().equals(new Card('H', 5)));
        assertThrows(IllegalArgumentException.class, () -> board.moveCard(board.getThrowStack().size() - 1, board.getThrowStack(), board.getFinalStack(0)));
    }

    @Test
    @DisplayName("Test dealing cards from the deck")
    void testDeal() {
        assertEquals(52 - 28, board.deal(300), "Dealing more cards than there are simply deals all of them");
        assertEquals(52 - 28, board.getThrowStack().getCardCount());
        assertEquals(-1, board.deal(1));
        assertEquals(0, board.getThrowStack().getCardCount());
        assertEquals(3, board.deal(3));
        assertEquals(3, board.getThrowStack().getCardCount());
        assertEquals("S1,S2,S3", board.getThrowStack().toString());
    }

    @Test
    @DisplayName("Test undo")
    void testUndo() {
        assertThrows(IllegalStateException.class, () -> board.undo(), "You cannot undo before you have made a move");
        String beforeString = board.toString();
        board.deal(3);
        board.undo();
        assertEquals(beforeString, board.toString());
        assertThrows(IllegalStateException.class, () -> board.undo(), "Game should only allow undoing one move");
        board.moveCard(0, board.getPlayStack(0), board.getPlayStack(6));
        board.undo();
        assertEquals(beforeString, board.toString(), "Moving a card and undoing should revert board");
        for (int i = 0; i < 8; i++)
            board.deal(3);
        String fullThrowStack = board.getThrowStack().toString();
        String[] allCardsInThrowStack = fullThrowStack.split(",");
        String throwStackAfterUndo = "";
        for (int i = 0; i < board.getThrowStack().size() - 3; i++) {
            throwStackAfterUndo += "," + allCardsInThrowStack[i];
        }
        throwStackAfterUndo = throwStackAfterUndo.substring(1); //remove leading comma
        board.undo();
        assertEquals(throwStackAfterUndo, board.getThrowStack().toString());
        board.deal(3);
        assertEquals(fullThrowStack, board.getThrowStack().toString(), "Throwstack should be the same after undoing and dealing again");
        board.deal(3);
        assertEquals(beforeString, board.toString(), "Game should be in original state after dealing all cards");
    }

    @Test
    @DisplayName("Test redo")
    void testRedo() {
        board.deal(3);
        String beforeString = board.toString();
        board.undo();
        board.redo();
        assertEquals(beforeString, board.toString());
        board.undo();
        board.moveCard(0, board.getPlayStack(0), board.getPlayStack(6));
        assertNotEquals(beforeString, board.toString());
        beforeString = board.toString();
        board.undo();
        board.redo();
        assertEquals(beforeString, board.toString(), "Redo should revert an undo");
        for (int i = 0; i < 8; i++)
            board.deal(3);
        String fullThrowStack = board.getThrowStack().toString();
        board.deal(3); //empty throwstack, put cards back in deck
        board.undo();  //undo: put cards back in throwstack
        assertEquals(fullThrowStack, board.getThrowStack().toString());
        board.redo(); //redo: put cards back in deck (again)
        assertTrue(board.getThrowStack().isEmpty());
        assertEquals(beforeString, board.toString());
    }

    @Test
    @DisplayName("Test if games are solved")
    void testIsSolved() {
    	Map<SType, CardContainer> testMap = new TreeMap<>();
    	for (int i = 0; i < 4 ; i++) 
			testMap.put(SType.valueOf("F" + i), new CardStack(SType.valueOf("F" + i)));
    	for (int i = 0; i < 7 ; i++) 
			testMap.put(SType.valueOf("P" + i), new CardStack(SType.valueOf("P" + i))); 
    	testMap.put(SType.THROWSTACK, new CardStack(SType.THROWSTACK));
    	CardDeck deck = new CardDeck(13);
    	testMap.put(SType.DECK, deck);
    	deck.deal((CardStack) testMap.get(SType.F0), 13);
    	deck.deal((CardStack) testMap.get(SType.F1), 13);
    	deck.deal((CardStack) testMap.get(SType.F3), 13);
    	deck.deal((CardStack) testMap.get(SType.P2), 13);
    	GameBoard currentBoard = new GameBoard(testMap);
    	assertFalse(currentBoard.isSolved());
    	testMap.put(SType.F2, testMap.get(SType.P2));
    	testMap.put(SType.P2, new CardStack(SType.P2));
    	assertTrue(currentBoard.isSolved());
    }

    @Test
    @DisplayName("Test moving card to finalstacks")
    void testMoveToFinalStackMethods() {
    	assertEquals(gameBoardString, board.toString());
    	assertEquals("D1", board.getPlayStack(1).peek().toString());
    	assertTrue(board.moveToFinalStacks(board.getPlayStack(1)));
    	assertEquals("D1", board.getFinalStack(0).toString());
    	board.undo();
    	assertFalse(board.moveToFinalStacks(board.getPlayStack(6)));
    	    	
    	assertTrue(board.iterateStacksAndMoveToFinalStacks());
    	assertThrows(IllegalStateException.class, () -> board.undo(), "Undo should be disabled after iterateStacksAndMoveToFinalStacks()");
    	assertFalse(board.iterateStacksAndMoveToFinalStacks());
    	assertEquals("D1", board.getFinalStack(0).toString());
    }
    
    @Test
    @DisplayName("Test revealCard")
    void testRevealCard() {
    	assertEquals(2, board.getPlayStack(1).getCardCount());
    	assertThrows(IllegalStateException.class, () -> board.revealTopCard(board.getPlayStack(1)));
    	assertThrows(IllegalArgumentException.class, () -> board.getPlayStack(1).getCard(0));
    	board.iterateStacksAndMoveToFinalStacks();
    	assertThrows(IllegalArgumentException.class, () -> board.getPlayStack(1).getCard(0));
    	board.revealTopCard(board.getPlayStack(1));
    	assertEquals("H13", board.getPlayStack(1).getCard(0).toString());
    }
}
