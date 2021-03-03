package solitaire.fxui;

import solitaire.model.CardStacks;
import solitaire.model.Card;
import solitaire.model.CardDeck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
	
	private CardStacks stacks;
	private CardDeck deck;
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private Label DrawStack;
	@FXML private Label ThrowStack;
	
	List<Label>[] p = (List <Label>[]) new ArrayList[CardStacks.PLAYSTACKSNUM];
	Label[] f = new Label[CardStacks.SUITS];
	//Label d, t;
	
	@FXML
	private void initialize() {
			
		deck = new CardDeck(CardStacks.CARDSINSUITE);
		for (int i = 0; i < 3; i++)
			deck.shufflePerfectly();
		stacks = new CardStacks(deck);

		for (int i = 0; i < CardStacks.PLAYSTACKSNUM; i++) {
			p[i] = new ArrayList<>();
			for (int j = 0; j < stacks.getPlayStacks()[i].size() ; j++) {
				Label label = new Label(stacks.getPlayStacks()[i].get(j).toString());
				p[i].add(label);
				pTranslate(p[i].get(j), i, j);
			
				//Events need to know what stack this card is in 
		    	int[] stackShortArr = new int[3]; 
		    	stackShortArr[0] = 'p';
		    	stackShortArr[1] = i;
		    	stackShortArr[2] = j;
		    	p[i].get(j).setUserData(stackShortArr);
			
		    	p[i].get(j).setOnDragDetected(dragDetectedEvent);
		    	p[i].get(j).setOnDragOver(dragOverEvent);	
		    	p[i].get(j).setOnDragDropped(cardDroppedEvent);
		    	p[i].get(j).setOnMouseClicked(doubleClickCard);
		    	PlayStacks.getChildren().add(p[i].get(j));
			}
		}
				
		for (int i = 0; i < CardStacks.SUITS; i++) {
			try {
				f[i] = new Label(stacks.getTopFinalStack(i).toString());
			} catch (Exception NullPointerException) {
				f[i] = new Label("Empty\nplaceholder");
			}
			
		    //Events need to know what stack this card is in 
			int[] stackShortArr = new int[2];
		    stackShortArr[0] = 'f';
		    stackShortArr[1] = i;
		    f[i].setUserData(stackShortArr);
			
		    f[i].setOnDragDetected(dragDetectedEvent);
			f[i].setTranslateX(70 + 90*i);
			f[i].setTranslateY(50);
			
			f[i].setOnDragOver(dragOverEvent);	
			f[i].setOnDragDropped(cardDroppedEvent);
			
			FinalStacks.getChildren().add(f[i]);
		}
		
		DrawStack.setText("");
		setCustomImage(DrawStack, 'b');
		
		int[] stackShortArr = new int[2]; 
		stackShortArr[0] = 't';
		
		ThrowStack.setText("Empty\nthrowstack");
		ThrowStack.setUserData(stackShortArr);
		ThrowStack.setOnDragDetected(dragDetectedEvent);
		ThrowStack.setOnMouseClicked(doubleClickCard);
		
		updateStack();
	}
	
	@FXML
	void handleClickDrawStack() {
		stacks.deal(3);
		if (!stacks.getThrowStack().isEmpty()) {
			Card topCard = stacks.getThrowStack().get(stacks.getThrowStack().size() - 1);
			ThrowStack.setText(topCard.toString());
			setImage(ThrowStack, topCard);
		} else {
			ThrowStack.setText("Empty\nthrowstack");
			setCustomImage(ThrowStack,'e');
		}
	}
	
	/** 
	 * setImageBack(Label, char) sets the top card image to the backside of a card deck
	 * @param stack
	 */
	private static void setCustomImage(Label stack, char imgToShow) {
		String imgDir = "C:\\Users\\Olav\\Downloads\\kort\\png\\";
		String ext = ".png";
		String imgName;
		Image img;
		ImageView view;
		
		switch (imgToShow) {
		case 'b' -> imgName = "1B";
		case 'e' -> imgName = "2J"; //empty stack
		default -> throw new IllegalArgumentException("Illegal image to show: " + imgToShow);
		}
		//System.out.println(stack + " " + imgName + "STRING: " + imgDir + imgName + ext);
		try {
			img = new Image(new FileInputStream(imgDir + imgName + ext));
			view = new ImageView(img);
			view.setFitHeight(img.getHeight()/3);
			view.setFitWidth(img.getWidth()/3);
			stack.setGraphic(view);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Could not find file");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not find file");
		}
				
	}
	private static void setImage(Label stack, Card card) {
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
		}

	}
	private static Image cardToImg(Card card) {
		String imgDir = "C:\\Users\\Olav\\Downloads\\kort\\png\\";
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
				img = new Image(new FileInputStream(imgDir + card.getFace() + card.getSuit() + ext));
			} else {
				img = new Image(new FileInputStream(imgDir + faceVal + card.getSuit() + ext));
			}
			return img;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Could not find file");
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("Could not get card face");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("HUH?");
		}
		return null;
	}
	
	void updateStack() {
		//TODO: REMOVE console printout
		for (int i = 0; i < 7; i++)
			System.out.println(stacks.getPlayStacks()[i]);
		
		for (int i = 0; i < CardStacks.PLAYSTACKSNUM; i++) {
			if (p[i].size() != stacks.getPlayStacks()[i].size()) {
				for (Label cardLabel: p[i]) {
					PlayStacks.getChildren().remove(cardLabel);
				}
				for (int j = 0; j < stacks.getPlayStacks()[i].size(); j++) {
					Label label = new Label(stacks.getPlayStacks()[i].get(j).toString());
					p[i].add(label);
					pTranslate(p[i].get(j), i, j);
				
					//Events need to know what stack this card is in 
			    	int[] stackShortArr = new int[3]; 
			    	stackShortArr[0] = 'p';
			    	stackShortArr[1] = i;
			    	stackShortArr[2] = j;
			    	p[i].get(j).setUserData(stackShortArr);
				
			    	p[i].get(j).setOnDragDetected(dragDetectedEvent);
			    	p[i].get(j).setOnDragOver(dragOverEvent);	
			    	p[i].get(j).setOnDragDropped(cardDroppedEvent);
			    	p[i].get(j).setOnMouseClicked(doubleClickCard);
			    	PlayStacks.getChildren().add(p[i].get(j));
				}
				if (stacks.getPlayStacks()[i].isEmpty()) {
					p[i].add(new Label("Empty pstack"));
					PlayStacks.getChildren().add(p[i].get(0));
					setCustomImage(p[i].get(0), 'e');
				}
			}
			for (int j = 0; j < stacks.getPlayStacks()[i].size(); j++) {
				try {
					Card topCard = stacks.getPlayStacks()[i].get(j);
					p[i].get(j).setText(topCard.toString());
					setImage(p[i].get(j),topCard);
				} catch (Exception IllegalArgumentException) {
					p[i].get(j).setText("Empty\nstack");
					setCustomImage(p[i].get(j),'e');
				} finally {
					p[i].get(j).setTextFill(Paint.valueOf("white"));
				}
			}
		}
		for (int i = 0; i < CardStacks.SUITS; i++) {
			try {
				Card topCard = stacks.getTopFinalStack(i);
				f[i].setText(topCard.toString());
				setImage(f[i], topCard);
			} catch (Exception IllegalArgumentException) {
				f[i].setText("Empty\nstack");
				setCustomImage(f[i],'e');
			} finally {
				f[i].setTextFill(Paint.valueOf("white"));
			}
		}
		try {
			Card topCard = stacks.getThrowStack().get(stacks.getThrowStack().size() - 1);
			ThrowStack.setText(topCard.toString());
			setImage(ThrowStack, topCard);
		} catch (Exception NullPointerException) {
			ThrowStack.setText("Empty\nthrowstack");
			setCustomImage(ThrowStack,'e');
		} finally {
			ThrowStack.setTextFill(Paint.valueOf("white"));
		}
	}
	
	private void pTranslate(Label l, int i, int j) {
		l.setTranslateX(20 + 84*i/* + 3*j*/);
		l.setTranslateY(15*j);
	}
	
	EventHandler <MouseEvent> dragDetectedEvent = new EventHandler <MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			
			int[] stackShortArr = (int[]) ((Label) event.getSource()).getUserData();
			
			switch ((char) stackShortArr[0]) {
			case 'p': {
				try {
					Dragboard db = p[stackShortArr[1]].get(stackShortArr[2]).startDragAndDrop(TransferMode.MOVE);
					
			        ClipboardContent content = new ClipboardContent();
			        content.putString("p" + stackShortArr[1] + "\t" + p[stackShortArr[1]].get(stackShortArr[2]).getText());
			        db.setContent(content);
			        //db.setDragView(cardToImg(Card.stringToCard(p[stackShortArr[1]].get(stackShortArr[2]).getText())));
			        System.out.println(db.getContent(DataFormat.PLAIN_TEXT));
				} catch (Exception e) {
					throw new IllegalArgumentException("Card cannot be moved here.");
				}
				break;
			} case 'f': {
				try {
					Dragboard db = f[stackShortArr[1]].startDragAndDrop(TransferMode.MOVE);
					
			        ClipboardContent content = new ClipboardContent();
			        content.putString("f" + stackShortArr[1] + "\t" + f[stackShortArr[1]].getText());
			        db.setContent(content);
				} catch (Exception e) {
					throw new IllegalArgumentException("Card cannot be moved here.");
				}
				break;
			} case 't': {
				try {
					Dragboard db = ThrowStack.startDragAndDrop(TransferMode.MOVE);
					
			        ClipboardContent content = new ClipboardContent();
			        content.putString("t" + "\t\t" + ((Label) event.getSource()).getText());
			        db.setContent(content);
			        //System.out.println(content);
				} catch (Exception e) {
					throw new IllegalArgumentException("Card cannot be moved here.");
				}
				break;
			} default: {
				event.consume();
			}
			event.consume();
		}}
	};
	
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
	EventHandler <DragEvent> cardDroppedEvent = new EventHandler <DragEvent>() {
		@Override
		public void handle(DragEvent event) {
			event.acceptTransferModes(TransferMode.MOVE);
			
			System.out.println(event.getDragboard().getString());
			String cardString = event.getDragboard().getString().substring(3);
			char suit = cardString.charAt(0);
			int value = Integer.parseInt(cardString.substring(1));
			Card card = new Card(suit, value);
						
			String fromStack = event.getDragboard().getString().substring(0, 2);
			
			int[] stackShortArr = (int[]) ((Label) event.getSource()).getUserData();

			switch ((char) stackShortArr[0]) {
			case 'p': {
				try {
					stacks.moveCard(card, fromStack, "p" + stackShortArr[1]);
					updateStack();
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalArgumentException("Card cannot be moved to this playStack. Exception:" + e);
				}
				break;
			} case 'f': {
				try {
					stacks.moveCard(card, fromStack, "f" + stackShortArr[1]);
					updateStack();
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalArgumentException("Card cannot be moved to this finalStack. Exception:" + e);
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + (char) stackShortArr[0]);
			}
			event.consume();
		}
	};
}
