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
	private static final float takeSnapshotTrigger = 2.5f;
	private static final int xSpeed = 3; //fixed horizontal speed
	private static final double yAcceleration = 0.5; //acceleration vertically
	private static final int bottombarheight = 30; //TODO: Not hardcode
	private static final int MAXIMAGESONSCREEN = 3000; //For performance reasons, limit the number of imageviews on screen at the same time
	private AnchorPane Root;
	private long previousTime; //previous time card was moved
	private int thisLabelFrames; //number of frames we've been on a particular card
    private Map<SolConst.SType, List<Label>> finalLabels = new TreeMap<SolConst.SType, List<Label>>();
    private int cardValue = SolConst.CARDSINSUIT;
    private int stackIndex = 0; 
	private double prevTranslateX; //position in x-direction on previous frame when a snapshot was taken
	private double prevTranslateY; //position in y-direction on previous frame when a snapshot was taken
    private double randomX;
    private double randomY;
    private double vy; //velocity in y-direction
    private double vyprev; //velocity in y-direction on previous frame
	private boolean justCollided = false; //true on the frame just after a card collides with bottom
    private Random r = new Random();
	private int rootChildrentoKeep;
	private WritableImage img;
   
	/**
	 * A card animation to be played when the game is won (controlled with start() and stop() methods inherited from AnimationTimer)
	 * similar to the animation in the classic Solitaire game in Windows XP and earlier versions
	 * @param finalLabels A Map of labels to move/animate
	 * @param Root The parent of 
	 * @param childrenToKeep
	 */
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
     * Returns a pseudorandom double between 0.5 (exclusive) and 1 (inclusive) (mathematically (0.5d, 1d] ) 
     */
    private double randomDoublePositive() {
    	return 1d - r.nextDouble()/2d;
    }
    
    /**
     * Returns a new pseudorandom double between -1 and -0.5 or 0.5 and 1 (more often negative) (mathematically [-1d, -0.5d) U (0.5d, 1d] )
     */
    private double randomDouble() {
    	double rand = randomDoublePositive();
		//3 of 4 times, swap sign to make our card go left direction 
    	//(which looks more interestion, because our final stacks are on the right side of the screen) 
    	if (r.nextDouble() < 0.75)
    		rand = -rand;
    	return rand;
    }

    /**
     * Moves a label left or right and "bounces" it on the bottom of the window, then makes the label invisible when offscreen 
     * @param l the label to move
     */
    public void moveLabelFunny(Label l) {
    	double vx = randomX * xSpeed;
		vy = vyprev + randomY*yAcceleration;
    	l.setTranslateX(vx*thisLabelFrames);
		l.setTranslateY(l.getTranslateY() + vy);
		handleCollision(l);
		//If the card just collided with the bottom of the window, we take a snapshot, to make it look better
		//Or it will look like the card "tunneled" / skipped the bottom collision
		if (Math.abs(prevTranslateY - l.getTranslateY()) > takeSnapshotTrigger || Math.abs(prevTranslateX - l.getTranslateX()) > takeSnapshotTrigger || justCollided) {
			takeSnapShotandFreezeinPlace(l);
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
	void takeSnapShotandFreezeinPlace(Label l) {
		ImageView view = new ImageView(img);
		view.relocate(l.getLayoutX() + l.getTranslateX(), l.getLayoutY() + l.getTranslateY());
		Root.getChildren().add(view);
		if (Root.getChildren().size() > MAXIMAGESONSCREEN && Root.getChildren().get(rootChildrentoKeep - 1).getClass() == ImageView.class) {
			Root.getChildren().remove(rootChildrentoKeep - 1); //removes the oldest snapshots we've added
		}
	}

	@Override
	public void handle(long now) {
		//approximate frame limiter to 60fps
		if (1e-9*(now - previousTime) < 1d/60) {
			previousTime = previousTime - now;
			return;
		} else 
			previousTime = now;
		
		//TODO: Verify that randomX is never 0
		if (randomY == 0)
			switchStack(now);
		Label l = null;
		if (stackIndex < SolConst.SUITS && cardValue > 0) {
    		l = finalLabels.get(SType.valueOf("F" + stackIndex)).get(cardValue);
    		l.toFront();
		}
		else {
			//Go to next label with smaller face value, starting at first index; 
			//(E.g. after last king, go to first queen, after last queen, go to first jack etc.)
			cardValue--;
			stackIndex = 0;
			switchStack(now);
		}
		
		if (l != null) {
			if (! l.isVisible()) {
				//Go to next stack of same face value
				stackIndex++;
				switchStack(now);
			} else {
				moveLabelFunny(l);
				thisLabelFrames++;
			}
		}
	}
	
	/**
	 * Saves a snapshot of our label l and initializes variables 
	 * @param now
	 */
	private void switchStack(long now) {
		thisLabelFrames = 0;
		randomX = randomDouble();
		randomY = randomDoublePositive();
		vy = randomY;
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

	@Override
	/**
	 * Stops the winAnimation and removes all snapshots we've added to Root.
	 */
	public void stop() {
		super.stop();
		while (Root.getChildren().size() >= rootChildrentoKeep)
			Root.getChildren().remove(rootChildrentoKeep - 1);
		Root.getChildren().removeIf(e -> (e instanceof Label || e instanceof ImageView));
	}
}
