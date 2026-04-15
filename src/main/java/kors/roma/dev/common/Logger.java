package kors.roma.dev.common;

public interface Logger {
    public void logErr(String message,Exception e);
    public void logInfo(String message);
}
