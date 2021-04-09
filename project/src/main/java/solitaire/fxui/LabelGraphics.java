package solitaire.fxui;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import solitaire.model.Card;
import solitaire.model.SolConst;

public final class LabelGraphics {
	private final static String IMAGEEXTENSION = ".png";
	private final static float cardScaler = (float) (1/2.9);
	public static enum SPECIALIMAGE {BACK,EMPTY}; 

	/**
	 * Creates an imageview of an image and assigns that to the given label
	 * @param label
	 * @param img
	 */
	private static void setLabelGraphicToView(Label label, Image img) {
		if (img == null)
			throw new IllegalArgumentException("Card image must not be null");
		ImageView view = new ImageView(img);
		view.setFitHeight(img.getHeight()*cardScaler);
		view.setFitWidth(img.getWidth()*cardScaler);
		view.setSmooth(true);
		label.setGraphic(view);
	}
	
	/**
	 * sets the image of a label to the backside of a card or a joker (empty stack image) 
	 * @param label the label to put a special image on
	 * @param imgToShow the type of special image to use
	 * @return True if image was successfully loaded and applied, false otherwise
	 */
	public static boolean setSpecialImage(Label label, SPECIALIMAGE imgToShow) {
		String imgDir = SolConst.IMGDIR;
		String fileName;
		Image img;
				
		switch (imgToShow) {
		case BACK -> fileName = "1B";
		case EMPTY -> fileName = "2J"; //we use a jack for empty stacks because I'm not a graphical artist
		default -> throw new IllegalArgumentException("Illegal image to show: " + imgToShow);
		}
		try {
			img = new Image(SolitaireController.class.getResourceAsStream(imgDir + fileName + IMAGEEXTENSION));
			setLabelGraphicToView(label, img);
		} catch (Exception e) {
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
	public static boolean setCardImage(Label label, Card card) {
		Image img;
		try {
			img = LabelGraphics.getImage(card);
			setLabelGraphicToView(label, img);
		} catch (Exception e) {
			return false;
		}
	return true;
	}

	/**
	 * Returns the appropriate image file to represent a card.
	 * @param card The card to represent
	 * @return
	 */
	public static Image getImage(Card card) {
		String imgDir = SolConst.IMGDIR;
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
				img = new Image(SolitaireController.class.getResourceAsStream(imgDir + card.getFace() + card.getSuit() + IMAGEEXTENSION));
			} else {
				img = new Image(SolitaireController.class.getResourceAsStream(imgDir + faceVal + card.getSuit() + IMAGEEXTENSION));
			}
			return img;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Could not get card face");
		} catch (Exception e) {
			throw new IllegalArgumentException("HUH? Error getting cardImage");
		}
	}
}
