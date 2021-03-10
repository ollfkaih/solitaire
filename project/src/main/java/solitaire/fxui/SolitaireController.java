package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.Card;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

public class SolitaireController {
	
	private GameBoard board;
	private CardDeck deck;
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private Label DrawStack;
	@FXML private Label ThrowStack; 
	@FXML private MenuItem newGame;
	@FXML private MenuItem Undo;
	
	@SuppressWarnings("unchecked")
	List<Label>[] p = (List <Label>[]) new ArrayList[SolConst.PLAYSTACKSNUM];
	Label[] f = new Label[SolConst.SUITS];
	private Label draggedLabel;
	private Label dropLabel;
	private CardStack dragParent;
	boolean initpStacks;
	
	@FXML
	private void initialize() {
		newGame.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		startNewGame();
	}
	
	/**
	 * startNewGame initializes a new game and cleans up after any previous game on the board
	 */
	@FXML
	private void startNewGame() {
		deck = new CardDeck(SolConst.CARDSINSUIT);
		deck.shuffle();
		board = new GameBoard(deck);
		FinalStacks.getChildren().clear(); //TODO: Make use of when adding a bottom label
		PlayStacks.getChildren().clear();
		
		Undo.setText("Undo");
		Undo.setOnAction(e -> undo());
		Undo.setDisable(true);
		
		for (int i = 0; i < SolConst.SUITS; i++) {
			f[i] = new Label(null);
			FinalStacks.getChildren().add(f[i]);
		}
		initpStacks = true;
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			p[i] = new ArrayList<>();
		}
		DrawStack.setText(null);
		setCustomImage(DrawStack, 'b');
		
		ThrowStack.setText(null);
		setCustomImage(ThrowStack,'e');
		
		updateBoard();
	}
	
	@FXML
	void clickDrawStack() {
		try {board.deal(SolConst.CARDSTODEAL);} catch (Exception e) {
			e.printStackTrace();
		}
		canUndo();
		updateDrawStack();
	}
	
	@FXML void undo() {
		try {board.undo();} catch (Exception e) {e.printStackTrace();}
		updateBoard();
		Undo.setText("Redo");
		Undo.setOnAction(e -> redo());
		//Undo.setAccelerator(new KeyCodeCombination(KeyCode.Y));
	}
	
	private void redo() {
		try {board.redo();} catch (Exception e) {e.printStackTrace();}
		updateBoard();
		canUndo();
	}
	
	private void canUndo() {
		Undo.setDisable(false);
		Undo.setText("Undo");
		//Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z));
		Undo.setOnAction(e -> undo());
	}
	
	@FXML void exit() {
        Platform.exit();
	}
	
	/** 
	 * setCustomImage(Label, char) sets the top card image to the backside of a card deck
	 * @param stack
	 */
	private static boolean setCustomImage(Label stack, char imgToShow) {
		String imgDir = SolConst.IMGDIR;
		String ext = ".png";
		String imgName;
		Image img;
		ImageView view;
		
		switch (imgToShow) {
		case 'b' -> imgName = "1B";
		case 'e' -> imgName = "2J"; //empty stack
		default -> throw new IllegalArgumentException("Illegal image to show: " + imgToShow);
		}
		try {
			img = new Image(SolitaireController.class.getResourceAsStream(imgDir + imgName + ext));
			view = new ImageView(img);
			view.setFitHeight(img.getHeight()/3);
			view.setFitWidth(img.getWidth()/3);
			stack.setGraphic(view);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not find file");
			return false;
		}
		return true;		
	}

	/**
	 * Sets the image of a label to the appropriate card image
	 * Returns true if the image was successfully set.
	 * @param stack
	 * @param card
	 * @return 
	 */
	private static boolean setCardImage(Label stack, Card card) {
		Image img;
		ImageView view;
		try {
			img = cardToImg(card);
			view = new ImageView(img);
			view.setFitHeight(img.getHeight()/3);
			view.setFitWidth(img.getWidth()/3);
			stack.setGraphic(view);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	return true;
	}

	/**
	 * This function takes a Card parameter and returns the appropriate image file.
	 * @param card
	 * @return
	 */
	private static Image cardToImg(Card card) {
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
				//img = new Image((SolitaireController.class.getResourceAsStream("img/3H.png")));
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
	
	/**
	 * Updates the throw- and drawstack (e.g. after dealing a card)
	 */
	private void updateDrawStack() {
		if (!board.getThrowStack().isEmpty()) {
			Card topCard = board.getThrowStack().get(board.getThrowStack().size() - 1);
			ThrowStack.setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, board.getThrowStack().peek(), board.getThrowStack()));
			ThrowStack.setOnDragDetected((MouseEvent event) -> dragDetected(event, ThrowStack, -1, -1, board.getThrowStack().getStackName()));
			ThrowStack.setOnDragDone(dragDoneEvent);
			setCardImage(ThrowStack, topCard);
		} else {
			ThrowStack.setOnMouseClicked(null);
			ThrowStack.setOnDragDetected(null);
			setCustomImage(ThrowStack,'e');
		}
		if (board.drawStackEmpty()) {
			setCustomImage(DrawStack,'e');
		} else {
			setCustomImage(DrawStack,'b');
		}
	}
	
	/**
	 * Updates all playstacks
	 */
	void updatePlayStacks() {
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			//if (p[i].size() != board.getPlayStack(i).getCardCount() + 1) {
			for (Label cardLabel: p[i]) {
				PlayStacks.getChildren().remove(cardLabel);
			}
			char targetStack = 'p';
			
			p[i].add(new Label(null));
			PlayStacks.getChildren().add(p[i].get(0));
			setCustomImage(p[i].get(0), 'e');

		    p[i].get(0).setUserData(targetStack);
			p[i].get(0).setOnDragOver(dragOverEvent);
	    	p[i].get(0).setOnDragDone(dragDoneEvent);
	    	int thisIndexi = i;
	    	p[i].get(0).setOnDragDropped((DragEvent event) -> drop(event, p[thisIndexi].get(0), thisIndexi, 0, board.getPlayStack(thisIndexi).getStackName()));
			
			for (int labelIndex = 1; labelIndex <= board.getPlayStack(i).getCardCount(); labelIndex++) {
				int cardIndex = labelIndex - 1;
				Label label = new Label(null);
				p[i].add(label);
			    p[i].get(labelIndex).setUserData(targetStack);
		    	
			    int labelj = labelIndex;
				int thisIndexj = cardIndex;
				//Hidden cards should not be draggable or doubleclickable
				if (! board.getPlayStack(i).isHidden(cardIndex)) {
					Card card = board.getPlayStack(i).get(cardIndex);
					setCardImage(p[i].get(labelIndex), card);

					p[i].get(labelIndex).setOnDragDetected((MouseEvent event) -> dragDetected(event, p[thisIndexi].get(labelj), thisIndexi, thisIndexj, board.getPlayStack(thisIndexi).getStackName()));
		    		p[i].get(labelIndex).setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, board.getPlayStack(thisIndexi).get(thisIndexj), board.getPlayStack(thisIndexi)));
				} else {
					setCustomImage(p[i].get(labelIndex),'b');
				}
		    	p[i].get(labelIndex).setOnDragOver(dragOverEvent);
		    	p[i].get(labelIndex).setOnDragDone(dragDoneEvent);
		    	//All cards should be droptargets; if you drop a card on a hidden card, the drop() method attempts to put it at the top of this stack
		    	p[i].get(labelIndex).setOnDragDropped((DragEvent event) -> drop(event, p[thisIndexi].get(labelj), thisIndexi, thisIndexj, board.getPlayStack(thisIndexi).getStackName()));
		    	PlayStacks.getChildren().add(p[i].get(labelIndex));
			}
			pTranslate(p[i], i);
		}
	}
	
	/**
	 * This method redraws play stacks if the number of elements is out of sync with our actual GameBoard,
	 * sets the correct card in each final stack and updates the drawing and throw stack.
	 */
	void updateBoard() {
		//TODO: loop through only neccessary cards and stacks
		//TODO: REMOVE console printout
		for (int i = 0; i < 7; i++)
			System.out.println(board.getPlayStack(i));
		
		try {updatePlayStacks();} catch (Exception e) {e.printStackTrace();}

		for (int i = 0; i < SolConst.SUITS; i++) {
			try {
				//Events need to know what stack this card is in 
				char targetStack = 'f';
			    f[i].setUserData(targetStack);
				
			    int thisIndexi = i;
			    int thisIndexj = board.getFinalStack(i).getCardCount() - 1;
			    f[i].setOnDragDetected((MouseEvent event) -> dragDetected(event, f[thisIndexi], thisIndexi, thisIndexj, board.getFinalStack(thisIndexi).getStackName()));

				f[i].setOnDragOver(dragOverEvent);	
				f[i].setOnDragDone(dragDoneEvent);
				f[i].setOnDragDropped((DragEvent event) -> drop(event, f[thisIndexi], thisIndexi, thisIndexj, board.getPlayStack(thisIndexi).getStackName()));
				
				fTranslate(f[i], i);
				Card topCard = board.getFinalStack(i).peek();
				setCardImage(f[i], topCard);
			} catch (Exception IllegalArgumentException) {
				setCustomImage(f[i],'e');
			}
		}
		updateDrawStack();
		
		if (board.isSolved()) {
			ThrowStack.setText("SOLVED");
		}
		
	}
	
	private void fTranslate(Label l, int i) {
		l.setTranslateX(105 + 85*i); 
		//l.setTranslateY(30);
	}
	
	private void pTranslate(List<Label> l, int i) {
		l.get(0).setTranslateX(20 + 85*i);
		int yOffset = 0;
		
		for (int j = 1; j < l.size(); j++) {
			l.get(j).setTranslateX(20 + 85*i);
			
			if (j == 1) 
				l.get(1).setTranslateY(yOffset = 0);
			else if (board.getPlayStack(i).isHidden(j - 2)) 
				yOffset = yOffset + 6;
			else 
				yOffset = yOffset + 15;
			
			l.get(j).setTranslateY(yOffset);
		}
	}

	/**
	 * This method is called when a dragEvent is detected on certain cards and puts a string on this events
	 * dragboard, as well as showing the card being dragged
	 * @param event
	 * @param l
	 * @param indexOfStacks
	 * @param indexOfList
	 * @param stackName
	 */
	private void dragDetected(MouseEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.SType stackName) {
		
		Dragboard db = l.startDragAndDrop(TransferMode.MOVE);
		draggedLabel = l;
		//l.setCursor(Cursor.NONE);
		
		dragParent = board.getStackbyName(stackName);
		
		String dbFromStack = "" + ((char) (stackName.toString().charAt(0))) + indexOfStacks + indexOfList; //Ascii char A -> a has a delta of 32 

	    ClipboardContent content = new ClipboardContent();
	    content.putString(dbFromStack);
	    db.setContent(content);
	    //TODO: Make snapshot render cards on top ?P33
	    db.setDragView(l.snapshot(null, null), event.getX(), event.getY());
		//TODO: Make invisible, but somehow make visible when letting go
		draggedLabel.setVisible(false);
	    event.consume();
	}
	
	//dragOverEvent makes the target able to accept cards
	EventHandler <DragEvent> dragOverEvent = new EventHandler <DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
            draggedLabel.setCursor(Cursor.DEFAULT);
            event.consume();
        }
	};
	
	//dragDoneEvent sets the label visible again (triggered when card is dropped on a non-target)
		EventHandler <DragEvent> dragDoneEvent = new EventHandler <DragEvent>() {
	        public void handle(DragEvent event) {
	            draggedLabel.setVisible(true);
	        	//event.consume();
	        }
		};
		
	private void drop(DragEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.SType stackName) {
		Dragboard db = event.getDragboard();
		if (!db.hasString()) throw new IllegalArgumentException("The dragboard is empty for " + event.getSource());
		dropLabel = l;
		draggedLabel.setVisible(true);
		
		if (draggedLabel == dropLabel) return;
		int inStackIndex = 0;
		char targetStack = ((Label) event.getTarget()).getUserData().toString().charAt(0);
		char fromType = db.getString().charAt(0);
		
		if (fromType != 'T') {
			inStackIndex = Integer.parseInt(db.getString().substring(2));
		} else {
			inStackIndex = board.getThrowStack().size() - 1;
		}

		switch (targetStack) {
		case 'p' -> {
			board.moveCard(inStackIndex, dragParent, board.getPlayStack(indexOfStacks));
		} case 'f' -> {
			board.moveCard(inStackIndex, dragParent, board.getFinalStack(indexOfStacks));
		}
		}
		canUndo();
		updateBoard();
		event.consume();
	}

	private void doubleClickCard(MouseEvent event, Card card, CardStack stack) {
		if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2) {
            	try {
            		System.out.println("Double-clicked:" + card.toString());
            		board.moveToFinalStacks(card, stack);
            		canUndo();
            	} catch (Exception e) {
            		e.printStackTrace();
	            	System.out.println("Could not move to a final stack: " + ((Label) event.getSource()).getText());
            	} finally {
            		updateBoard();
            	}
            }
        }
	}
}
