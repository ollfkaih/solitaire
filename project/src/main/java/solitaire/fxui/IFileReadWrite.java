package solitaire.fxui;

import java.io.FileNotFoundException;
import java.io.IOException;

import solitaire.model.GameBoard;

public interface IFileReadWrite {
	/**
	 * Writes a text string to file 
	 * @param filename The name of the file to save to
	 * @param textToWrite The string to write to file 
	 * @throws FileNotFoundException
	 */
	void writeToFile(String filename, String textToWrite) throws FileNotFoundException;
	
	/**
	 * Loads a GameBoard from the default location.
	 * @param filename Name of the file
	 * @return Instance of GameBoard
	 * @throws IOException
	 */
	GameBoard loadGame(String filename) throws IOException;
}
