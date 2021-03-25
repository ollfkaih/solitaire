package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
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
import java.util.TreeMap;

import javafx.scene.image.Image;

public class IOController implements IFileReadWrite {
	
	public final static String SAVEEXT = "sol";
	
	private Path getSaveFolderPath() {
		return Path.of(System.getenv("APPDATA"), "Solitaire");
	}
	
	private Path getSavePath(String filename) {
		return getSaveFolderPath().resolve(filename + "." + SAVEEXT);
	}
	
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
            PrintWriter writer = new PrintWriter(getSavePath("Save").toFile());
            writer.write(board.toString());
            writer.flush();
            
            writer.close();
        } catch (FileNotFoundException e) {
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
        	SolConst.SType key = SolConst.SType.valueOf(items[0]);
        	CardStack stack = new CardStack(key);
        	char stackType = key.toString().charAt(0);
        	
        	if (items.length == 3) {
        		String[] cards = items[2].replace(" ","").split(",");
            	for (String card : cards) {
            		if (!card.isEmpty())
           				stack.addCard(new Card(card.charAt(0),Integer.valueOf(card.substring(1))));	
           		}
           	}
        	if (stackType == 'D') {
        		stacks.put(key, new CardDeck(stack));
        	}
        	else {
        		if (! items[1].isEmpty())
        			stack.setHiddenCards(Integer.parseInt(items[1]));
        		stacks.put(key, stack);
        	}
        }
        scanner.close();
        
        try {
        	board = new GameBoard(stacks);
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        	throw new IllegalArgumentException("Could not load file");
        }
        return board;
    }

	/**
	 * This function takes a Card parameter and returns the appropriate image file.
	 * @param card
	 * @return
	 */
	public static Image getImage(Card card) {
		String imgDir = SolConst.IMGDIR;
		String ext = ".png";
		Image img;
		
		boolean useInt = false;
		char faceVal = 0;
		switch (card.getFace()) {
		case 2,3,4,5,6,7,8,9 -> {
				useInt = true;
			}
		case 1  -> faceVal = 'A';
		case 10 -> faceVal = 'T';
		case 11 -> faceVal = 'J';
		case 12 -> faceVal = 'Q';
		case 13 -> faceVal = 'K';
		default -> throw new IllegalArgumentException("Illegal card");
		}
		
		try {
			if (useInt) {
				img = new Image(SolitaireController.class.getResourceAsStream(imgDir + card.getFace() + card.getSuit() + ext));
			} else {
				img = new Image(SolitaireController.class.getResourceAsStream(imgDir + faceVal + card.getSuit() + ext));
			}
			return img;
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("Could not get card face");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("HUH? Error getting cardImage");
		}
		return null;
	}
}
