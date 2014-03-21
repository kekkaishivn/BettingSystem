package tdgroup.betting.crawler;

import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JMemoryPacket;

public abstract class PcapOfflinePacketCapturer extends PcapPackageCapturer {
	String filename;

	public JMemoryPacket outPacket;

	public PcapOfflinePacketCapturer(String filename) {
		this.filename = filename;
	}

	public Pcap openOfflineByFile() {
		StringBuilder errorBuffer = new StringBuilder();
		return Pcap.openOffline(this.filename, errorBuffer);
	}

	public void establishOfflineConnection() {
		this.pCapturer = openOfflineByFile();
	}

	@Override
	public void run() {
		if (pCapturer == null) {
			return;
		}
		connect();
	}

}
