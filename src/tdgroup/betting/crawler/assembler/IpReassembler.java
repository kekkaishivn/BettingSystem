package auxillary;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

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

public class IpReassembler implements PcapPacketHandler<Object> {
	public interface IpReassemblyBufferHandler {
		public void nextIpDatagram(IpReassemblyBuffer buffer);
	}

	public static class IpReassemblyBuffer extends JBuffer implements
			Comparable<IpReassemblyBuffer> {

		private Ip4 header = new Ip4();
		public int ipDatagramLength = -1;
		private int bytesCopiedIntoBuffer = 20;
		private final int start = 20; // length Ip4 header
		private final long timeout;
		private final int hash;

		@Override
		public int hashCode() {
			return this.hash;
		}

		public IpReassemblyBuffer(Ip4 ip, int size, long timeout, int hash) {
			super(size); // allocate memory

			this.timeout = timeout;
			this.hash = hash;

			transferFrom(ip); // copy fragment's Ip header to our buffer
		}

		private void transferFrom(Ip4 ip) {
			ip.transferTo(this, 0, 20, 0);
			header.peer(this, 0, 20);
			header.hlen(5); // Clear IP optional headers
			header.clearFlags(Ip4.FLAG_MORE_FRAGMENTS); // FRAG flag
			header.offset(0); // Offset is now 0
			header.checksum(0); // Reset header CRC, unless we calculate it
			// again
		}

		public void addLastSegment(JBuffer packet, int offset, int length,
				int packetOffset) {
			addSegment(packet, offset, length, packetOffset);
			this.ipDatagramLength = start + offset + length;
			super.setSize(this.ipDatagramLength);
			header.length(ipDatagramLength); // Set Ip4 total length field
		}

		public void addSegment(JBuffer packet, int offset, int length,
				int packetOffset) {
			System.out.println("Package length " + length);
			this.bytesCopiedIntoBuffer += length;
			packet.transferTo(this, packetOffset, length, offset + start);
		}

		public int compareTo(IpReassemblyBuffer o) {
			return (int) (o.timeout - this.timeout);
		}

		public boolean isComplete() {
			return this.ipDatagramLength == this.bytesCopiedIntoBuffer;
		}

		public boolean isTimedout() {
			return this.timeout < System.currentTimeMillis(); // Future or
			// past
		}

		public Ip4 getIpHeader() {
			return header;
		}

	}

	private static final int DEFAULT_REASSEMBLY_SIZE = 8 * 1024; // 8k

	public static void main(String[] args) {

		StringBuilder errbuf = new StringBuilder();
		Pcap pcap = Pcap.openOffline("pcapfiles/test-ipreassembly2.pcap",
				errbuf);
		if (pcap == null) {
			System.err.println(errbuf.toString());
			return;
		}

		pcap.loop(6, new IpReassembler(5 * 1000,
				new IpReassemblyBufferHandler() {

					public void nextIpDatagram(IpReassemblyBuffer buffer) {
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

	private Map<Integer, IpReassemblyBuffer> buffers = new HashMap<Integer, IpReassemblyBuffer>();

	private IpReassemblyBufferHandler handler;

	private Ip4 ip = new Ip4(); // Ip4 header

	private final long timeout;

	private final Queue<IpReassemblyBuffer> timeoutQueue = new PriorityQueue<IpReassemblyBuffer>();

	public IpReassembler(long timeout, IpReassemblyBufferHandler handler) {
		this.timeout = timeout;
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
	}

	private IpReassemblyBuffer bufferFragment(PcapPacket packet, Ip4 ip) {
		IpReassemblyBuffer buffer = getBuffer(ip);

		final int hlen = ip.hlen() * 4;
		System.out.println("ip.hlen() " + ip.hlen());
		final int len = ip.length() - hlen;
		System.out.println("ip.length() " + ip.length());
		final int packetOffset = ip.getOffset() + hlen;
		final int dgramOffset = ip.offset() * 8;
		buffer.addSegment(packet, dgramOffset, len, packetOffset);

		if (buffer.isComplete()) {
			if (buffers.remove(ip.hashCode()) == null) {
				System.err.println("bufferFragment(): failed to remove buffer");
				System.exit(0);
			}
			timeoutQueue.remove(buffer);

			dispatch(buffer);
		}

		return buffer;
	}

	private IpReassemblyBuffer bufferLastFragment(PcapPacket packet, Ip4 ip) {
		IpReassemblyBuffer buffer = getBuffer(ip);

		final int hlen = ip.hlen() * 4;
		final int len = ip.length() - hlen;
		final int packetOffset = ip.getOffset() + hlen;
		final int dgramOffset = ip.offset() * 8;
		buffer.addLastSegment(packet, dgramOffset, len, packetOffset);

		if (buffer.isComplete()) {
			if (buffers.remove(buffer.hashCode()) == null) {
				System.err
						.println("bufferLastFragment(): failed to remove buffer");
				System.exit(0);
			}
			timeoutQueue.remove(buffer);

			dispatch(buffer);
		}

		return buffer;
	}

	private void dispatch(IpReassemblyBuffer buffer) {
		handler.nextIpDatagram(buffer);
	}

	private IpReassemblyBuffer getBuffer(Ip4 ip) {

		IpReassemblyBuffer buffer = buffers.get(ip.hashCode());
		if (buffer == null) { // First time we're seeing this id

			final long bufTimeout = System.currentTimeMillis() + this.timeout;
			buffer = new IpReassemblyBuffer(ip, DEFAULT_REASSEMBLY_SIZE,
					bufTimeout, ip.hashCode());
			buffers.put(ip.hashCode(), buffer);
		}

		return buffer;
	}

	Tcp tcp = new Tcp();
	Http http = new Http();

	public void nextPacket(PcapPacket packet, Object user) {
		if (packet.hasHeader(tcp)) {
			System.out.println("Hash code " + tcp.hashCode());
			// System.out.println("Tcp ack " + tcp.seq());
		}

		if (packet.hasHeader(http)) {
			System.out.println(http);
			// System.out.println("Http length " + http.g);
//			if (http.hasField(Http.Response.Content_Length)) {
//				System.out.println(http.fieldValue(Http.Response.Content_Length));
//			}
//			for (JField field : http.getFields()) {
//				System.out.println(field);
//			}
		}

		if (packet.hasHeader(ip)) {
			if (ip.flags() != 2) {
				System.out.println("Ip flag " + ip.flags());
				System.out.println("Fragment flag " + Ip4.FLAG_MORE_FRAGMENTS);
				if ((ip.flags() & Ip4.FLAG_MORE_FRAGMENTS) != 0) {
					System.out.println("Expect more fragments");
				}
			}

			if ((ip.flags() & Ip4.FLAG_MORE_FRAGMENTS) != 0) {
				bufferFragment(packet, ip);
			} else {
				bufferLastFragment(packet, ip);
			}

			timeoutBuffers();
		}
	}

	private void timeoutBuffers() {
		while (timeoutQueue.isEmpty() == false) {

			if (timeoutQueue.peek().isTimedout()) {
				dispatch(timeoutQueue.poll());
			} else {
				break;
			}
		}
	}
}