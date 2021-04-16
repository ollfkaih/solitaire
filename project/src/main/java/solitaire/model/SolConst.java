package solitaire.model;

//Solitaire Constants
public abstract class SolConst {

	public enum SType {DECK, P0, P1, P2, P3, P4, P5, P6, F0, F1, F2, F3, THROWSTACK}
	public static final int CARDSINSUIT = 13;
	public static final int PLAYSTACKSNUM = 7; // Seven play stacks
	public static final int SUITS = 4;
	public static final String IMGDIR = "img/";
	public static final int CARDSTODEAL = 3;
	public static final int TOPDELTAY = 40;
	public static final int BOTTOMDELTAY = 30;
}
