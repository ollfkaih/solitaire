package solitaire.fxui;

import solitaire.model.CardStacks;
import solitaire.model.CardDeck;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


public class SolitaireController {
	
	private CardStacks stacks;
	private CardDeck deck;
	@FXML private AnchorPane Root;
	@FXML private AnchorPane PlayStacks;
	@FXML private AnchorPane FinalStacks;
	@FXML private Label DrawStack;
	@FXML private Label ThrowStack;

	@FXML
	private void initialize() {
			
		deck = new CardDeck(CardStacks.CARDSINSUITE);
		for (int i = 0; i < 3; i++)
			deck.shufflePerfectly();
		stacks = new CardStacks(deck);
		//System.out.println(stacks.getPlayStacks()[6]);
//		System.out.println(stacks.getPlayStacks()[6].get(6) + " " +  stacks.getPlayStacks()[6] + " " + stacks.getPlayStacks()[5]);
//		stacks.moveCard(stacks.getPlayStacks()[6].get(6), "p6", "p5");
//		System.out.println(stacks.getPlayStacks()[6] + " " + stacks.getPlayStacks()[5]);

		Label[] p = new Label[CardStacks.PLAYSTACKSNUM];
		Label[] f = new Label[CardStacks.SUITS];
		Label d, t;
		
		for (int i = 0; i < CardStacks.PLAYSTACKSNUM; i++) {
			p[i] = new Label(stacks.getPlayStacks()[i].get(stacks.getPlayStacks()[i].size() - 1).toString());
			p[i].setTranslateX(100 + 60*i);
			p[i].setTranslateY(50);
			p[i].setOnMouseClicked(handleClickDrawStack());
			PlayStacks.getChildren().add(p[i]);
		}
		
		for (int i = 0; i < CardStacks.SUITS; i++) {
			f[i] = new Label(stacks.getFinalStacks()[i].toString());
			f[i].setTranslateX(100 + 60*i);
			f[i].setTranslateY(50);
			FinalStacks.getChildren().add(f[i]);
		}
		
		DrawStack.setText("Backside of cards");
		
		//t skal være tom fra start
		//t = new Label(stacks.getThrowStack().get(stacks.getThrowStack().size() - 1).toString());
		
		//PlayStack0.setText(stacks.getPlayStacks()[0].get(0).toString());;
	}
	
	@FXML
	void handleClickDrawStack() {
		stacks.deal(3);
		ThrowStack.setText(stacks.getThrowStack().get(stacks.getThrowStack().size() - 1).toString());
	}
}
