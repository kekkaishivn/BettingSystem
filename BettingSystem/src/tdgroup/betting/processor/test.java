package tdgroup.betting.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class test {

	/**
	 * @param args
	 * @throws UnsupportedResponseFormatException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws UnsupportedResponseFormatException, IOException {
		// TODO Auto-generated method stub
		BetUpdateEventHandler handler = new BetUpdateEventHandlerImpl();
		//handler.onReceiveSbobetHandicapOddDisplayUpdate(readFile("today-data_test2.txt"));
		//handler.onReceiveSbobetOddDisplayUpdate(BetUpdateEventHandlerImpl.data);
		//handler.onReceiveHandicapSbobetTicketUpdate(readFile("ticket-data_test3.txt"));
		//handler.onReceiveIbetOddsValueUpdate(readFile("ibet-underover_newearly_test.txt"));
		String data = readFile("today-data_test2.txt");
		
		long before = System.currentTimeMillis();
		handler.onReceiveSbobetHandicapOddsDisplayUpdate(data);
		System.out.println("Processing time:" + (System.currentTimeMillis()-before));
	}
	
	static String readFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    try {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        return sb.toString();
    } finally {
        br.close();
    }
	}

}
