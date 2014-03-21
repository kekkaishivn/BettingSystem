package tdgroup.betting.crawler;

public interface Connector extends Runnable {
	public void run();
	public void connect();
}
