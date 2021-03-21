package solitaire.fxui;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;
import solitaire.model.SolConst;

public class WinAnimation {
    private Map<SolConst.SType, List<Label>> finalLabels = new TreeMap<SolConst.SType, List<Label>>();

    WinAnimation (Map<SolConst.SType, List<Label>> finalLabels) {
        if (finalLabels.size() != SolConst.SUITS)
            throw new IllegalArgumentException("There should be four final stacks");
            finalLabels.entrySet().stream().forEach(e -> {
            if (e.getValue().size() != SolConst.CARDSINSUIT + 1) {
                throw new IllegalArgumentException("Each final stack should have " + SolConst.CARDSINSUIT + " cards");
            } 
        });
        this.finalLabels = finalLabels;
    }

    public void runWinAnimation() {
        for (int i = SolConst.CARDSINSUIT; i > 0; i--) {
            for (Entry<SolConst.SType, List<Label>> e: finalLabels.entrySet()) {
                Label cardLabel = e.getValue().get(i);
                int dx = -120*i;
                cardLabel.setTranslateX(cardLabel.getTranslateX() + dx);
            }
        }
    }
}
