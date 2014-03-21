package tdgroup.betting.crawler;

import java.io.ByteArrayOutputStream;

import org.jnetpcap.ByteBufferHandler;
import org.jnetpcap.PcapIf;

public class PcapInterfaceSimplePackageCapturer extends PcapInterfacePackageCapturer {
	public PcapInterfaceSimplePackageCapturer(PcapIf interfaceCandidate) {
		super(interfaceCandidate);
	}

	@Override
	public void connect() {
		int cnt = 1; // Capture only one package count to see if
						// the connection could be established
		ByteBufferHandler<ByteArrayOutputStream> handler = ListeningHandlers
				.simplePackageListeningHandler();
		pCapturer.loop(cnt, handler, outString);
		pCapturer.close();
	}
}
