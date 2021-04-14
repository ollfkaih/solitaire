package solitaire.logging;

public interface ILogger {
    String ERROR = "error", WARNING = "warning", INFO = "info", FINE = "fine";
    void log(String severity, String message, Exception exception);
}
