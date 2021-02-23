package solitaire.fxui;

import solitaire.model.CardStacks;
import solitaire.model.CardDeck;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;


public class SolitaireController {
	
	private CardStacks stacks;
	private CardDeck deck;
	@FXML private StackPane PlayStacks;
	
	//@FXML private GridPane PlayStacks;
	//private List<solitaire.model.Card> playStacks;
	
	@FXML
	private void initialize() {
		//playStacks = new ArrayList<>();
		
		deck = new CardDeck(CardStacks.CARDSINSUITE);
		for (int i = 0; i < 3; i++)
			deck.shufflePerfectly();
		stacks = new CardStacks(deck);
		//System.out.println(stacks.getPlayStacks()[6]);
		System.out.println(stacks.getPlayStacks()[6].get(6) + " " +  stacks.getPlayStacks()[6] + " " + stacks.getPlayStacks()[5]);
		stacks.moveCard(stacks.getPlayStacks()[6].get(6), "p6", "p5");
		System.out.println(stacks.getPlayStacks()[6] + " " + stacks.getPlayStacks()[5]);

		for (int i = 0; i < CardStacks.PLAYSTACKSNUM; i++) {
			//Label currentLabel = (Label) "PlayStack" + i;
			
			Label b = new Label(stacks.getPlayStacks()[i].get(stacks.getPlayStacks()[i].size() - 1).toString());
			b.setTranslateX(i*30);
			PlayStacks.getChildren().add(b);
					// 
		}
		
		//PlayStack0.setText(stacks.getPlayStacks()[0].get(0).toString());;
	}
}
