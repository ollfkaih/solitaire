package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.Card;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;


public class SolitaireController {
	
	private final static String IMGDIR = "img/";
	
	private GameBoard stacks;
	private CardDeck deck;
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private Label DrawStack;
	@FXML private Label ThrowStack;
	
	List<Label>[] p = (List <Label>[]) new ArrayList[GameBoard.PLAYSTACKSNUM];
	Label[] f = new Label[GameBoard.SUITS];
	private Label draggedLabel;
	private Label dropLabel;
	private CardStack dragParent;
	
	@FXML
	private void initialize() {
			
		deck = new CardDeck(GameBoard.CARDSINSUITE);
		for (int i = 0; i < 3; i++)
			deck.shufflePerfectly();
		stacks = new GameBoard(deck);

		for (int i = 0; i < GameBoard.PLAYSTACKSNUM; i++) {
			p[i] = new ArrayList<>();
		}
		DrawStack.setText("");
		setCustomImage(DrawStack, 'b');
		
		char targetStack = 't';
	    ThrowStack.setUserData(targetStack);
		setCustomImage(ThrowStack,'e');
		ThrowStack.setOnDragDetected((MouseEvent event) -> dragDetected(event, ThrowStack, -1, -1, stacks.getThrowStack().getStackName()));
		ThrowStack.setOnMouseClicked(doubleClickCard);
		
		updateStack();
	}
	
	@FXML
	void handleClickDrawStack() {
		stacks.deal(3);
		if (!stacks.getThrowStack().isEmpty()) {
			Card topCard = stacks.getThrowStack().get(stacks.getThrowStack().size() - 1);
			ThrowStack.setText(topCard.toString());
			setCardImage(ThrowStack, topCard);
		} else {
			ThrowStack.setText("Empty\nthrowstack");
			setCustomImage(ThrowStack,'e');
		}
	}
	
	/** 
	 * setCustomImage(Label, char) sets the top card image to the backside of a card deck
	 * @param stack
	 */
	private static boolean setCustomImage(Label stack, char imgToShow) {
		String imgDir = IMGDIR;
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
	private static Image cardToImg(Card card) {
		String imgDir = IMGDIR;
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
	
	void updateStack() {
		//TODO: REMOVE console printout
		for (int i = 0; i < 7; i++)
			System.out.println(stacks.getPlayStack(i));
		
		for (int i = 0; i < GameBoard.PLAYSTACKSNUM; i++) {
			if (p[i].size() != stacks.getPlayStack(i).getCardCount()) {
				for (Label cardLabel: p[i]) {
					PlayStacks.getChildren().remove(cardLabel);
				}
				for (int j = 0; j < stacks.getPlayStack(i).getCardCount(); j++) {
					try {
						Label label = new Label(stacks.getPlayStack(i).get(j).toString());
						p[i].add(label);
						pTranslate(p[i].get(j), i, j);
					} catch (Exception e) {
						e.printStackTrace();
					}
				
					char targetStack = 'p';
				    p[i].get(j).setUserData(targetStack);
			    	
					int thisIndexi = i;
					int thisIndexj = j;
			    	p[i].get(j).setOnDragDetected((MouseEvent event) -> dragDetected(event, p[thisIndexi].get(thisIndexj), thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
			    	p[i].get(j).setOnDragOver(dragOverEvent);	
			    	p[i].get(j).setOnDragDropped((DragEvent event) -> drop(event, p[thisIndexi].get(thisIndexj), thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
			    	p[i].get(j).setOnMouseClicked(doubleClickCard);
			    	PlayStacks.getChildren().add(p[i].get(j));
				}
				if (stacks.getPlayStack(i).isEmpty()) {
					p[i].add(new Label("Empty\npstack"));
					setCustomImage(p[i].get(0), 'e');
					PlayStacks.getChildren().add(p[i].get(0));
				}
			}
			for (int j = 0; j < stacks.getPlayStack(i).getCardCount(); j++) {
				try {
					Card topCard = stacks.getPlayStack(i).get(j);
					p[i].get(j).setText(topCard.toString());
					setCardImage(p[i].get(j),topCard);
				} catch (Exception IllegalArgumentException) {
					p[i].get(j).setText("Empty\nstack");
					setCustomImage(p[i].get(j),'e');
				} finally {
					p[i].get(j).setTextFill(Paint.valueOf("white"));
				}
			}
		}
		for (int i = 0; i < GameBoard.SUITS; i++) {
			if (stacks.getFinalStack(i).empty()) 
				f[i] = new Label("Empty\nFstack");
			else 
				f[i] = new Label(stacks.getFinalStack(i).peek().toString());
			try {
				//Events need to know what stack this card is in 
				char targetStack = 'f';
			    f[i].setUserData(targetStack);
				
			    int thisIndexi = i;
			    int thisIndexj = stacks.getFinalStack(i).getCardCount() - 1;
			    f[i].setOnDragDetected((MouseEvent event) -> dragDetected(event, f[thisIndexi], thisIndexi, thisIndexj, stacks.getFinalStack(thisIndexi).getStackName()));
				f[i].setTranslateX(70 + 90*i);
				f[i].setTranslateY(50);
				
				f[i].setOnDragOver(dragOverEvent);	
				f[i].setOnDragDropped((DragEvent event) -> drop(event, f[thisIndexi], thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
				
				Card topCard = stacks.getFinalStack(i).peek();
				f[i].setText(topCard.toString());
				setCardImage(f[i], topCard);
			} catch (Exception IllegalArgumentException) {
				f[i].setText("Empty\nstack");
				setCustomImage(f[i],'e');
			} finally {
				f[i].setTextFill(Paint.valueOf("white"));
				FinalStacks.getChildren().add(f[i]);
			}

		}
		try {
			Card topCard = stacks.getThrowStack().get(stacks.getThrowStack().size() - 1);
			ThrowStack.setText(topCard.toString());
			setCardImage(ThrowStack, topCard);
			System.out.println(String.format("Cards in the throwStack: %s", stacks.getThrowStack().toString()));
		} catch (Exception NullPointerException) {
			ThrowStack.setText("Empty\nthrowstack");
			setCustomImage(ThrowStack,'e');
		} finally {
			ThrowStack.setTextFill(Paint.valueOf("white"));
		}
	}
	
	//TODO: Remove unused parameter j, cards are not shown diagonally
	private void pTranslate(Label l, int i, int j) {
		l.setTranslateX(20 + 84*i); 
		l.setTranslateY(15*j);
	}
	
	private void dragDetected(MouseEvent event, Label l, int indexOfStacks, int indexOfList, Card.Stack stackName) {
		
		draggedLabel = l;
		dragParent = stacks.findCard(Card.stringToCard(l.getText().toString()));
		Dragboard db = l.startDragAndDrop(TransferMode.MOVE);
		
		//l.snapshot ? for rendering dragview?
		String dbFromStack = "" + ((char) (stackName.toString().charAt(0) + 32)) + indexOfStacks + indexOfList; //Ascii char A -> a has a delta of 32 
		//TODO: remove
		//System.out.println("dbFromStack: " + dbFromStack);
		
	    ClipboardContent content = new ClipboardContent();
	    //content.put(JAVA_FORMAT, draggedItem.getValue());
	    content.putString(dbFromStack);
	    db.setContent(content);
	    db.setDragView(l.snapshot(null, null));
	    event.consume();
	}
	
	EventHandler <DragEvent> dragOverEvent = new EventHandler <DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
            
            event.consume();
        }
	};
	
	EventHandler <MouseEvent> doubleClickCard = new EventHandler <MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
	        if(event.getButton().equals(MouseButton.PRIMARY)){
	            if(event.getClickCount() == 2) {
	            	try {
	            		System.out.println("Double-clicked:" + ((Label) event.getSource()).getText());
	            		stacks.moveToFinalStacks(((Label) event.getSource()).getText());
	            	} catch (Exception e) {
	            		e.printStackTrace();
		            	System.out.println("Could not move to a final stack: " + ((Label) event.getSource()).getText());
	            	} finally {
	            		updateStack();
	            	}
	            }
	        }
		}
	};
	
	private void drop(DragEvent event, Label l, int indexOfStacks, int indexOfList, Card.Stack stackName) {
		Dragboard db = event.getDragboard();
		if (!db.hasString())
			throw new IllegalArgumentException("The dragboard is empty for " + event.getSource());
		dropLabel = l;
		
		if (draggedLabel == dropLabel)
			return;
		int inStackIndex = 0;
		char targetStack = ((Label) event.getTarget()).getUserData().toString().charAt(0);
		char fromType = db.getString().charAt(0);
		
		if (fromType != 't') {
			inStackIndex = Integer.parseInt(db.getString().substring(2));
		} else {
			inStackIndex = stacks.getThrowStack().size() - 1;
		}

		switch (targetStack) {
		case 'p' -> {
			stacks.moveCard(inStackIndex, dragParent, stacks.getPlayStack(indexOfStacks));
		} case 'f' -> {
			stacks.moveCard(inStackIndex, dragParent, stacks.getFinalStack(indexOfStacks));
		}
		}
		updateStack();
		event.consume();
	}
}
