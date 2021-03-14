package solitaire.fxui;

import solitaire.model.GameBoard;
import solitaire.model.SolConst;
import solitaire.model.CardContainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;


public interface IOController {
    static public boolean writeToFile(GameBoard board) {
        if (Files.notExists(SolConst.SAVEDIR)) {
            try {
                Files.createDirectories(SolConst.SAVEDIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Files.notExists(SolConst.SAVEFILE)) {
            try {
                System.out.println(SolConst.SAVEFILE + " " + board.toString());
                Files.createFile(SolConst.SAVEFILE);
            } catch (IOException e ) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Files.exists(SolConst.SAVEFILE)) {
            try {
                PrintWriter writer = new PrintWriter(SolConst.SAVEFILE.toString());
                writer.write(board.toString());
                writer.flush();
                
                writer.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    static public Map <SolConst.SType, CardContainer> fileToMap(String fileName) {
        //Read and parse file

        //TODO: return a map, and use that for constructor GameBoard(map)

        return null;
    }
}
