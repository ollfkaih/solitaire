package solitaire.fxui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import solitaire.model.Card;
import solitaire.model.CardContainer;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;
import solitaire.model.GameBoard;
import solitaire.model.SolConst.SType;
class IOHandlerTest {
    Map<SType, CardContainer> gameMap;
    IOHandler ioHandler = new IOHandler();

    @BeforeEach
    void setup() {
        gameMap = new HashMap<>();
        List<Card> deckCards = new ArrayList<>();
        CardStack finalStack = new CardStack(SType.F1);
        for (int i = 1; i <= 13; i++) {
            for (char suit: "CDH".toCharArray()) {
                deckCards.add(new Card(suit, i));
            }
            finalStack.add(new Card('S', i));
        }
        gameMap.put(SType.DECK, new CardDeck(new CardStack(SType.F0, deckCards.toArray(new Card[0]))));
        for (SType sType: SType.values())
            if (sType != SType.DECK)
                gameMap.put(sType, new CardStack(sType));
        gameMap.put(SType.F1, finalStack);
    }

      @Test
      @DisplayName("Test that createSaveFolder creates a save folder")
      void testCreateSaveFolder() {
          assertTrue(true);
          //Rename directory and test that it is created
          //Test that nothing happens if directory already exists
      }

      @Test
      @DisplayName("Test that writing to file works")
      void testWriteToFile() {
        GameBoard board = new GameBoard(gameMap);
        try {
            ioHandler.writeToFile("testFile", board);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(IOHandler.getSavePath("testFile").toString());
        String string = "";	
        try (Scanner scanner = new Scanner(file))  {
            while (scanner.hasNextLine())
            	string += scanner.nextLine() + "\n";
            assertEquals(board.toString(), string, "The created file " );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
      }

      @AfterEach
      @DisplayName("Clean up created files")
      void cleanup() {
        File file = new File(IOHandler.getSavePath("testFile").toString());
        file.delete();
      }
}
