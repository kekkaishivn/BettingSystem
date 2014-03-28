package tdgroup.betting.processor;

public interface BetUpdateEventHandler {
	
	public void onReceiveSbobetOddDisplayUpdate(String data) throws UnsupportedResponseFormatException;
	public void onReceiveSbobetTicketUpdate(String data) throws UnsupportedResponseFormatException;
	public void onReceiveSbobetSportMarketUpdate(String data) throws UnsupportedResponseFormatException;

}
