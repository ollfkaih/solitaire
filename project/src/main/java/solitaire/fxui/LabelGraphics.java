package solitaire.fxui;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import solitaire.model.Card;
import solitaire.model.SolConst;

public final class LabelGraphics {
	private final static float cardScaler = (float) (1/2.9);

	/**
	 * Creates an imageview of an image and assigns that to the given label
	 * @param label
	 * @param img
	 */
	private static void setLabelGraphicToView(Label label, Image img) {
		ImageView view;
		view = new ImageView(img);
		view.setFitHeight(img.getHeight()*cardScaler);
		view.setFitWidth(img.getWidth()*cardScaler);
		view.setSmooth(true);
		label.setGraphic(view);
	}
	
	/** 
	 * setSpecialImage(Label, char) sets the top card image to the backside of a card carddeck
	 * @param stack
	 */
	public static boolean setSpecialImage(Label label, char imgToShow) throws IllegalArgumentException {
		String imgDir = SolConst.IMGDIR;
		String ext = ".png";
		String fileName;
		Image img;
				
		switch (imgToShow) {
		case 'b' -> fileName = "1B";
		case 'e' -> fileName = "2J"; //empty stack
		default -> throw new IllegalArgumentException("Illegal image to show: " + imgToShow);
		}
		try {
			img = new Image(SolitaireController.class.getResourceAsStream(imgDir + fileName + ext));
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
			img = IOHandler.getImage(card);
			setLabelGraphicToView(label, img);
		} catch (Exception e) {
			return false;
		}
	return true;
	}
}
