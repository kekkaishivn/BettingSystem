package tdgroup.betting.crawler;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jnetpcap.Pcap;

import tdgroup.betting.util.Utils;
import auxillary.TcpHttpReassembler;
import auxillary.TcpHttpReassembler.TcpHttpReassemblyBuffer;
import auxillary.TcpHttpReassembler.TcpReassemblyBufferHandler;

public class PcapOfflinePacketCapturerTcpAssembler extends
		PcapOfflinePacketCapturer {

	public PcapOfflinePacketCapturerTcpAssembler(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	public void connect() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(this.outputFilename), "utf-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		class TcpReassemblyBufferHandlerPrintToWriter implements
				TcpReassemblyBufferHandler {
			BufferedWriter writer;

			public TcpReassemblyBufferHandlerPrintToWriter(BufferedWriter writer) {
				this.writer = writer;
			}

			@Override
			public void nextTcpUdp(TcpHttpReassemblyBuffer buffer) {
				if (buffer.isComplete() == false) {
					System.err.println("WARNING: missing fragments");
				} else {
					try {
						System.out.println(buffer.getUTF8String(0, 3000));
						writer.write(buffer.getUTF8String(0, 1000));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		pCapturer.loop(Pcap.LOOP_INFINITE, new TcpHttpReassembler(
				new TcpReassemblyBufferHandlerPrintToWriter(writer),
				Utils.ENUMLABEL.DEBUG_INFO), null);
		pCapturer.close();
	}

}
