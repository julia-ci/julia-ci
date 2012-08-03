package JuliaLang;

/**
 * @author Keno Fischer
 */
public interface JBoxLogger {
  public void logInfo(String message);
  public void logWarning(String message);
  public void logError(String message);
  public void logFatalError(String message);
}
