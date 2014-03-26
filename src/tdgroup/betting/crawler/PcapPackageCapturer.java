package tdgroup.betting.crawler;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.jnetpcap.Pcap;

import tdgroup.betting.crawler.filter.NetworkFilter;

abstract class PcapPackageCapturer implements Connector {
	Pcap pCapturer = null;
	File filterConfigFile = null;
	NetworkFilter networkFilter = null;
	public ByteArrayOutputStream outString = new ByteArrayOutputStream();
	String outputFilename = null;
	

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	private void compileFilter() throws Exception {
		if (this.pCapturer.compile(this.networkFilter.getPacketFilterProgram(),
				this.networkFilter.getExpression(),
				this.networkFilter.getOptimize(),
				this.networkFilter.getNetmask()) != Pcap.OK) {
			throw new Exception("Compiling filter failed.\n"
					+ this.pCapturer.getErr());
		}
	}

	private void setNetworkFilter() throws Exception {
		if (this.pCapturer.setFilter(networkFilter.getPacketFilterProgram()) != Pcap.OK) {
			System.err.println(this.pCapturer.getErr());
			throw new Exception(
					"Compiling filter done.\n Setting filter failed.\n"
							+ this.pCapturer.getErr());
		} 
	}

	private void setFilterFile(File filterConfigFile) {
		this.filterConfigFile = filterConfigFile;
	}

	private void checkNullCapturer() throws Exception {
		if (this.pCapturer == null) {
			throw new Exception(
					"Packet Capturer has not been initated yet.");
		}
	}

	private void checkNullFilterFile() throws Exception {
		if (this.filterConfigFile == null) {
			throw new Exception(
					"Filter file has not been set. Using setFilterFile(File filterConfigFile)");
		}
	}

	private void resetFilter() throws Exception {
		checkNullCapturer();
		checkNullFilterFile();
		networkFilter = NetworkFilter
				.getSingleton(this.filterConfigFile);
		compileFilter();
		setNetworkFilter();
	}
	
	public void setFilterFileAndResetFilter(File filterConfigFile) {
		this.setFilterFile(filterConfigFile);
		try {
			this.resetFilter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
