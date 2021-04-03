package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.SolConst.SType;
import solitaire.logging.DistributingLogger;
import solitaire.logging.ILogger;
import solitaire.logging.LabelLogger;
import solitaire.model.Card;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

public class SolitaireController {
	
	private GameBoard board;
	@FXML private AnchorPane Root;
	@FXML private GridPane GridRoot;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private AnchorPane ThrowStack; 
	@FXML private AnchorPane Deck;
	@FXML private HBox BottomBar;
	@FXML private MenuItem newGame;
	@FXML private MenuItem Undo;
	@FXML private MenuBar menubar;
	
	private Map<SolConst.SType, List<Label>> labels = new TreeMap<>();
	private Label draggedLabel;
	private Label dropLabel;
	private CardStack dragParentCardStack;
	boolean initpStacks;
	
	private DistributingLogger logger;
	private LabelLogger labelLogger;
	private final IOController ioController = new IOController();
	private final static String filename = "Save";
	private final static float cardScaler = (float) (1/3.0);
	
	private List<Label> getFinalLabelStack(int i) {
		return labels.get(SType.valueOf("F" + i));
	}	
	private List<Label> getPlayLabelStack(int i) {
		return labels.get(SType.valueOf("P" + i));
	}	
	private List<Label> getThrowLabelStack() {
		return  labels.get(SType.THROWSTACK);
	}	
	private List<Label> getLabelDeck() {
		return labels.get(SType.DECK);
	}

	@FXML
	private void initialize() {
		newGame.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		//Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z));
		startNewGame();
		
		Root.widthProperty().addListener(e -> {
			for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) 
				pTranslate(getPlayLabelStack(i),i);
			for (int i = 0; i < SolConst.SUITS; i++)
				fTranslate(getFinalLabelStack(i), i);
			deckTranslate();
		});
	}
	
	/**
	 * startNewGame initializes a new game and cleans up after any previous game on the board
	 */
	@FXML
	private void startNewGame() {
		CardDeck carddeck = new CardDeck(SolConst.CARDSINSUIT);
		carddeck.shuffle();	
		board = new GameBoard(carddeck);
		
		//TODO: REMOVE TO NOT AUTOLOAD
		loadGame();
		
		resetBoardLabels();
		//TODO: Use distributive logger, log error and info to status bar, but warning (e.g. "card cannot be moved here") to file
		labelLogger = new LabelLogger(BottomBar);
		logger = new DistributingLogger(labelLogger, null, labelLogger);
		logger.log(ILogger.INFO, "New game started", null);
		/*Label statusLabel = new Label("New game started");
		int width = windowWidth();
		statusLabel.setTranslateX(width/35);
		statusLabel.setTranslateY(5);
		BottomBar.getChildren().add(statusLabel);*/
	}
	
	private void resetBoardLabels() {
		Root.getChildren().removeIf(e -> e.getClass() == ImageView.class);
		FinalStacks.getChildren().clear();
		PlayStacks.getChildren().clear();
		ThrowStack.getChildren().clear();
		
		Undo.setText("Undo");
		Undo.setOnAction(e -> undo());
		Undo.setDisable(true);
		
		for (int i = 0; i < SolConst.SUITS; i++) 
			labels.put(SType.valueOf("F" + i), new ArrayList<Label>());
		//TODO: REMove ?
		initpStacks = true;
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) 
			labels.put(SType.valueOf("P" + i), new ArrayList<Label>());
		
		labels.put(SType.THROWSTACK, new ArrayList<Label>()); 
		
		labels.put(SType.DECK, new ArrayList<Label>());
		if (getLabelDeck().size() == 0 || getLabelDeck().size() > 1)
			getLabelDeck().clear();
			getLabelDeck().add(new Label(null));
		Deck.getChildren().add(getLabelDeck().get(0));
		//Deck.setTranslateX(20);
		setCustomImage(getLabelDeck().get(0), 'b');
		updateBoard();
	}
	
	@FXML
	void clickDeck() {
		try {board.deal(SolConst.CARDSTODEAL);} catch (Exception e) {
			e.printStackTrace();
		}
		canUndo();
		updateDeck();
		updateThrowStack();
		deckTranslate();
	}
	
	@FXML void undo() {
		try {board.undo();} catch (Exception e) {e.printStackTrace(); return;}
		updateBoard();
		Undo.setText("Redo");
		Undo.setOnAction(e -> redo());
		deckTranslate();
		//Undo.setAccelerator(new KeyCodeCombination(KeyCode.Y));
	}
	
	private void redo() {
		try {board.redo();} catch (Exception e) {e.printStackTrace(); return;}
		updateBoard();
		canUndo();
		//tTranslate();
	}
	
	private void canUndo() {
		Undo.setDisable(false);
		Undo.setText("Undo");
		//Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z));
		Undo.setOnAction(e -> undo());
	}

	public void promptSave() { 
		Alert askToSave = new Alert(AlertType.CONFIRMATION);
		askToSave.setTitle("Solitaire");
		askToSave.setHeaderText("Do you want to save the game?");
		askToSave.setContentText("Your previous save file will be overwritten.");
		askToSave.initStyle(StageStyle.UTILITY);
		ButtonType saveButtonType = new ButtonType("Save", ButtonData.YES);
		ButtonType dontSaveButtonType = new ButtonType("Dont Save", ButtonData.CANCEL_CLOSE);
		askToSave.getButtonTypes().setAll(saveButtonType, dontSaveButtonType);

		Optional<ButtonType> result = askToSave.showAndWait();
		if (result.get() == saveButtonType){
			saveGame();
		} 
	}
	
	@FXML void exit() {
		promptSave();
		Platform.exit();
		//System.exit(0);
	}
	
	public void saveGame() {
		ioController.writeToFile(this.board);
	}
	
	public void loadGame() {
		try {
			this.board = ioController.loadGame(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resetBoardLabels();
	}

	/**
	 * Updates the throw- and drawstack (e.g. after dealing a card)
	 */
	private void updateDeck() {
		if (board.drawStackEmpty()) {
			setCustomImage(getLabelDeck().get(0),'e');
		} else {
			setCustomImage(getLabelDeck().get(0),'b');
		}
	}
	
	/**
	 * updateStack
	 */
	private void updateStack(List<Label> stackLabels, CardStack stack, AnchorPane LabelParent, int i) {
		stackLabels.add(new Label(null));
		LabelParent.getChildren().add(stackLabels.get(0));
		setCustomImage(stackLabels.get(0), 'e');
		
		char targetStack = stack.getStackName().toString().toLowerCase().charAt(0);
	    stackLabels.get(0).setUserData(targetStack);
		stackLabels.get(0).setOnDragOver(dragOverEvent);
    	stackLabels.get(0).setOnDragDone(dragDoneEvent);
    	stackLabels.get(0).setOnDragDropped((DragEvent event) -> drop(event, stackLabels.get(0), i, 0, stack.getStackName()));
		
		for (int labelIndex = 1; labelIndex <= stack.getCardCount(); labelIndex++) {
			int cardIndex = labelIndex - 1;
			
			Label label = new Label(null);
			stackLabels.add(label);
		    stackLabels.get(labelIndex).setUserData(targetStack);
	    	
		    int labelj = labelIndex;
			//Hidden cards should not be draggable or doubleclickable
			if (! stack.isHidden(cardIndex)) {
				Card card = stack.get(cardIndex);
				setCardImage(stackLabels.get(labelIndex), card);
				if (stack.getStackName().toString().charAt(0) != 'T' || stack.indexOf(card) == stack.getCardCount() - 1) {
					stackLabels.get(labelIndex).setOnDragDetected((MouseEvent event) -> dragDetected(event, stackLabels.get(labelj), i, cardIndex, stack.getStackName()));
	    			stackLabels.get(labelIndex).setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, stack.get(cardIndex), stack));
				}
			} else {
				setCustomImage(stackLabels.get(labelIndex),'b');
				if (cardIndex == stack.size() - 1)
					stackLabels.get(labelIndex).setOnMouseClicked((MouseEvent event) -> revealCard(event, stack));
			}
	    	stackLabels.get(labelIndex).setOnDragDone(dragDoneEvent);
			if (stack.getStackName().toString().charAt(0) != 'T') {
				stackLabels.get(labelIndex).setOnDragOver(dragOverEvent);
				//All cards should be droptargets; if you drop a card on a hidden card, the drop() method attempts to put it at the top of this stack
				stackLabels.get(labelIndex).setOnDragDropped((DragEvent event) -> drop(event, stackLabels.get(labelj), i, cardIndex, stack.getStackName()));
			}
	    	LabelParent.getChildren().add(stackLabels.get(labelIndex));
		}
		for (int labelIndex = stack.getCardCount() + 1; labelIndex < stackLabels.size(); ) {
			LabelParent.getChildren().remove(stackLabels.get(labelIndex));
			stackLabels.remove(labelIndex);
		}
	}

	/**
	 * Updates all playstacks
	 */
	void updatePlayStacks() {
		PlayStacks.getChildren().clear();
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			updateStack(getPlayLabelStack(i), board.getPlayStack(i), PlayStacks, i);
			pTranslate(getPlayLabelStack(i), i);
		}
	}
	/**
	 * Updates all finalStacks
	 */
	private void updateFinalStacks() {
		FinalStacks.getChildren().clear();
		for (int i = 0; i < SolConst.SUITS; i++) {
			updateStack(getFinalLabelStack(i), board.getFinalStack(i), FinalStacks, i);
			fTranslate(getFinalLabelStack(i), i);
		}
	}

	private void updateThrowStack() {
		ThrowStack.getChildren().clear();
		List<Label> tLabels = getThrowLabelStack();
		updateStack(tLabels, board.getThrowStack(), ThrowStack, -1);
		tLabels.get(tLabels.size() - 1).setOnDragDetected(((MouseEvent event) -> dragDetected(event, tLabels.get(tLabels.size() - 1), -1, tLabels.size() - 2, SolConst.SType.THROWSTACK)));
	}
	
	/**
	 * This method redraws play stacks if the number of elements is out of sync with our actual GameBoard,
	 * sets the correct card in each final stack and updates the drawing and throw stack.
	 */
	void updateBoard() {
		try {updatePlayStacks();} catch (Exception e) {e.printStackTrace();}
		try {updateFinalStacks();} catch (Exception e) {e.printStackTrace();}
		try {updateDeck();} catch (Exception e) {e.printStackTrace();}
		try {updateThrowStack();} catch (Exception e) {e.printStackTrace();} 
		
		System.out.println(board.toString());
		if (board.isSolved()) {
			Undo.setDisable(true);
			//TODO: Change so it can be enabled again;
			menubar.setDisable(true);

			Deck.setOnMouseClicked(null);
			labels.forEach((key, list) -> list.get(list.size() - 1).setOnDragDetected(null));
			labels.entrySet().removeIf(c -> c.getKey().toString().charAt(0) != 'F');
			labels.forEach((k, list) -> {
				for (Label l: list) {
					l.setOnMouseClicked(null);
					l.setOnDragDetected(null);
					double posX = l.getParent().getLayoutX() + l.getTranslateX();
					l.setTranslateX(0);
					
					//Make sure the remaining cards parent is root, so we can use getParent in winAnimate to get window size
					Root.getChildren().add(l);
					l.relocate(posX, 40);
				}
			});
			FinalStacks.getChildren().clear();
			
			WinAnimation winAnimate = new WinAnimation(labels, Root, Root.getChildren().size());
			//TODO: research animation stop
			menubar.setOnMouseClicked(e -> winAnimate.stop());
			newGame.setOnAction(e -> {
				winAnimate.stop();
				startNewGame();
			});
			winAnimate.start();
		}
	}
	
	/**
	 * @return the width of the current window in pixels
	 */
	private int windowWidth() {
		int width = (int) Root.getWidth();
		if (width <= 0)
			width = (int) Root.getPrefWidth();
		return width;
	}
	
	/**
	 * @return the height of the current window in pixels
	 */
	/*private int windowHeight() {
		int height = (int) Root.getHeight();
		if (height <= 0)
			height = (int) Root.getPrefHeight();
		return height;
	}
	*/
	/**
	 * Translates the final stacks to correct position
	 * @param l List of labels that represents a final stack
	 * @param i the number 
	 */
	private void fTranslate(List<Label> l, int i) {
		if (i < 0 || i >= SolConst.SUITS)
			throw new IllegalArgumentException("The index of the final stack must be between 0 and " + (SolConst.SUITS - 1));
		int width = windowWidth();
		int xOffset =  3*width/7 + width/120;
		float xFactor = (float) (width/7.35);
		
		FinalStacks.setLayoutX(xOffset);
		
		for (Label label : l)
			label.setTranslateX(xFactor*i);
	}
	
	private void deckTranslate() {	
		int width = windowWidth();
		int xOffset = (int) (width/35);
		float xFactor = (float) (width/7.35);
		ThrowStack.setTranslateX(Math.round(xOffset + xFactor));
		Deck.setTranslateX(xOffset);
		
		int tsize = getThrowLabelStack().size();
		for (Label l : getThrowLabelStack())
			l.setTranslateX(0);
		for (int i = 0; i < 3; i++)
			if (tsize + i - 3 > 0)
				getThrowLabelStack().get(i + tsize - 3).setTranslateX(12*(i));
	}
	
	private void pTranslate(List<Label> l, int i) {
		int width = windowWidth();
		int xOffset = (int) (width/35);
		float xFactor = (float) (width/7.35);
		l.get(0).setTranslateX(Math.round(xOffset + xFactor*i));
		int yOffset = 0;
		
		for (int j = 1; j < l.size(); j++) {
			l.get(j).setTranslateX(Math.round(xOffset + xFactor*i));
			
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
		WritableImage img;
		Dragboard db = l.startDragAndDrop(TransferMode.MOVE);
		double scale = Screen.getPrimary().getOutputScaleX();
		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(Transform.scale(scale, scale));
		draggedLabel = l;
		//l.setCursor(Cursor.NONE);
		dragParentCardStack = (CardStack) board.getStackbyName(stackName);
		String dbFromStack = "" + ((char) (stackName.toString().charAt(0))) + indexOfStacks + indexOfList;
	    ClipboardContent content = new ClipboardContent();
	    content.putString(dbFromStack);
	    db.setContent(content);
		boolean ignoreCardsOnTop = false;

		for (Map.Entry<SolConst.SType, List<Label>> labellist : labels.entrySet()) {
			SType key = labellist.getKey();
			if (key.equals(SType.THROWSTACK) || key.toString().charAt(0) == SType.F0.toString().charAt(0))
				if (labellist.getValue().contains(l))
					ignoreCardsOnTop = true;
		}
	 
		if (ignoreCardsOnTop) {
			img = new WritableImage((int) Math.round(l.getWidth() * scale), (int) Math.round(l.getHeight() * scale));
			l.snapshot(snapshotParameters, img);
			draggedLabel.setVisible(false);
		} else {
			Pane stackOfCards = new Pane();
			boolean addCards = false;
			int indexOfL = getPlayLabelStack(indexOfStacks).indexOf(l);
			for (int i = indexOfL; i < getPlayLabelStack(indexOfStacks).size(); i++) {
				if (addCards || getPlayLabelStack(indexOfStacks).get(i).equals(l)) {
					addCards = true;
					ImageView view = new ImageView(getPlayLabelStack(indexOfStacks).get(i).snapshot(snapshotParameters,null));
					view.setY(15*(i - indexOfL));
					view.setPreserveRatio(true);
					view.setFitHeight(getPlayLabelStack(indexOfStacks).get(i).getHeight());
					view.setFitWidth(getPlayLabelStack(indexOfStacks).get(i).getWidth());
					getPlayLabelStack(indexOfStacks).get(i).setVisible(false);
					stackOfCards.getChildren().add(view);
				}
			}
			img = new WritableImage(
				(int) Math.round(l.getWidth() * scale),
				(int) Math.round(l.getHeight() * scale) + 15*(getPlayLabelStack(indexOfStacks).size() - indexOfL - 1));
			stackOfCards.snapshot(snapshotParameters,img);
		}
	    db.setDragView(img, event.getX(), event.getY());
	    event.consume();
	}
	
	/** dragOverEvent makes the target able to accept cards
	 */
	EventHandler <DragEvent> dragOverEvent = new EventHandler <DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
            draggedLabel.setCursor(Cursor.DISAPPEAR);
            event.consume();
        }
	};
	
	/**
	 * dragDoneEvent sets the label visible again (triggered when card is dropped on a non-target)
	 */
		EventHandler <DragEvent> dragDoneEvent = new EventHandler <DragEvent>() {
	        public void handle(DragEvent event) {
				for (Node label : PlayStacks.getChildren())
					label.setVisible(true);
	            draggedLabel.setVisible(true);
	        	//event.consume();
	        }
		};
		
	/**
	 * Calls revealCard() on our gameboard to reveal the top card of a play stack
	 * @param event
	 * @param stack
	 */
	private void revealCard(MouseEvent event, CardStack stack) {
		board.revealCard(stack);
		Undo.setDisable(true);
		updateBoard();
	}
	
	private void drop(DragEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.SType stackName) {
		labelLogger.clearStatusBar();
		Dragboard db = event.getDragboard();
		if (!db.hasString()) throw new IllegalArgumentException("The dragboard is empty for " + event.getSource());
		dropLabel = l;
		for (Node label : PlayStacks.getChildren())
			label.setVisible(true);
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
			board.moveCard(inStackIndex, dragParentCardStack, board.getPlayStack(indexOfStacks));
		} case 'f' -> {
			board.moveCard(inStackIndex, dragParentCardStack, board.getFinalStack(indexOfStacks));
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

/** 
	 * setCustomImage(Label, char) sets the top card image to the backside of a card carddeck
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
			view.setFitHeight(img.getHeight()*cardScaler);
			view.setFitWidth(img.getWidth()*cardScaler);
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
			img = IOController.getImage(card);
			view = new ImageView(img);
			view.setFitHeight(img.getHeight()*cardScaler);
			view.setFitWidth(img.getWidth()*cardScaler);
			stack.setGraphic(view);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	return true;
	}
}