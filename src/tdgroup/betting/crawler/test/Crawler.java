package tdgroup.betting.crawler.test;

import java.io.File;

import org.jnetpcap.PcapIf;

import tdgroup.betting.crawler.NetworkInterfaceChecker;
import tdgroup.betting.crawler.PcapInterfaceOnlinePacketCapturer;
import tdgroup.betting.crawler.PcapOfflinePacketCapturer;
import tdgroup.betting.crawler.PcapOfflinePacketCapturerTcpAssembler;

public class Crawler {

	public static void testCaptureAndSaveToFile() {
		PcapIf pCaptureInterface;
		NetworkInterfaceChecker.checkNetworks();
		NetworkInterfaceChecker.selectInterfaceManually(2);
		pCaptureInterface = NetworkInterfaceChecker.selectedPcapIf;

		String testPcapOutputFileRelPath = "pcapfiles\\TestCapture.pcap";
		String testConfigFileRelPath = "filterconfigs\\sbobet.properties";
		PcapInterfaceOnlinePacketCapturer packetCapturer = new PcapInterfaceOnlinePacketCapturer(
				pCaptureInterface);
		packetCapturer.setOutputFilename(testPcapOutputFileRelPath);
		packetCapturer.establishOnlineConnection();

		try {
			packetCapturer.setFilterFileAndResetFilter(new File(
					testConfigFileRelPath));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread pCapturerThread = new Thread(packetCapturer);
		pCapturerThread.start();
	}

	public static void testOpenOfflineAndDecode() {
		String testPcapFileRelPath = "pcapfiles\\TestCapture.cap";
		String testConfigFileRelPath = "filterconfigs\\sbobet.properties";
		String testPcapOutputFileRelPath = "pcapfiles\\TestCapture.pcapout";
		PcapOfflinePacketCapturer packetCapturer = new PcapOfflinePacketCapturerTcpAssembler(
				testPcapFileRelPath);
		packetCapturer.setOutputFilename(testPcapOutputFileRelPath);
		packetCapturer.establishOfflineConnection();

		System.out.println("Done set offline file.");
		try {
			packetCapturer.setFilterFileAndResetFilter(new File(
					testConfigFileRelPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done set filter file.");

		Thread pCapturerThread = new Thread(packetCapturer);
		pCapturerThread.start();
	}

	public static void main(String[] args) {
		testOpenOfflineAndDecode();
		// testCaptureAndSaveToFile();
	}
}
