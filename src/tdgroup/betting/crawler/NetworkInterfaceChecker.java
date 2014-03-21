package tdgroup.betting.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

public class NetworkInterfaceChecker {
	public static List<PcapIf> PcapInterfaces;
	public static PcapIf selectedPcapIf;
	static ArrayList<PcapInterfaceSimplePackageCapturer> interfaceCapturers;
	static ArrayList<Thread> interfaceCaptureThreads;

	public static void selectInterfaceManually(int i) {
		try {
			selectedPcapIf = PcapInterfaces.get(i);
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void initiateConnectorAndConnectorThread(int PcapIfIndex) {
		PcapInterfaceSimplePackageCapturer newConnector = new PcapInterfaceSimplePackageCapturer(
				PcapInterfaces.get(PcapIfIndex));
		interfaceCapturers.add(newConnector);
		interfaceCaptureThreads.add(new Thread(newConnector));
	}

	public static void startConnectorThread(int PcapIfIndex) {
		System.out.println("interfaceConnectorThread " + PcapIfIndex
				+ "PcapIfIndexs started");
		interfaceCaptureThreads.get(PcapIfIndex).start();
	}

	public static void joinConnectorThread(int PcapIfIndex,
			long microTimeToStopListening) {
		try {
			interfaceCaptureThreads.get(PcapIfIndex).join(
					microTimeToStopListening);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void selectPCapInterfaceAndConnector(int PcapIfIndex) {
		if (interfaceCapturers.get(PcapIfIndex).outString.toString().length() != 0) {
			System.out.println("Selected interface is number " + PcapIfIndex);
			selectedPcapIf = PcapInterfaces.get(PcapIfIndex);
		}
	}

	public static void shutdownConnectorAndConnectorThreads(int PcapIfIndex) {
		if (interfaceCaptureThreads.get(PcapIfIndex).isAlive()) {
			System.out.println("is alive " + PcapIfIndex);
			interfaceCapturers.get(PcapIfIndex).interruptPcapLoop();
			interfaceCapturers.get(PcapIfIndex).closePcap();
			interfaceCaptureThreads.get(PcapIfIndex).interrupt();
		}
	}
	
	@Deprecated
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(1, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@Deprecated
	public static void employExecutor() {
		// ========================================================
		// New added code to change to executors
		ExecutorService pool = Executors.newFixedThreadPool(PcapInterfaces
				.size());
		ArrayList<Callable<Object>> interfaceCaptureExecutors = new ArrayList<Callable<Object>>();
		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			// pool.execute(interfaceCapturers.get(PcapIfIndex));
			interfaceCaptureExecutors.add(Executors
					.callable(interfaceCaptureThreads.get(PcapIfIndex)));
		}

		try {
			pool.invokeAll(interfaceCaptureExecutors, 500,
					TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // Timeout

		// shutdownAndAwaitTermination(pool);
		// System.out.println("Finish this step");
		// try {
		// executor.awaitTermination(1, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// ========================================================

	}

	public static void selectInterfaceAutomatically() {

		interfaceCapturers = new ArrayList<PcapInterfaceSimplePackageCapturer>();
		interfaceCaptureThreads = new ArrayList<Thread>();

		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			initiateConnectorAndConnectorThread(PcapIfIndex);
		}

		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			startConnectorThread(PcapIfIndex);
		}

		long microTimeToStopListening = 500;
		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			joinConnectorThread(PcapIfIndex, microTimeToStopListening);
		}
		
		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			selectPCapInterfaceAndConnector(PcapIfIndex);
		}

		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			shutdownConnectorAndConnectorThreads(PcapIfIndex);
			System.out.println("Shut down " + PcapIfIndex);
		}
	}

	public static void checkNetworks() {
		StringBuilder errorBuffer = new StringBuilder();
		PcapInterfaces = new ArrayList<PcapIf>(); // Will hold list of devices
		int statusCode = Pcap.findAllDevs(PcapInterfaces, errorBuffer);
		if (statusCode != Pcap.OK) {
			System.out.println("Error occured: " + errorBuffer.toString());
			return;
		}
		for (int PcapIfIndex = 0; PcapIfIndex < PcapInterfaces.size(); PcapIfIndex++) {
			System.out.println("#" + PcapIfIndex + ": "
					+ PcapInterfaces.get(PcapIfIndex).toString());
		}
	}

	public static void main(String[] args) {
		checkNetworks();
		selectInterfaceAutomatically();
	}
}
