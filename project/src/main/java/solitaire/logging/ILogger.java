package solitaire.logging;

public interface ILogger {
    public String ERROR = "error", WARNING = "warning", INFO = "info", FINE = "fine";
    public void log(String severity, String message, Exception exception);
}