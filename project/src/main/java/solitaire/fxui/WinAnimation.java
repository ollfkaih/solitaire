package solitaire.fxui;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import solitaire.model.SolConst;
import solitaire.model.SolConst.SType;

public class WinAnimation extends AnimationTimer{
	int thisLabelFrames;
    private Map<SolConst.SType, List<Label>> finalLabels = new TreeMap<SolConst.SType, List<Label>>();
    int cardValue = SolConst.CARDSINSUIT;
    int stackIndex = 0; 
    double randX;
    double randY;
    Random r = new Random();
    
    WinAnimation (Map<SolConst.SType, List<Label>> finalLabels) {
        if (finalLabels.size() != SolConst.SUITS)
            throw new IllegalArgumentException("There should be four final stacks");
            finalLabels.entrySet().stream().forEach(e -> {
            if (e.getValue().size() != SolConst.CARDSINSUIT + 1) {
                throw new IllegalArgumentException("Each final stack should have " + SolConst.CARDSINSUIT + " cards");
            } 
        });
        randX = changeRandom();
        randY = changeRandomPos();
        this.finalLabels = finalLabels;
    }
    
    /**
     * Returns a pseudorandom double between 0.75 and 1
     * @returnthe pseudorandom value
     */
    private double changeRandomPos() {
    	return (r.nextDouble()/10 + 0.9);
    }
    
    /**
     * Returns a new pseudorandom double between -1 and 1
     * @return the pseudorandom value
     */
    private double changeRandom() {
    	double rand = changeRandomPos();
    	if (r.nextDouble() < 0.5)
    		rand = -rand;
    	return rand;
    }

    /**
     * Moves a label left or right and "bounces" it on the bottom of the window, then makes the label invisible when offscreen 
     * @param l the label to move
     */
    public void runWinAnimation(Label l) {
    	
    	//double speedFactor = 10;
    	//on each frame, move slightly
    	double dx = randX * thisLabelFrames;
    	//TODO: Factor down or make x component also accelerate
    	double dy = randY * thisLabelFrames * thisLabelFrames;
    	l.setTranslateX(dx);
    	l.setTranslateY(dy);
    	System.out.println(randX + " " + randY);
    	//when left edge off right side of stage etc, remove card
    	if (l.getLayoutX() + l.getTranslateX() < -l.getWidth() || l.getLayoutX() + l.getTranslateX() > ((AnchorPane) l.getParent()).getWidth())
    		l.setVisible(false);
    	
    	/*for (int i = SolConst.CARDSINSUIT; i > 0; i--) {
            for (Entry<SolConst.SType, List<Label>> e: finalLabels.entrySet()) {
                Label cardLabel = e.getValue().get(i);
                int dx = (int) (0.0000005*(now - prev));
                System.out.println(dx + " " + now + " prev: " + prev);
                cardLabel.setTranslateX(cardLabel.getTranslateX() + dx);
            }
        }*/
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
				runWinAnimation(l);
				thisLabelFrames++;
			}
		}
	}
	
	private void switchStack(long now) {
		thisLabelFrames = 0;
		randX = changeRandom();
		randY = changeRandomPos();
		if (cardValue > 0)
			handle(now);
	}
}
