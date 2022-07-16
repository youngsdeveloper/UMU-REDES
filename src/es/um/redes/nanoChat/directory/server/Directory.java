package es.um.redes.nanoChat.directory.server;

import java.net.SocketException;

public class Directory {
	public static final int DIRECTORY_PORT = 6868;
	public static final double DEFAULT_CORRUPTION_PROBABILITY = 0.0;



	public static void main(String[] args) {
		double datagramCorruptionProbability = DEFAULT_CORRUPTION_PROBABILITY;

		/**
		 * Command line argument to directory is optional, if not specified,
		 * default value is used:  -loss: probability of corruption of
		 * received datagrams
		 */
		String arg;

		//Analizamos si hay parámetro
		
		if (args.length > 0 && args[0].startsWith("-")) {
			arg = args[0];
			//Examinamos si es un parámetro válido
			if (arg.equals("-loss")) {
				if (args.length == 2) {
					try {
						//El segundo argumento contiene la probabilidad de descarte
						datagramCorruptionProbability = Double.parseDouble(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("Wrong value passed to option " + arg);
						return;
					}
				} else
					System.err.println("option " + arg + " requires a value");
			} else {
				System.err.println("Illegal option " + arg);
			}
		}
		
		System.out.println("Probability of corruption for received datagrams: "
				+ datagramCorruptionProbability);
		DirectoryThread dt;
		try {
			dt = new DirectoryThread("Directory", DIRECTORY_PORT, datagramCorruptionProbability);
			dt.start();
		} catch (SocketException e) {
			System.err.println("Directory cannot create UDP socket on port " + DIRECTORY_PORT);
			System.err.println("Most likely a Directory process is already running and listening on that port...");
			System.exit(-1);
		}
	}
}
