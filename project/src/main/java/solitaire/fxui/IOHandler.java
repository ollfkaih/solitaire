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
	
	public final static String SAVEEXT = "sol";
	
	/**
	 * Returns the path to the save folder (%APPDATA%\Solitaire on Windows), user.home/Solitaire otherwise
	 */
	private Path getSaveFolderPath() {
		//I chose to save to appdata on windows because it is more frowned upon on windows to create subdirectories
		//directly in user folder (and personal preference), and appdata was easy to get on all localised Windows system.
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			return Path.of(System.getenv("APPDATA"), "Solitaire");
		return Path.of(System.getProperty("user.home"), "Solitaire");
	}
	
	/**
	 * Returns the path to a file in our save directory
	 * @param filename the name of the file (without file extension)
	 */
	private Path getSavePath(String filename) {
		return getSaveFolderPath().resolve(filename + "." + SAVEEXT);
	}
	
	/**
	 * Ensures that our save directory exists
	 */
	private void createSaveFolder() {
		if (Files.notExists(getSaveFolderPath())) {
            try {
                Files.createDirectories(getSaveFolderPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	@Override
    public void writeToFile(GameBoard board) {
        createSaveFolder();
        try {
            String filename = "Save";
			PrintWriter writer = new PrintWriter(getSavePath(filename).toFile());
            writer.write(board.toString());
            writer.flush();
            
            writer.close();
        } catch (FileNotFoundException e) {
			//TODO: LOG PROPERLY (Throw exception here, let the controller log it)
            e.printStackTrace();
        }    
    }
	
    public GameBoard loadGame(String name) throws IOException {
        Scanner scanner = new Scanner(getSavePath(name));
        GameBoard board;
        
        Map <SolConst.SType, CardContainer> stacks = new TreeMap<>();
        
        while (scanner.hasNextLine()) {
        	String line = scanner.nextLine();
        	String[] items = line.split(":", 3);
        	SolConst.SType stackName = SolConst.SType.valueOf(items[0]);
        	CardStack stack;
        	
        	Stack<Card> tempStack = new Stack<Card>();
        	if (items.length == 3) {
        		String[] cards = items[2].replace(" ","").split(",");
            	for (String card : cards) {
            		if (!card.isEmpty())
           				tempStack.add(new Card(card.charAt(0),Integer.valueOf(card.substring(1))));	
           		}
           	}
        	
        	if (items[1].isEmpty())
            	stack = new CardStack(stackName,tempStack.toArray(new Card[0]));
        	else 
        		stack = new CardStack(stackName, Integer.parseInt(items[1]), tempStack.toArray(new Card[0]));
        	
        	if (stackName.equals(SType.DECK)) 
        		stacks.put(stackName, new CardDeck(stack));
        	else 
        		stacks.put(stackName, stack);
        }
        scanner.close();
        
        try {
        	board = new GameBoard(stacks);
        } catch (IllegalArgumentException e) {
        	throw new IllegalArgumentException("Could not load file");
        }
        return board;
    }
}
