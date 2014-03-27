package tdgroup.betting.crawler.assembler;

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

import tdgroup.betting.crawler.HttpPacket;
import tdgroup.betting.crawler.filter.NetworkFilter;
import tdgroup.betting.util.Utils;

/**
 * 
 * @author Tuan TcpHttpReassembler is a class to select packets from a feeder of
 *         packets (could be file feeder, or a connector to a network
 *         interface). The algorithm to assemble an http file: - Detect a packet
 *         has an http header, should be the first of a list of packet
 *         fragments. - Check the content length in the http header. - Buffer a
 *         space to contain the http header and the content of http file
 * 
 */
public class TcpHttpReassembler implements PcapPacketHandler<Object> {
	public interface TcpReassemblyBufferHandler {
		public void nextTcpUdp(TcpHttpReassemblyBuffer buffer);
	}

	public static class TcpHttpReassemblyBuffer extends JBuffer implements
			Comparable<TcpHttpReassemblyBuffer> {

		private Http httpHeader;
		public int PDULength;
		// Copy one IP4 header and one TCP header
		private int bytesCopiedIntoBuffer;
		private long timeout;
		private Utils.ENUMLABEL debugLevel;
		private int wholePacketLength;
		private HttpPacket httpPacket;
		private HttpPacket.EncodingType encoding;

		// private final int hash;

		// @Override
		// public int hashCode() {
		// return this.hash;
		// }

		public TcpHttpReassemblyBuffer(Http httpHeader, int size,
				int PDULength, HttpPacket.EncodingType encoding,
				Utils.ENUMLABEL debugLevel) {
			super(size);
			this.httpHeader = httpHeader;
			this.debugLevel = debugLevel;
			// this.hash = hash;
			this.PDULength = PDULength;

			this.encoding = encoding;
			// transferFrom(httpHeader); // copy fragment's Http header to our
			// buffer
			this.bytesCopiedIntoBuffer = 0; // httpHeader.getLength();
			this.wholePacketLength = this.PDULength + httpHeader.getLength();
		}

		private void transferFrom(Http httpHeader) {
			int headerLength = httpHeader.getLength();
			httpHeader.transferTo(this, 0, headerLength, 0);

			this.httpHeader.peer(this, headerLength, headerLength);
		}

		public boolean isComplete() {
			if (this.debugLevel == Utils.ENUMLABEL.DEBUG_INFO) {
				System.out.println("===========================");
				System.out.println("TcpReaassembler - isComplete");
				System.out.println("wholePacketLength "
						+ this.wholePacketLength);
				System.out.println("bytesCopiedIntoBuffer "
						+ this.bytesCopiedIntoBuffer);
				if (this.wholePacketLength == this.bytesCopiedIntoBuffer) {
					System.out.println("A PACKAGE IS ASSEMBLED");
				}
			}
			return this.wholePacketLength == this.bytesCopiedIntoBuffer;
		}

		public void addSegment(JBuffer packet, int assemblyOffset, int length,
				int packetOffset) {
			this.bytesCopiedIntoBuffer += length;
			if (this.debugLevel == Utils.ENUMLABEL.DEBUG_INFO) {
				System.out.println("===========================");
				System.out
						.println("TcpReaassembler - addSegment - transfer to buffer");
				System.out.println("packetOffset " + packetOffset);
				System.out.println("length " + length);
				System.out.println("assemblyOffset " + (assemblyOffset));
				System.out.println("Sample string "
						+ packet.getUTF8String(packetOffset, 10));
			}

			packet.transferTo(this, packetOffset, length, assemblyOffset);
		}

		public int compareTo(TcpHttpReassemblyBuffer o) {
			return (int) (o.timeout - this.timeout);
		}

		public boolean isTimedout() {
			return this.timeout < System.currentTimeMillis(); // Future or past
		}

		public Http getHttpHeader() {
			return this.httpHeader;
		}

		public void buildHttpPacket() {
			int bufferLength = this.wholePacketLength
					- this.httpHeader.getLength();
			JBuffer contentBuffer = new JBuffer(bufferLength);
			this.transferTo(contentBuffer, this.httpHeader.getLength(),
					bufferLength, 0);
			this.httpPacket = new HttpPacket(this.httpHeader, contentBuffer,
					bufferLength, this.encoding);
		}

		public HttpPacket getHttpPacket() {
			return this.httpPacket;
		}

	}

	private static final int DEFAULT_REASSEMBLY_SIZE = 8 * 1024; // 8k

	private int currentContentLength;
	private HttpPacket.EncodingType currentContentEncoding;

	public static void main(String[] args) {

		StringBuilder errbuf = new StringBuilder();
		Pcap pcap = Pcap.openOffline("pcapfiles/test-ipreassembly2.pcap",
				errbuf);
		if (pcap == null) {
			System.err.println(errbuf.toString());
			return;
		}

		pcap.loop(6, new TcpHttpReassembler(new TcpReassemblyBufferHandler() {

			public void nextTcpUdp(TcpHttpReassemblyBuffer buffer) {
				if (buffer.isComplete() == false) {
					System.err.println("WARNING: missing fragments");
				} else {
					JPacket packet = new JMemoryPacket(Type.POINTER);
					packet.peer(buffer);
					// System.out.println(packet.toString());
				}

			}

		}, Utils.ENUMLABEL.DEBUG_INFO), null);
	}

	TcpHttpReassemblyBuffer currentBuffer = null;

	private TcpReassemblyBufferHandler handler;
	private Ip4 ipHeader = new Ip4();
	private Tcp tcpHeader = new Tcp();
	private Http httpHeader = new Http();
	private Utils.ENUMLABEL debugLevel;

	public TcpHttpReassembler(TcpReassemblyBufferHandler handler,
			Utils.ENUMLABEL debugLevel) {
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
		this.debugLevel = debugLevel;
	}

	private void dispatch(TcpHttpReassemblyBuffer buffer) {
		buffer.buildHttpPacket();
		handler.nextTcpUdp(buffer);
	}

	private TcpHttpReassemblyBuffer getBuffer(PcapPacket packet, Http http) {
		if (http == null)
			return currentBuffer;
		currentBuffer = new TcpHttpReassemblyBuffer(http,
				DEFAULT_REASSEMBLY_SIZE, this.currentContentLength,
				this.currentContentEncoding, this.debugLevel);

		return currentBuffer;
	}

	long currentInitialOffset;

	private TcpHttpReassemblyBuffer bufferFragment(PcapPacket packet, Ip4 ip,
			Tcp tcp) {

		// tcp related parameters

		int len = ip.length() - ip.getHeaderLength() - tcp.getHeaderLength();
		int packetOffset = tcp.getOffset() + tcp.getHeaderLength();

		TcpHttpReassemblyBuffer buffer;
		if (packet.hasHeader(httpHeader)) {
			// If it is the first packet in a UDP
			// The packet will have a http header that contains a certain amount
			// of data
			// Therefore packet offset and len need to be updated
			// len -= httpHeader.getLength();
			// packetOffset += httpHeader.getLength();
			buffer = getBuffer(packet, httpHeader);
		} else {
			buffer = getBuffer(packet, null);
		}

		if (this.debugLevel == Utils.ENUMLABEL.DEBUG_INFO) {
			System.out.println("===========================");
			System.out.println("TcpReassembler - bufferFragment");
			System.out.println("ip.length() " + ip.length());
			System.out.println("tcp.seq() " + tcp.seq());
			System.out.println("currentInitialOffset " + currentInitialOffset);
		}
		int assemblyOffset = (int) (tcp.seq() - currentInitialOffset);
		// + buffer.httpHeader.getLength());

		System.out.println("buffer.httpHeader.getLength() "
				+ buffer.httpHeader.getLength());
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

				String Content_Encoding_Field = httpHeader
						.fieldValue(Http.Response.Content_Encoding);
				if (Content_Encoding_Field == null) {
					this.currentContentEncoding = HttpPacket.EncodingType.PLAIN;
				} else if (Content_Encoding_Field.equals("gzip")) {
					this.currentContentEncoding = HttpPacket.EncodingType.GZIP;
				}

				if (this.debugLevel == Utils.ENUMLABEL.DEBUG_INFO) {
					System.out.println("==========================");
					System.out
							.println("TcpReassembler - detectNewInitialIncomingHttpPacket");
					System.out.println("currentContentLength "
							+ this.currentContentLength);
					System.out.println("currentContentEncoding "
							+ this.currentContentEncoding);
					System.out.println("httpHeader length "
							+ httpHeader.getLength());
				}
			}
			packet.hasHeader(tcpHeader);
			currentInitialOffset = tcpHeader.seq();
		}
	}

	public boolean isSource(int[] ipSource) {
		NetworkFilter filter = NetworkFilter.getSingleton();
		int[] definedIpSource = filter.getSource();
		for (int i = 0; i < 4; i++) {
			if (definedIpSource[i] != ipSource[i])
				return false;
		}
		return true;
	}

	public void nextPacket(PcapPacket packet, Object user) {
		detectNewInitialIncomingHttpPacket(packet);

		if (packet.hasHeader(tcpHeader) && packet.hasHeader(ipHeader)) {
			int[] ipSource = new int[4];

			for (int i = 0; i < 4; i++) {
				ipSource[i] = (ipHeader.source()[i] + 256) % 256;
			}

			if (isSource(ipSource)) {
				bufferFragment(packet, ipHeader, tcpHeader);
			}

		}
	}

}