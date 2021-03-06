package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.Card;
import solitaire.model.CardDeck;
import solitaire.model.CardStack;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;


public class SolitaireController {
	
	private GameBoard stacks;
	private CardDeck deck;
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private Label DrawStack;
	@FXML private Label ThrowStack;
	
	@SuppressWarnings("unchecked")
	List<Label>[] p = (List <Label>[]) new ArrayList[SolConst.PLAYSTACKSNUM];
	Label[] f = new Label[SolConst.SUITS];
	private Label draggedLabel;
	private Label dropLabel;
	private CardStack dragParent;
	
	@FXML
	private void initialize() {
			
		deck = new CardDeck(SolConst.CARDSINSUITE);
		for (int i = 0; i < 3; i++)
			deck.shufflePerfectly();
		stacks = new GameBoard(deck);
		
		for (int i = 0; i < SolConst.SUITS; i++) {
			f[i] = new Label(null);
			FinalStacks.getChildren().add(f[i]);
		}
		
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			p[i] = new ArrayList<>();
		}
		DrawStack.setText(null);
		setCustomImage(DrawStack, 'b');
		
		ThrowStack.setText(null);
		setCustomImage(ThrowStack,'e');
		
		updateStack();
	}
	
	private void updateDrawStack() {
		if (!stacks.getThrowStack().isEmpty()) {
			Card topCard = stacks.getThrowStack().get(stacks.getThrowStack().size() - 1);
			ThrowStack.setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, stacks.getThrowStack().peek(), stacks.getThrowStack()));
			ThrowStack.setOnDragDetected((MouseEvent event) -> dragDetected(event, ThrowStack, -1, -1, stacks.getThrowStack().getStackName()));
			setCardImage(ThrowStack, topCard);
		} else {
			ThrowStack.setOnMouseClicked(null);
			ThrowStack.setOnDragDetected(null);
			setCustomImage(ThrowStack,'e');
		}
	}
	
	@FXML
	void handleClickDrawStack() {
		stacks.deal(3);
		updateDrawStack();
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
	
	void updateStack() {
		//TODO: REMOVE console printout
		for (int i = 0; i < 7; i++)
			System.out.println(stacks.getPlayStack(i));
		
		for (int i = 0; i < SolConst.PLAYSTACKSNUM; i++) {
			if (p[i].size() != stacks.getPlayStack(i).getCardCount()) {
				for (Label cardLabel: p[i]) {
					PlayStacks.getChildren().remove(cardLabel);
				}
				for (int j = 0; j < stacks.getPlayStack(i).getCardCount(); j++) {
					Label label = new Label(null);
					p[i].add(label);
					pTranslate(p[i].get(j), i, j);
					
					char targetStack = 'p';
				    p[i].get(j).setUserData(targetStack);
			    	
					int thisIndexi = i;
					int thisIndexj = j;
					//Hidden cards should not be draggable or doubleclickable
					if ( ! stacks.getPlayStack(i).isHidden(j)) {
						Card topCard = stacks.getPlayStack(i).get(j);
						setCardImage(p[i].get(j),topCard);

						p[i].get(j).setOnDragDetected((MouseEvent event) -> dragDetected(event, p[thisIndexi].get(thisIndexj), thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
			    		p[i].get(j).setOnMouseClicked((MouseEvent event) -> doubleClickCard(event, stacks.getPlayStack(thisIndexi).get(thisIndexj), stacks.getPlayStack(thisIndexi)));
					} else {
						setCustomImage(p[i].get(j),'e');
					}
			    	p[i].get(j).setOnDragOver(dragOverEvent);
			    	//All cards should be droptargets; if you drop a card on a hidden card, the drop() method attempts to put it at the top of this stack
			    	p[i].get(j).setOnDragDropped((DragEvent event) -> drop(event, p[thisIndexi].get(thisIndexj), thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
			    	PlayStacks.getChildren().add(p[i].get(j));
				}
				if (stacks.getPlayStack(i).isEmpty()) {
					setCustomImage(p[i].get(0), 'e');
					PlayStacks.getChildren().add(p[i].get(0));
				}
			}
		}
		for (int i = 0; i < SolConst.SUITS; i++) {
			//TODO: REMOVE
			/*for (Label cardLabel: f) {
				PlayStacks.getChildren().remove(cardLabel);
			}
			f[i] = new Label(null);
			if (stacks.getFinalStack(i).empty()) 
				f[i] = new Label(null);
			else 
				f[i] = new Label(stacks.getFinalStack(i).peek().toString());*/
			try {
				//Events need to know what stack this card is in 
				char targetStack = 'f';
			    f[i].setUserData(targetStack);
				
			    int thisIndexi = i;
			    int thisIndexj = stacks.getFinalStack(i).getCardCount() - 1;
			    f[i].setOnDragDetected((MouseEvent event) -> dragDetected(event, f[thisIndexi], thisIndexi, thisIndexj, stacks.getFinalStack(thisIndexi).getStackName()));
				f[i].setTranslateX(85*i); 
				f[i].setTranslateY(25);
				
				f[i].setOnDragOver(dragOverEvent);	
				f[i].setOnDragDropped((DragEvent event) -> drop(event, f[thisIndexi], thisIndexi, thisIndexj, stacks.getPlayStack(thisIndexi).getStackName()));
				
				Card topCard = stacks.getFinalStack(i).peek();
				setCardImage(f[i], topCard);
			} catch (Exception IllegalArgumentException) {
				setCustomImage(f[i],'e');
			}
		}
		updateDrawStack();
	}
	
	//TODO: Remove unused parameter j, cards are not shown diagonally
	private void pTranslate(Label l, int i, int j) {
		l.setTranslateX(20 + 85*i); 
		l.setTranslateY(15*j);
	}

	private void dragDetected(MouseEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.Stack stackName) {
		
		Dragboard db = l.startDragAndDrop(TransferMode.MOVE);
		draggedLabel = l;
		
		dragParent = stacks.getStackbyName(stackName);
		
		String dbFromStack = "" + ((char) (stackName.toString().charAt(0) + 32)) + indexOfStacks + indexOfList; //Ascii char A -> a has a delta of 32 

	    ClipboardContent content = new ClipboardContent();
	    content.putString(dbFromStack);
	    db.setContent(content);
	    //TODO: Make snapshot render cards on top ?
	    db.setDragView(l.snapshot(null, null), event.getX(), event.getY());
	    event.consume();
	}
	
	//dragOverEvent sets makes the target able to accept cards
	EventHandler <DragEvent> dragOverEvent = new EventHandler <DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
                        
            event.consume();
        }
	};
		
	private void drop(DragEvent event, Label l, int indexOfStacks, int indexOfList, SolConst.Stack stackName) {
		Dragboard db = event.getDragboard();
		if (!db.hasString()) throw new IllegalArgumentException("The dragboard is empty for " + event.getSource());
		dropLabel = l;
		
		if (draggedLabel == dropLabel) return;
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

	private void doubleClickCard(MouseEvent event, Card card, CardStack stack) {
		if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2) {
            	try {
            		System.out.println("Double-clicked:" + card.toString());
            		stacks.moveToFinalStacks(card, stack);
            	} catch (Exception e) {
            		e.printStackTrace();
	            	System.out.println("Could not move to a final stack: " + ((Label) event.getSource()).getText());
            	} finally {
            		updateStack();
            	}
            }
        }
	}
}
