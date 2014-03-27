package tdgroup.betting.crawler;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jnetpcap.Pcap;

import tdgroup.betting.crawler.assembler.TcpHttpReassembler;
import tdgroup.betting.crawler.assembler.TcpHttpReassembler.TcpHttpReassemblyBuffer;
import tdgroup.betting.crawler.assembler.TcpHttpReassembler.TcpReassemblyBufferHandler;
import tdgroup.betting.util.Utils;

public class PcapOfflinePacketCapturerTcpAssembler extends
		PcapOfflinePacketCapturer {

	public PcapOfflinePacketCapturerTcpAssembler(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}

	static int counter = 0;

	public void connect() {

		class TcpReassemblyBufferHandlerPrintToFile implements
				TcpReassemblyBufferHandler {

			public TcpReassemblyBufferHandlerPrintToFile() {
			}

			@Override
			public void nextTcpUdp(TcpHttpReassemblyBuffer buffer) {
				if (buffer.isComplete() == false) {
					System.err.println("WARNING: missing fragments");
				} else {
					HttpPacket packet = buffer.getHttpPacket();
					System.out.println(packet.getContentString());
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(
										"tempo" + counter + ".txt"), "utf-8"));
						counter += 1;
						writer.write(packet.getHeadString());
						writer.write(packet.getContentString());
						writer.close();
					} catch (UnsupportedEncodingException
							| FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		pCapturer.loop(Pcap.LOOP_INFINITE, new TcpHttpReassembler(
				new TcpReassemblyBufferHandlerPrintToFile(),
				Utils.ENUMLABEL.DEBUG_INFO), null);
		pCapturer.close();
	}

}
