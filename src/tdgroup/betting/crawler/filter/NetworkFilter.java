package tdgroup.betting.crawler.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.Properties;

import org.jnetpcap.PcapBpfProgram;

public class NetworkFilter {
	PcapBpfProgram packetFilterProgram;
	String expression;
	int optimize;
	int netmask;
	String source;
	static String HOST_KEY = "host";
	static String OPTIMIZE = "optimize";
	static String NETMASK = "netmask";
	static String SOURCE_IP = "sourceip";

	static int USE_OPTIMIZATION = 1;
	static int _8_BITS_MASK = 0xFFFFFF00;
	private static File currentFilterFile;
	static NetworkFilter singleton = null;
	
	private NetworkFilter(String expression, int optimize, int netmask,
			String source) {
		packetFilterProgram = new PcapBpfProgram();
		this.expression = expression;
		this.optimize = optimize;
		this.netmask = netmask;
		this.source = source;
	}
	
	public static NetworkFilter getSingleton(File file){
		if (file.equals(currentFilterFile) && singleton != null){
			return singleton;
		}
		singleton = createNetworkFilter(file);
		return singleton;
	}
	
	public static NetworkFilter getSingleton(){
		return singleton;
	}

	public PcapBpfProgram getPacketFilterProgram() {
		return packetFilterProgram;
	}

	public String getExpression() {
		return expression;
	}

	public int getOptimize() {
		return optimize;
	}

	public int getNetmask() {
		return netmask;
	}


	private static NetworkFilter createNetworkFilter(File file) {
		// create and load default properties
		Properties defaultProps = new Properties();
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			defaultProps.load(inputStream);
			inputStream.close();
			currentFilterFile = file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String expression;
		int optimize;
		int netmask;
		String source;
		if (defaultProps.containsKey(HOST_KEY)) {
			expression = HOST_KEY + " " + defaultProps.getProperty(HOST_KEY);
		} else {
			expression = "";
		}
		if (defaultProps.containsKey(OPTIMIZE)) {
			try {
				optimize = Integer.parseInt(defaultProps.getProperty(OPTIMIZE));
			} catch (NumberFormatException e) {
				optimize = USE_OPTIMIZATION; // employ
			}
		} else {
			optimize = USE_OPTIMIZATION;
		}
		if (defaultProps.containsKey(NETMASK)) {
			try {
				netmask = Integer.parseInt(defaultProps.getProperty(NETMASK));
			} catch (NumberFormatException e) {
				netmask = _8_BITS_MASK; // employ
			}
		} else {
			netmask = _8_BITS_MASK;
		}

		if (defaultProps.containsKey(SOURCE_IP)) {
			source = defaultProps.getProperty(SOURCE_IP);
		} else {
			source = null;
		}
		return new NetworkFilter(expression, optimize, netmask, source);
	}

	public int[] getSource() {
		String[] splittedString = source.split("\\.");
		int[] splittedSources = new int[4];
		for (int i = 0; i < 4; i++) {
			splittedSources[i] = Integer.parseInt(splittedString[i]);
		}
		return splittedSources;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
