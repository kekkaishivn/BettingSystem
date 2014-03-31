package tdgroup.betting.processor;

public interface BetUpdateEventHandler {
	
	public void onReceiveSbobetHandicapOddsDisplayUpdate(String data) throws UnsupportedResponseFormatException;
	public void onReceiveSbobetHandicapTicketUpdate(String data) throws UnsupportedResponseFormatException;
	//public void onReceiveSbobetSportMarketUpdate(String data) throws UnsupportedResponseFormatException;
	public void onReceiveIbetOddsValueUpdate(String data) throws UnsupportedResponseFormatException;
	public void onReceiveIbetBetProcessing(String data) throws UnsupportedResponseFormatException;

}
