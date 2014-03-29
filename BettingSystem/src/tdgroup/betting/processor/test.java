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
		//handler.onReceiveSbobetOddDisplayUpdate(readFile("today-data_test2.txt"));
		//handler.onReceiveSbobetOddDisplayUpdate(BetUpdateEventHandlerImpl.data);
		handler.onReceiveSbobetTicketUpdate(readFile("ticket-data_test3.txt"));
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
