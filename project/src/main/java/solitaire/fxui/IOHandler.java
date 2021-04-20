package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.SolConst.SType;
import solitaire.model.Card;
import solitaire.model.CardContainer;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;

public class IOHandler implements IFileReadWrite {
	
	public static final String SAVEEXT = "sol";
	
	/**
	 * Returns the path to the save folder (%APPDATA%\Solitaire on Windows), user.home/Solitaire otherwise
	 */
	public static final Path getSaveFolderPath() {
		//I chose to save to appdata on windows because it is more frowned upon on windows to create subdirectories
		//directly in user folder (and personal preference), and appdata was easy to get on all localised Windows system.
		//The try/catch is because I have not tested if (Path of appdata) will be null or throw exception on linux etc. 
		Path savePath;
		try {
			if (System.getProperty("os.name").toLowerCase().contains("win"))
				savePath = Path.of(System.getenv("APPDATA"), "Solitaire");
			else
				savePath = Path.of(System.getProperty("user.home"), "Solitaire");
		} catch (Exception e) {
			savePath = Path.of(System.getProperty("user.home"), "Solitaire");
		}
		return savePath;
	}
	
	/**
	 * Returns the path to a file in our save directory
	 * @param filename the name of the file (without file extension)
	 */
	public static final Path getSavePath(String filename) {
		return getSaveFolderPath().resolve(filename + "." + SAVEEXT);
	}
	
	/**
	 * Ensures that our save directory exists
	 */
	private static void createSaveFolder() {
		if (Files.notExists(getSaveFolderPath())) {
            try {
                Files.createDirectories(getSaveFolderPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	@Override
    public void writeToFile(String filename, String textToWrite) throws FileNotFoundException {
        createSaveFolder();
        try (PrintWriter writer = new PrintWriter(getSavePath(filename).toFile())) {
            writer.write(textToWrite);
            writer.flush();
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
        }  
    }
	
    public GameBoard loadGame(String filename) throws IOException {
        Scanner scanner = new Scanner(getSavePath(filename));
        GameBoard board;
        
        Map <SolConst.SType, CardContainer> stacks = new TreeMap<>();
        
        while (scanner.hasNextLine()) {
        	String line = scanner.nextLine();
        	String[] items = line.split(":", 3);
        	SolConst.SType stackName = SolConst.SType.valueOf(items[0]);
        	CardStack stack;
        	
        	Stack<Card> tempStack = new Stack<>();
        	if (items.length == 3) {
        		String[] cardsAsString = items[2].replace(" ","").split(",");
            	for (String card : cardsAsString) {
            		if (!card.isEmpty())
           				tempStack.add(new Card(card.charAt(0),Integer.valueOf(card.substring(1))));	
           		}
           	}
			Card[] cards = tempStack.toArray(new Card[0]);   
			if (stackName.equals(SType.DECK)){
        		stacks.put(stackName, new CardDeck(new CardStack(SType.F0, cards))); 
			} else {
				if (items[1].isEmpty())
            		stack = new CardStack(stackName, cards);
        		else 
        			stack = new CardStack(stackName, Integer.parseInt(items[1]), cards);
				stacks.put(stackName, stack);	
			}
        }
        scanner.close();
        
        try {
        	board = new GameBoard(stacks);
        } catch (IllegalArgumentException e) {
        	throw new IllegalArgumentException("Could not create game from given file");
        }
        return board;
    }
}
