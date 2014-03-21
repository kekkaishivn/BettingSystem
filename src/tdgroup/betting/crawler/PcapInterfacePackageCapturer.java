package tdgroup.betting.crawler;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapClosedException;
import org.jnetpcap.PcapIf;

public abstract class PcapInterfacePackageCapturer extends PcapPackageCapturer {
	PcapIf pCaptureInterfaceCandidate = null;

	public PcapInterfacePackageCapturer(PcapIf interfaceCandidate) {
		this.pCaptureInterfaceCandidate = interfaceCandidate;
	}

	public Pcap openConnectionByInterface() {
		if (pCaptureInterfaceCandidate == null) {
			return null;
		}
		StringBuilder errorBuffer = new StringBuilder();
		int truncatePacketSize = 2048; // Truncate packet at this size
		int promiscous = Pcap.MODE_PROMISCUOUS;
		int timeout = 10 * 1000; // In milliseconds

		return Pcap.openLive(pCaptureInterfaceCandidate.getName(),
				truncatePacketSize, promiscous, timeout, errorBuffer);
	}

	public void establishOnlineConnection() {
		pCapturer = this.openConnectionByInterface();
	}

	public void interruptPcapLoop() {
		pCapturer.breakloop();
	}

	public void closePcap() {
		try {
			pCapturer.close();
		} catch (PcapClosedException e) {
			// Just ignore if the pcapCapturer couldn't be closed
		}
	}

	@Override
	public void run() {
		if (pCapturer == null) {
			return;
		}
		connect();
	}
}
