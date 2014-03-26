package tdgroup.betting.crawler;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jnetpcap.Pcap;
import org.jnetpcap.nio.JMemory.Type;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.network.Ip4;

import tdgroup.betting.crawler.assembler.IpReassembler;
import tdgroup.betting.crawler.assembler.IpReassembler.IpReassemblyBuffer;
import tdgroup.betting.crawler.assembler.IpReassembler.IpReassemblyBufferHandler;

public class PcapOfflinePacketCapturerIpAssembler extends
		PcapOfflinePacketCapturer {

	public PcapOfflinePacketCapturerIpAssembler(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connect() {
		// int cnt = 50; // Capture packet count

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(this.outputFilename), "utf-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}

		class IpReassemblyBufferHandlerPrintToWriter implements
				IpReassemblyBufferHandler {
			BufferedWriter writer;

			public IpReassemblyBufferHandlerPrintToWriter(BufferedWriter writer) {
				this.writer = writer;
			}

			public void nextIpDatagram(IpReassemblyBuffer buffer) {
				if (buffer.isComplete() == false) {
					System.err.println("WARNING: missing fragments");
				} else {
					JPacket packet = new JMemoryPacket(Type.POINTER);
					packet.peer(buffer);
					packet.getCaptureHeader().wirelen(buffer.size());
					packet.scan(Ip4.ID); // decode the packet
					try {
						writer.write(packet.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		pCapturer.loop(Pcap.LOOP_INFINITE, new IpReassembler(5 * 1000,
				new IpReassemblyBufferHandlerPrintToWriter(writer)), null);
		pCapturer.close();
	}

}
