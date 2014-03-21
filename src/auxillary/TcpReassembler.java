package auxillary;

import org.jnetpcap.Pcap;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory.Type;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;

public class TcpReassembler implements PcapPacketHandler<Object> {
	public interface TcpReassemblyBufferHandler {
		public void nextTcpUdp(TcpReassemblyBuffer buffer);
	}

	public static class TcpReassemblyBuffer extends JBuffer implements
			Comparable<TcpReassemblyBuffer> {

		private Tcp TCPHeader = new Tcp();
		public int tcpUdpLength;
		// Copy one IP4 header and one TCP header
		private int bytesCopiedIntoBuffer = 20;
		private final int tcpHeaderLen = 20; // length Tcp header
		private final long timeout;

		// private final int hash;

		// @Override
		// public int hashCode() {
		// return this.hash;
		// }

		public TcpReassemblyBuffer(Tcp tcpHeader, int size, int tcpUdpLength,
				long timeout) {
			super(size);
			// this.hash = hash;
			this.tcpUdpLength = tcpUdpLength;// in case of Tcp allocation,
												// tcpUdpLength
			// should be known beforehand
			this.timeout = timeout;
			transferFrom(tcpHeader); // copy fragment's Ip header to
										// our buffer
		}

		private void transferFrom(Tcp tcpHeader) {
			tcpHeader.transferTo(this, 0, 20, 20);

			// Set some values for TCP Header
			this.TCPHeader.peer(this, 20, 20);
		}

		public boolean isComplete() {
			return this.tcpUdpLength == this.bytesCopiedIntoBuffer;
		}

		public void addSegment(JBuffer packet, int assemblyOffset, int length,
				int packetOffset) {
			this.bytesCopiedIntoBuffer += length;
			System.out.println(packetOffset);
			System.out.println(length);
			System.out.println(assemblyOffset + tcpHeaderLen);
			packet.transferTo(this, packetOffset, length, assemblyOffset
					+ tcpHeaderLen);
		}

		public int compareTo(TcpReassemblyBuffer o) {
			return (int) (o.timeout - this.timeout);
		}

		public boolean isTimedout() {
			return this.timeout < System.currentTimeMillis(); // Future or past
		}

		public Tcp getTcpHeader() {
			return this.TCPHeader;
		}

	}

	private static final int DEFAULT_REASSEMBLY_SIZE = 8 * 1024; // 8k

	private int currentContentLength;

	public static void main(String[] args) {

		StringBuilder errbuf = new StringBuilder();
		Pcap pcap = Pcap.openOffline("pcapfiles/test-ipreassembly2.pcap",
				errbuf);
		if (pcap == null) {
			System.err.println(errbuf.toString());
			return;
		}

		pcap.loop(6, new TcpReassembler(new TcpReassemblyBufferHandler() {

			public void nextTcpUdp(TcpReassemblyBuffer buffer) {
				if (buffer.isComplete() == false) {
					System.err.println("WARNING: missing fragments");
				} else {
					JPacket packet = new JMemoryPacket(Type.POINTER);
					packet.peer(buffer);
					System.out.println(buffer.size());
					packet.getCaptureHeader().wirelen(buffer.size());
					packet.scan(Ip4.ID); // decode the packet
					// System.out.println(packet.toString());
				}

			}

		}), null);
	}

	TcpReassemblyBuffer currentBuffer = null;

	private TcpReassemblyBufferHandler handler;
	private Ip4 ipHeader = new Ip4();
	private Tcp tcpHeader = new Tcp();
	private Http httpHeader = new Http();

	public TcpReassembler(TcpReassemblyBufferHandler handler) {
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
	}

	private void dispatch(TcpReassemblyBuffer buffer) {
		System.out.println("Dispatch here");
		handler.nextTcpUdp(buffer);
	}

	private TcpReassemblyBuffer getBuffer(PcapPacket packet, Tcp tcp) {
		if (currentBuffer == null) {
			currentBuffer = new TcpReassemblyBuffer(tcp,
					DEFAULT_REASSEMBLY_SIZE, this.currentContentLength,
					tcp.hashCode());
		}

		return currentBuffer;
	}

	long currentInitialOffset;

	private TcpReassemblyBuffer bufferFragment(PcapPacket packet, Ip4 ip,
			Tcp tcp) {
		TcpReassemblyBuffer buffer = getBuffer(packet, tcp);

		// tcp related parameters
		// Incorrect
		final int len = ip.length() - ip.getHeaderLength()
				- tcp.getHeaderLength();

		final int packetOffset = tcp.getOffset() + tcp.getHeaderLength();
		final int assemblyOffset = (int) (tcp.seq() - currentInitialOffset);

		buffer.addSegment(packet, assemblyOffset, len, packetOffset);

		if (buffer.isComplete()) {
			dispatch(buffer);
		}

		return buffer;
	}

	public void detectNewInitialIncomingHttpPacket(PcapPacket packet) {
		if (packet.hasHeader(httpHeader)) {
			// System.out.println("Http length " + http.g);
			if (httpHeader.hasField(Http.Response.Content_Length)) {
				// System.out.println(httpHeader.fieldValue(Http.Response.Content_Length));
				this.currentContentLength = Integer.parseInt(httpHeader
						.fieldValue(Http.Response.Content_Length));
			}
			packet.hasHeader(tcpHeader);
			currentInitialOffset = tcpHeader.seq();
		}
	}

	public void nextPacket(PcapPacket packet, Object user) {
		detectNewInitialIncomingHttpPacket(packet);

		if (packet.hasHeader(tcpHeader) && packet.hasHeader(ipHeader)) {
			bufferFragment(packet, ipHeader, tcpHeader);
		}
	}

}