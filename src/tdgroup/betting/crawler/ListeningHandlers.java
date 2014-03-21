package tdgroup.betting.crawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;

import org.jnetpcap.ByteBufferHandler;
import org.jnetpcap.JBufferHandler;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.PeeringException;
import org.jnetpcap.protocol.JProtocol;

public class ListeningHandlers {
	public static ByteBufferHandler<ByteArrayOutputStream> simplePackageListeningHandler() {
		ByteBufferHandler<ByteArrayOutputStream> handler = new ByteBufferHandler<ByteArrayOutputStream>() {
			public void nextPacket(PcapHeader header, ByteBuffer buffer,
					ByteArrayOutputStream out) {
				PrintStream ps = new PrintStream(out);
				ps.println(header.toString());
			}
		};
		return handler;
	}

	public static ByteBufferHandler<JMemoryPacket> detailPacketHandler() {
		ByteBufferHandler<JMemoryPacket> handler = new ByteBufferHandler<JMemoryPacket>() {
			public void nextPacket(PcapHeader header, ByteBuffer buffer,
					JMemoryPacket out) {
				try {
					out = new JMemoryPacket(JProtocol.ETHERNET_ID, buffer);
					System.out.println(out);
				} catch (PeeringException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		return handler;
	}

	public static JBufferHandler<PcapDumper> dumpHandler() {
		class JBufferDumpHandler implements JBufferHandler<PcapDumper>{
			int packet_counter = 0;
			public void nextPacket(PcapHeader header, JBuffer buffer,
					PcapDumper dumper) {
				packet_counter += 1;
				System.out.println(packet_counter);
				dumper.dump(header, buffer);
			}
		}
		JBufferHandler<PcapDumper> handler = new JBufferDumpHandler() ;
		return handler;
	}

	public static ByteBufferHandler<JMemoryPacket> detailPacketPrintToFileHandler(
			final Writer writer) {
		ByteBufferHandler<JMemoryPacket> handler = new ByteBufferHandler<JMemoryPacket>() {
			public void nextPacket(PcapHeader header, ByteBuffer buffer,
					JMemoryPacket out) {
				try {
					out = new JMemoryPacket(JProtocol.ETHERNET_ID, buffer);
					writer.write(out.toString());
				} catch (PeeringException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		return handler;
	}

	// public static PcapPacketHandler bufferedAssemblyHandler(){
	// PcapPacketHandler handler = new PcapPacketHandler({
	// private Ip4 ip = new Ip4(); // Ip4 header
	//
	// public void nextPacket(PcapPacket packet, Object user) {
	// final int flags = ip.flags();
	//
	// if ((flags & Ip4.FLAG_MORE_FRAGEMNTS) != 0) {
	// bufferFragment(packet, ip);
	// } else {
	// bufferLastFragment(packet, ip);
	// }
	//
	// timeoutBuffers();
	// }
	// });
	// return handler;
	// }
}
