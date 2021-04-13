package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.SolConst.SType;
import solitaire.fxui.LabelGraphics.SPECIALIMAGE;
import solitaire.logging.ILogger;
import solitaire.model.Card;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SolitaireController  {
	
	private final IOHandler ioHandler = new IOHandler();
	private static final String FILENAME = "Save";
	
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private AnchorPane ThrowStack; 
	@FXML private AnchorPane Deck;
	@FXML private MenuBar menubar;
	@FXML private MenuItem newGame;
	@FXML private MenuItem Solve;
	@FXML private MenuItem Undo;
	@FXML private HBox statusBar;
	@FXML private StatusBarController statusBarController;
	
	private GameBoard board;
	
	private Map<SolConst.SType, List<Label>> labels = new TreeMap<>();
	private Label draggedLabel;
	private Label dropLabel;
	private CardStack dragParentCardStack;
	private WinAnimation winAnimate;
	private Stage stage;

	private List<Label> getFinalStackLabels(int i) {
		return labels.get(SType.valueOf("F" + i));
	}	
	private List<Label> getPlayStackLabels(int i) {
		return labels.get(SType.valueOf("P" + i));
	}	
	private List<Label> getThrowStackLabels() {
		return labels.get(SType.THROWSTACK);
	}	
	private List<Label> getDeckLabel() {
		return labels.get(SType.DECK);
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}	
	/**
	 * Wrapper function for LabelGraphics' setSpecialImage that also log an error message if the image was not loaded correctly
	 * @param label The label to set an imageview to
	 * @param type
	 */
	private void setSpecialImageforLabel(Label label, SPECIALIMAGE type) {
		boolean labelSetCorrectly = LabelGraphics.setSpecialImage(label, type);
		if (!labelSetCorrectly)
			statusBarController.log(ILogger.ERROR, StatusBarController.LOADGRAPHICSERROR, null);
	}

	@FXML
	private void initialize() {
		newGame.setAccelerator(new KeyCodeCombination(KeyCode.F2));
		Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		startNewGame();
		
		Root.widthProperty().addListener(e -> {
			for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) 
				playStackTranslate(getPlayStackLabels(i),i);
			for (int i = 0; i < SolConst.SUITS; i++)
				finalStackTranslate(getFinalStackLabels(i), i);
			deckAndThrowStackTranslate(0);
		});		
	}
	
	/**
	 * startNewGame initializes a new deck and gameboard
	 */
	@FXML
	private void startNewGame() {
		CardDeck carddeck = new CardDeck(SolConst.CARDSINSUIT);
		carddeck.shuffle();	
		board = new GameBoard(carddeck);
		
		//TODO: REMOVE TO NOT AUTOLOAD
//		loadGame();
		
		statusBarController.log(ILogger.INFO, StatusBarController.NEWGAME, null);
		resetBoardLabels();
	}
	
	/**
	 * resetBoardLabels() cleans up after any previous game on the board and instantiates the labels map with empty lists  
	 */
	private void resetBoardLabels() {
		stopWinAnimation();
		FinalStacks.getChildren().clear();
		PlayStacks.getChildren().clear();
		ThrowStack.getChildren().clear();
		labels.clear();
		
		Undo.setText("Undo");
		Undo.setOnAction(e -> undo());
		Undo.setDisable(true);
		
		for (int i = 0; i < SolConst.SUITS; i++) 
			labels.put(SType.valueOf("F" + i), new ArrayList<Label>());
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) 
			labels.put(SType.valueOf("P" + i), new ArrayList<Label>());
		
		labels.put(SType.THROWSTACK, new ArrayList<Label>()); 
		
		labels.put(SType.DECK, new ArrayList<Label>());
		if (getDeckLabel().size() != 1) {
			getDeckLabel().clear();
			getDeckLabel().add(new Label(null));
		}
		Deck.getChildren().add(getDeckLabel().get(0));
		setSpecialImageforLabel(getDeckLabel().get(0), SPECIALIMAGE.BACK);
		updateBoard();
	}
	
	@FXML
	void clickDeck() {
		int cardsDealt = 0;
		try {
			cardsDealt = board.deal(SolConst.CARDSTODEAL);
		} catch (Exception e) {
			statusBarController.log(ILogger.ERROR, StatusBarController.DEALERROR , null);
		}
		canUndo();
		if (winAnimate != null) return;
		statusBarController.log(ILogger.FINE, cardsDealt + " cards dealt", null);
		updateDeck();
		updateThrowStack();
		deckAndThrowStackTranslate(cardsDealt);
	}
	
	@FXML void undo() {
		try {board.undo();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null); return;}
		int visibleCards = visibleCardsInThrowStack();
		updateBoard();
		Undo.setText("Redo");
		Undo.setOnAction(e -> redo());
		if (board.getLastFromStack().getStackName().equals(SType.THROWSTACK) && visibleCards < 3)
			visibleCards++;
		deckAndThrowStackTranslate(visibleCards);
		Undo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
	}
	
	private void redo() {
		try {board.redo();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null); return;}
		int visibleCards = visibleCardsInThrowStack();
		updateBoard();
		canUndo();
		if (board.getLastFromStack().getStackName().equals(SType.THROWSTACK) && visibleCards > 1)
			visibleCards--;
		deckAndThrowStackTranslate(visibleCards);
	}
	
	private void canUndo() {
		Undo.setDisable(false);
		Undo.setText("Undo");
		Undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		Undo.setOnAction(e -> undo());
	}
	
	private int visibleCardsInThrowStack() {
		int count = 0;
		for (Label l : getThrowStackLabels()) {
			if (l.getTranslateX() > 0)
				count++;
		}
		//TODO: Uncomment if last move involved the throwstack?
		if (getThrowStackLabels().size() > 1 )
			count++;
		//if (/*board.getLastFromStack().getStackName() == SType.THROWSTACK ||*/ board.getLastToStack().getStackName() == SType.THROWSTACK)
			//count++;*/
		if (count > 3)
			count = 3;
		return count;
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
	}
	
	public void saveGame() {
		if (winAnimate != null)
			statusBarController.log(ILogger.ERROR, StatusBarController.SAVEERROR, null);
		else {
			try {
				ioHandler.writeToFile(this.board);
				statusBarController.log(ILogger.INFO, StatusBarController.SAVESUCCESS, null);
			} catch (Exception e) {
				statusBarController.log(ILogger.ERROR, StatusBarController.SAVEERROR, e);
			}
		}
	}
	
	public void loadGame() {
		try {
			this.board = ioHandler.loadGame(FILENAME);
			statusBarController.log(ILogger.INFO, StatusBarController.LOADSUCCESS, null);
		} catch (IllegalArgumentException e) {
			statusBarController.log(ILogger.ERROR, StatusBarController.SAVEFILECORRUPT, e);
		} catch (Exception e) {
			statusBarController.log(ILogger.ERROR, StatusBarController.LOADERROR, e);
		}
		resetBoardLabels();
	}
	
	/**
	 * Stops winAnimate if it is not null, and then overwrites the reference to it with null
	 */
	private void stopWinAnimation() {
		Solve.setDisable(false);
		if (winAnimate != null) {
			winAnimate.stop();
			stage.setResizable(true);
		}
		winAnimate = null;
	}
	/**
	 * Updates the throw- and drawstack (e.g. after dealing a card)
	 */
	private void updateDeck() {
		if (board.drawStackEmpty()) {
			setSpecialImageforLabel(getDeckLabel().get(0),SPECIALIMAGE.EMPTY);
		} else {
			setSpecialImageforLabel(getDeckLabel().get(0),SPECIALIMAGE.BACK);
		}
	}

	/**
	 * updateStack() updates a stack of labels to show the state that that stack of cards in GameBoard is in
	 * @param stackLabels the stack of labels to update
	 * @param stack the cardstack we want to show labels of
	 * @param LabelParent the parent Pane of the stack of labels
	 * @param i the play/final stack number (unneccessary for throwstack, may be any int) 
	 */
	private void updateStack(List<Label> stackLabels, CardStack stack, AnchorPane LabelParent, int i) {
		char targetStack = stack.getStackName().toString().toLowerCase().charAt(0);
		
		for (int labelIndex = 0; labelIndex <= stack.getCardCount(); labelIndex++) {
			int cardIndex = labelIndex - 1;
			
			Label label = new Label(null);
			label.setTextFill(null);
			stackLabels.add(label);
		    stackLabels.get(labelIndex).setUserData(targetStack);
	    	
		    int labelj = labelIndex;
			//Hidden cards should not be draggable or doubleclickable
			if (! stack.isHidden(cardIndex)) {
				Card card = stack.getCard(cardIndex);
				boolean labelSetCorrectly = LabelGraphics.setCardImage(stackLabels.get(labelIndex), card);
				if (!labelSetCorrectly)
					statusBarController.log(ILogger.ERROR, StatusBarController.LOADGRAPHICSERROR, null);
				if (labelIndex != 0 && stack.getStackName() != SType.THROWSTACK || cardIndex == stack.getCardCount() - 1) {
					stackLabels.get(labelIndex).setOnDragDetected((MouseEvent event) -> dragDetected(event, stackLabels.get(labelj), i, cardIndex, stack.getStackName()));
	    			stackLabels.get(labelIndex).setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, stack));
				}
			} else {
				if (labelIndex == 0) {
					setSpecialImageforLabel(stackLabels.get(labelIndex), SPECIALIMAGE.EMPTY);
				}
				else {
					setSpecialImageforLabel(stackLabels.get(labelIndex),SPECIALIMAGE.BACK);
				}
				if (cardIndex == stack.size() - 1 && labelIndex != 0)
					stackLabels.get(labelIndex).setOnMouseClicked((MouseEvent event) -> revealCard(stack));
			}
	    	stackLabels.get(labelIndex).setOnDragDone(dragDoneEvent);
			if (stack.getStackName() != SType.THROWSTACK) {
				stackLabels.get(labelIndex).setOnDragOver(dragOverEvent);
				//All cards should be droptargets; if you drop a card on a hidden card, the drop() method attempts to put it at the top of this stack
				stackLabels.get(labelIndex).setOnDragDropped((DragEvent event) -> drop(event, stackLabels.get(labelj), i));
			}
	    	LabelParent.getChildren().add(stackLabels.get(labelIndex));
		}
		//remove labels for cards that have been moved to another stack
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
			updateStack(getPlayStackLabels(i), board.getPlayStack(i), PlayStacks, i);
			playStackTranslate(getPlayStackLabels(i), i);
		}
	}
	/**
	 * Updates all finalStacks
	 */
	private void updateFinalStacks() {
		FinalStacks.getChildren().clear();
		for (int i = 0; i < SolConst.SUITS; i++) {
			updateStack(getFinalStackLabels(i), board.getFinalStack(i), FinalStacks, i);
			finalStackTranslate(getFinalStackLabels(i), i);
		}
	}

	private void updateThrowStack() {
		ThrowStack.getChildren().clear();
		List<Label> tLabels = getThrowStackLabels();
		updateStack(tLabels, board.getThrowStack(), ThrowStack, -1);
		if (tLabels.size() > 1)
			tLabels.get(tLabels.size() - 1).setOnDragDetected(((MouseEvent event) -> dragDetected(event, tLabels.get(tLabels.size() - 1), -1, tLabels.size() - 2, SolConst.SType.THROWSTACK)));
	}
	
	/**
	 * This method redraws play stacks if the number of elements is out of sync with our actual GameBoard,
	 * sets the correct card in each final stack and updates the drawing and throw stack.
	 */
	private void updateBoard() {
		try {updatePlayStacks();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null);}
		try {updateFinalStacks();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null);}
		try {updateDeck();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null);}
		try {updateThrowStack();} catch (Exception e) {statusBarController.log(ILogger.WARNING, e.getMessage(), null);} 
		
		if (board.isSolved())
			finishGameAndStartWinAnimation();
	}
	
	/**
	 * An algorithm to iterate through the throwstack and playstacks to move any available cards to finalstacks
	 * and continues to do so until no moves are made after iterating through those stacks
	 */
	@FXML private void lazySolve() {
		boolean somethingChanged = false;
		CardStack stack = board.getThrowStack();
		somethingChanged = tryMoveTopCardToFinalStack(stack);
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			stack = board.getPlayStack(i);
			if (tryMoveTopCardToFinalStack(stack))
				somethingChanged = true;
		}
		if (somethingChanged) {
			updateBoard();
			Undo.setDisable(true);
			lazySolve();
		}
	}
	
	/**
	 * Tries to move the top card of a stack to finalstacks
	 * @param stack the stack the card is in
	 * @return true if card was successfully moved, false otherwise
	 */
	private boolean tryMoveTopCardToFinalStack(CardStack stack) {
		int count = stack.getCardCount();
		try {
			board.moveToFinalStacks(stack.peek(), stack);
		} catch (Exception e) {
			;	//We ignore exceptions because this indicates that the top card cannot be legally moved to a final stack (IllegalArgument), 
				//or the stack is empty (IndexOutOfBounds) (the user shouldn't be overloaded with messages, and only needs to see that the card isn't moved anywhere)
		}
		if (count != stack.getCardCount())
			return true;
		return false;
	}
	
	/**
	 * Disables undo and solve buttons, and makes the window fixed in size before starting a win animation
	 */
	private void finishGameAndStartWinAnimation() {
		Undo.setDisable(true);
		Solve.setDisable(true);
		//winAnimation depends on the edges of the window to be static to look right
		stage.setResizable(false);

		labels.entrySet().removeIf(c -> c.getKey().toString().charAt(0) != 'F');
		Double topDeltaY = AnchorPane.getTopAnchor(FinalStacks);
		labels.forEach((k, list) -> {
			for (Label l: list) {
				l.setOnMouseClicked(null);
				l.setOnDragDetected(null);
				//I move all the cards in all final stacks from FinalStacks Pane too our Root Pane, so that we can use the size of
				//the parent for all the cards (now Root) to get the edges of our window (for collision detection)
				if (list.get(0) != l) {
					double posX = l.getParent().getLayoutX() + l.getTranslateX();
					Root.getChildren().add(l);
					FinalStacks.getChildren().remove(l);
					l.setTranslateX(0);
					l.relocate(posX, topDeltaY);
		}}});
		winAnimate = new WinAnimation(labels, Root, Root.getChildren().size());
		statusBarController.log(ILogger.INFO, StatusBarController.GAMEWON, null);
		winAnimate.start();
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
	 * Translates a final stack to correct position
	 * @param l List of labels that represents a final stack
	 * @param i the index of the final stack (0-3)
	 */
	private void finalStackTranslate(List<Label> l, int i) {
		if (i < 0 || i >= SolConst.SUITS)
			throw new IllegalArgumentException("The index of the final stack must be between 0 and " + (SolConst.SUITS - 1));
		int width = windowWidth();
		int xOffset =  3*width/7 + width/120;
		float xFactor = (float) (width/7.35);
		
		FinalStacks.setLayoutX(xOffset);
		
		for (Label label : l)
			label.setTranslateX(xFactor*i);
	}
	
	/**
	 * translates the deck and throwStack to correct positon, and shows the top cards of the throwStack 
	 * @param cardsDealt maximum number of cards to show from the throwStack
	 */
	private void deckAndThrowStackTranslate(int cardsDealt) {	
		int width = windowWidth();
		int xOffset = (int) (width/35);
		float xFactor = (float) (width/7.35);
		ThrowStack.setTranslateX(Math.round(xOffset + xFactor));
		Deck.setTranslateX(xOffset);
		
		int tsize = getThrowStackLabels().size();
		for (Label l : getThrowStackLabels())
			l.setTranslateX(0);
		for (int i = 0; i < cardsDealt; i++)
			if (tsize + i - cardsDealt > 0)
				getThrowStackLabels().get(i + tsize - cardsDealt).setTranslateX(12*(i));
	}
	
	/**
	 * Translates a play stack to correct position
	 * @param l List of labels that represents a play stack
	 * @param i the index of the final stack (0-6)
	 */	
	private void playStackTranslate(List<Label> l, int i) {
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
	 * @param l Label being dragged
	 * @param indexOfStacks the index of the stack (if playStack or finalStack)
	 * @param indexOfList the index of the label IN the stack
	 * @param stackName the stackName of the stack the label is in
	 */
	private void dragDetected(MouseEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.SType stackName) {
		WritableImage img;
		Dragboard db = l.startDragAndDrop(TransferMode.MOVE);
		double scale = Screen.getPrimary().getOutputScaleX();
		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(Transform.scale(scale, scale));
		draggedLabel = l;
		dragParentCardStack = (CardStack) board.getStackbyName(stackName);
		String dbFromStack = "" + ((char) (stackName.toString().charAt(0))) + indexOfStacks + indexOfList;
	    ClipboardContent content = new ClipboardContent();
	    content.putString(dbFromStack);
	    db.setContent(content);
		boolean ignoreCardsOnTop = false;

		for (Map.Entry<SolConst.SType, List<Label>> labellist : labels.entrySet()) {
			SType key = labellist.getKey();
			if ((key.equals(SType.THROWSTACK) || key.toString().charAt(0) == SType.F0.toString().charAt(0)) && labellist.getValue().contains(l))
				ignoreCardsOnTop = true;
		}
	 
		if (ignoreCardsOnTop) {
			img = new WritableImage((int) Math.round(l.getWidth() * scale), (int) Math.round(l.getHeight() * scale));
			l.snapshot(snapshotParameters, img);
			draggedLabel.setVisible(false);
		} else {
			Pane stackOfCards = new Pane();
			boolean addCards = false;
			int indexOfL = getPlayStackLabels(indexOfStacks).indexOf(l);
			for (int i = indexOfL; i < getPlayStackLabels(indexOfStacks).size(); i++) {
				if (addCards || getPlayStackLabels(indexOfStacks).get(i).equals(l)) {
					addCards = true;
					ImageView view = new ImageView(getPlayStackLabels(indexOfStacks).get(i).snapshot(snapshotParameters,null));
					view.setY(15*(i - indexOfL));
					view.setPreserveRatio(true);
					view.setFitHeight(getPlayStackLabels(indexOfStacks).get(i).getHeight());
					view.setFitWidth(getPlayStackLabels(indexOfStacks).get(i).getWidth());
					getPlayStackLabels(indexOfStacks).get(i).setVisible(false);
					stackOfCards.getChildren().add(view);
				}
			}
			img = new WritableImage(
				(int) Math.round(l.getWidth() * scale),
				(int) Math.round(l.getHeight() * scale) + 15*(getPlayStackLabels(indexOfStacks).size() - indexOfL - 1));
			stackOfCards.snapshot(snapshotParameters,img);
		}
	    db.setDragView(img, event.getX(), event.getY());
	    event.consume();
	}
	
	/** dragOverEvent makes the target able to accept cards */
	EventHandler <DragEvent> dragOverEvent = new EventHandler <DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
            //draggedLabel.setCursor(Cursor.DISAPPEAR);
            event.consume();
        }
	};
	
	/** dragDoneEvent sets the label visible again (triggered when card is dropped on a non-target) */
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
	 * @param stack the stack to show top card of 
	 */
	private void revealCard(CardStack stack) {
		statusBarController.clearStatusBar();
		board.revealCard(stack);
		Undo.setDisable(true);
		updateBoard();
	}
	
	/**
	 * Handles a card label being dropped on another label, and moves that card in our GameBoard 
	 * @param event the event of dropping the card 
	 * @param label label to drop card on
	 * @param indexOfStacks index of which final/play stack to drop on
	 * @param indexOfList index of the card to move (in the stack it is in before move)
	 */
	private void drop(DragEvent event, Label label, int indexOfStacks) {
		event.consume();
		statusBarController.clearStatusBar();
		Dragboard db = event.getDragboard();
		if (!db.hasString()) throw new IllegalArgumentException("The dragboard is empty for " + event.getSource());
		dropLabel = label;
		for (Node relatedLabel : label.getParent().getChildrenUnmodifiable())
			relatedLabel.setVisible(true);
		if (draggedLabel == dropLabel) return;

		int inStackIndex = 0;
		char targetStack = ((Label) event.getTarget()).getUserData().toString().charAt(0);
		char fromType = db.getString().charAt(0);
		
		if (fromType != 'T') {
			inStackIndex = Integer.parseInt(db.getString().substring(2));
		} else {
			inStackIndex = board.getThrowStack().size() - 1;
		}

		try {
			switch (targetStack) {
			case 'p' -> {
				board.moveCard(inStackIndex, dragParentCardStack, board.getPlayStack(indexOfStacks));
			} case 'f' -> {
				board.moveCard(inStackIndex, dragParentCardStack, board.getFinalStack(indexOfStacks));
			}}
		} catch (Exception e) {
			statusBarController.log(ILogger.WARNING, "Not a legal move", e);
		}
		canUndo();
		updateBoard();
	}

	/**
	 * Tries to move the top card of a cardStack to the final stacks
	 * @param event The event of doubleclicking 
	 * @param stack The stack to attempt to move the top card from
	 */
	private void doubleClickCard(MouseEvent event, CardStack stack) {
		if (event.getButton().equals(MouseButton.PRIMARY)){
            if (event.getClickCount() == 2) {
            	Card card = stack.peek();            	
            	if (tryMoveTopCardToFinalStack(stack)) {
            		statusBarController.log(ILogger.FINE, "Card moved to a final stack: " + card, null);
            		canUndo();
            		statusBarController.clearStatusBar();
            		updateBoard();
            	} else {
            		statusBarController.log(ILogger.WARNING, "No final stack to legally move " + card + " to.", null);
            	} 
            }
        }
	}
	
}