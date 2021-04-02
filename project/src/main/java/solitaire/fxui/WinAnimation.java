package solitaire.fxui;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import solitaire.model.SolConst;
import solitaire.model.SolConst.SType;

public class WinAnimation extends AnimationTimer{
	private static final int takeSnapshotTrigger = 2;
	private static final int xSpeed = 3;
	private static final double yAcceleration = 0.5; 
	private static final int bottombarheight = 30; //TODO: Not hardcode
	private AnchorPane Root;
	private int thisLabelFrames;
    private Map<SolConst.SType, List<Label>> finalLabels = new TreeMap<SolConst.SType, List<Label>>();
    private int cardValue = SolConst.CARDSINSUIT;
    private int stackIndex = 0; 
	private double prevTranslateX;
    private double randX;
    private double randY;
    private double vy; //velocity in y-direction
    private double vyprev; //velocity in y-direction on previous frame
	private double prevTranslateY; //previous y-translate position
	private boolean justCollided = false; //true on the frame just after a card collides with bottom
    private Random r = new Random();
	private int rootChildrentoKeep;
	private WritableImage img;
    
    WinAnimation (Map<SolConst.SType, List<Label>> finalLabels, AnchorPane Root, int childrenToKeep) {
        if (finalLabels.size() != SolConst.SUITS)
            throw new IllegalArgumentException("There should be four final stacks");
            finalLabels.entrySet().stream().forEach(e -> {
            if (e.getValue().size() != SolConst.CARDSINSUIT + 1) {
                throw new IllegalArgumentException("Each final stack should have " + SolConst.CARDSINSUIT + " cards");
            } 
        });
        this.finalLabels = finalLabels;
		this.Root = Root;
		this.rootChildrentoKeep = childrenToKeep;
    }
    
    /**
     * Returns a pseudorandom double between 0.5 and 1
     * @returnthe pseudorandom value
     */
    private double randomDoublePositive() {
    	return (r.nextDouble()/2 + 0.5);
    }
    
    /**
     * Returns a new pseudorandom double between -1 and -0.5 or 0.5 and 1 (more often negative)
     * @return the pseudorandom value
     */
    private double randomDouble() {
    	double rand = randomDoublePositive();
		//3 of 4 times, swap sign and make card go left
    	if (r.nextDouble() < 0.75)
    		rand = -rand;
    	return rand;
    }

    /**
     * Moves a label left or right and "bounces" it on the bottom of the window, then makes the label invisible when offscreen 
     * @param l the label to move
     */
    public void moveLabelFunny(Label l) {
    	double vx = randX * xSpeed;
		vy = vyprev + randY*yAcceleration;
    	l.setTranslateX(vx*thisLabelFrames);
		l.setTranslateY(l.getTranslateY() + vy);
		handleCollision(l);
		//If the card just collided with the bottom of the window, we take a snapshot, to make it look better
		//Or it will look like the card "tunneled" / skipped the bottom collision
		if (Math.abs(prevTranslateY - l.getTranslateY()) > takeSnapshotTrigger || Math.abs(prevTranslateX - l.getTranslateX()) > takeSnapshotTrigger || justCollided) {
			takeSnapShotandFreeze(l);
			prevTranslateY = l.getTranslateY();
			prevTranslateX = l.getTranslateX();
			justCollided = false;
		}
		//when left edge off right side of stage etc, make cardlabel invisible
    	if (l.getLayoutX() + l.getTranslateX() < -l.getWidth() || l.getLayoutX() + l.getTranslateX() > ((AnchorPane) l.getParent()).getWidth())
    		l.setVisible(false);
    }

	/**
	 * Checks if a label has collided with the bottom of the game board, and if so makes it go in the diametrically opposite direction and slows down the label.
	 * @param l The label to handle collision of
	 */
	private void handleCollision(Label l) {
		double labelBottom = l.getLayoutY() + l.getTranslateY() + l.getHeight();
		double windowHeight = ((AnchorPane) l.getParent()).getHeight() - bottombarheight;
		if (labelBottom > windowHeight /*|| l.getTranslateY() < -15*/) {
			double r = randomDoublePositive()/4 + 0.75;
			double dampenCollision = r * 0.8;
			vyprev = - dampenCollision * vy;
			//Move the cardlabel back up labelBottom - windowHeight units
			l.setTranslateY(l.getTranslateY() - labelBottom + windowHeight);
			justCollided = true;
        } else 
			vyprev = vy;
	}
	
	/**
	 * Adds the image img to Root at the position of the Label l
	 * Also removes old snapshots
	 * @param l Label to put the image at same position
	 */
	void takeSnapShotandFreeze(Label l) {
		ImageView view = new ImageView(img);
		view.relocate(l.getLayoutX() + l.getTranslateX(), l.getLayoutY() + l.getTranslateY());
		Root.getChildren().add(view);
		if (Root.getChildren().size() > 3000 && Root.getChildren().get(rootChildrentoKeep).getClass() == ImageView.class) {
			Root.getChildren().remove(rootChildrentoKeep); //removes the oldest snapshots we've added
		}
	}

	@Override
	public void handle(long now) {
		//TODO: Verify that randX is never 0
		if (randY == 0)
			switchStack(now);
		Label l = null;
		if (stackIndex < SolConst.SUITS && cardValue > 0) {
    		l = finalLabels.get(SType.valueOf("F" + stackIndex)).get(cardValue);
    		l.toFront();
		}
		else {
			//Go to next label below, starting at first index;
			cardValue--;
			stackIndex = 0;
			switchStack(now);
		}
		if (l != null) {
			if (! l.isVisible()) {
				//Go to next stack
				stackIndex++;
				switchStack(now);
			} else {
				moveLabelFunny(l);
				thisLabelFrames++;
			}
		}
	}
	
	/**
	 * Switches to a new stack and takes a snapshot of that image
	 * @param now
	 */
	private void switchStack(long now) {
		thisLabelFrames = 0;
		randX = randomDouble();
		randY = randomDoublePositive();
		vy = randY;
		vyprev = 0;
		if (stackIndex >= 0 && stackIndex < 4) {
			Label l = finalLabels.get(SType.valueOf("F" + stackIndex)).get(cardValue);
			try {
				img = new WritableImage((int) l.getWidth(), (int) l.getHeight());
			} catch (IllegalArgumentException e) {
				img = new WritableImage(80,105);
			}
			l.snapshot(null, img);
		}
		if (cardValue > 0)
			handle(now);
        else
            this.stop();
	}
}
