package tdgroup.betting.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.protocol.tcpip.Http;

public class HttpPacket {
	public enum EncodingType {
		PLAIN, GZIP
	}

	private EncodingType contentEncoding;
	private Http httpHeader;
	private int bufferLength;
	private JBuffer contentBuffer;
	private String contentString;

	public EncodingType getContentEncoding() {
		return contentEncoding;
	}

	public Http getHttpHeader() {
		return httpHeader;
	}

	public int getBufferLength() {
		return bufferLength;
	}

	public JBuffer getContentBuffer() {
		return contentBuffer;
	}

	public HttpPacket(Http httpHeader, JBuffer contentBuffer, int bufferLength,
			EncodingType contentEncoding) {
		this.httpHeader = httpHeader;
		this.contentBuffer = contentBuffer;
		this.bufferLength = bufferLength;
		this.contentEncoding = contentEncoding;
		this.decode();
	}

	public String getHeadString() {
		return this.httpHeader.getUTF8String(0, this.httpHeader.getLength());
	}

	private String decode() {
		if (this.contentEncoding == EncodingType.PLAIN) {
			this.contentString = this.contentBuffer.getUTF8String(0,
					this.bufferLength);
		}
		if (this.contentEncoding == EncodingType.GZIP) {
			try {
				GZIPInputStream gzipInputStreamer = new GZIPInputStream(
						new ByteArrayInputStream(
								this.contentBuffer.getByteArray(0,
										this.bufferLength)));
				InputStreamReader inputStreamReader = new InputStreamReader(
						gzipInputStreamer);
				BufferedReader bufferReader = new BufferedReader(
						inputStreamReader);

				StringBuilder sb = new StringBuilder();
				String readedLine;
				while ((readedLine = bufferReader.readLine()) != null) {
					sb.append(readedLine);
				}
				this.contentString = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public String getContentString() {
		return this.contentString;
	}
}
