package tdgroup.betting.crawler;

import org.jnetpcap.JBufferHandler;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapIf;

public class PcapInterfaceOnlinePacketCapturer extends
		PcapInterfaceSimplePackageCapturer {

	public PcapInterfaceOnlinePacketCapturer(PcapIf interfaceCandidate) {
		super(interfaceCandidate);
	}

	@Override
	public void connect() {
		int cnt = 100; // Capture packet count
		
		PcapDumper dumper = pCapturer.dumpOpen(this.outputFilename); // output file  
		
		JBufferHandler<PcapDumper> handler = ListeningHandlers
				.dumpHandler();
		pCapturer.loop(cnt, handler, dumper);
		pCapturer.close();
		dumper.close();
	}

}
