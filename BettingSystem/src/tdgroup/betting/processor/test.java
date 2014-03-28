package tdgroup.betting.processor;

public class test {

	/**
	 * @param args
	 * @throws UnsupportedResponseFormatException 
	 */
	public static void main(String[] args) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		BetUpdateEventHandler handler = new BetUpdateEventHandlerImpl();
		handler.onReceiveSbobetOddDisplayUpdate(BetUpdateEventHandlerImpl.data);
	}

}
