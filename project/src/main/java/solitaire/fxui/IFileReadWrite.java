package solitaire.fxui;

import java.io.IOException;

import solitaire.model.GameBoard;

public interface IFileReadWrite {
	/**
	 * Writes a game to file
	 * @param board The GameBoard to write
	 */
	public void writeToFile(GameBoard board);
	
	/**
	 * Loads a GameBoard from the default location.
	 * @param name Name of the file
	 * @return Instance of GameBoard
	 * @throws IOException
	 */
	public GameBoard loadGame(String name) throws IOException;
}
