package solitaire.fxui;

import java.io.FileNotFoundException;
import java.io.IOException;

import solitaire.model.GameBoard;

public interface IFileReadWrite {
	/**
	 * Writes a game to file
	 * @param board The GameBoard to write
	 * @throws FileNotFoundException
	 */
	void writeToFile(String filename, GameBoard board) throws FileNotFoundException;
	
	/**
	 * Loads a GameBoard from the default location.
	 * @param name Name of the file
	 * @return Instance of GameBoard
	 * @throws IOException
	 */
	GameBoard loadGame(String filename) throws IOException;
}
