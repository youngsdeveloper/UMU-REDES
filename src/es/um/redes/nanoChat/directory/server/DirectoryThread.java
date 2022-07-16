package es.um.redes.nanoChat.directory.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import es.um.redes.nanoChat.directory.connector.DirectoryConnector;


public class DirectoryThread extends Thread {

	//Tamaño máximo del paquete UDP
	private static final int PACKET_MAX_SIZE = 128;
	//Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del servidor
	protected Map<Integer,InetSocketAddress> servers;

	//Socket de comunicación UDP
	protected DatagramSocket socket = null;
	//Probabilidad de descarte del mensaje
	protected double messageDiscardProbability;

	public DirectoryThread(String name, int directoryPort,
			double corruptionProbability)
			throws SocketException {
		super(name);
		
		
		
		
		
		
		//DONE Anotar la dirección en la que escucha el servidor de Directorio
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);

		
 		//DONE Crear un socket de servidor
		socket = new DatagramSocket(serverAddress);
		
		messageDiscardProbability = corruptionProbability;
		//Inicialización del mapa
		servers = new HashMap<Integer,InetSocketAddress>();
	}

	@Override
	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		while (running) {
			
				

				// DONE 1) Recibir la solicitud por el socket
			
				// Create datagram packet
				DatagramPacket pckt = new DatagramPacket(buf, buf.length);
				// Receive request message
				try {
					socket.receive(pckt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				// DONE 2) Extraer quién es el cliente (su dirección)
				
				InetSocketAddress ca = (InetSocketAddress) pckt.getSocketAddress();
				
				// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte
			
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory DISCARDED corrupt request from... " + ca.getAddress().toString() + ":"+ca.getPort() + " -- OP_CODE PACKET: " + pckt.getData()[0] );
					continue;
				}

				//DONE (Solo Boletín 2) Devolver una respuesta idéntica en contenido a la solicitud

				/*buf = new byte[PACKET_MAX_SIZE];
				
				pckt = new DatagramPacket(buf, buf.length,ca);
				try {
					socket.send(pckt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				//DONE 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
				//DONE 5) Tratar las excepciones que puedan producirse

				try {
					processRequestFromClient(buf, ca);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
		}
		socket.close();
	}

	// Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		
		
		
		//TODO 1) Extraemos el tipo de mensaje recibido
		
		ByteBuffer ret = ByteBuffer.wrap(data); //Toma como entrada men
		int opcode = ret.get(); //Obtiene un campo de 1 byte
		
		//TODO 2) Procesar el caso de que sea un registro y enviar mediante sendOK
		
		
		
		switch(opcode){
			case DirectoryConnector.OPCODE_REGISTER_SERVER:
				System.out.println("RCV(REGISTER_SERVER)");
				
				int protocol = ret.getInt();
				int port = ret.getInt();
				InetSocketAddress server_address = new InetSocketAddress(clientAddr.getAddress(),port); // TODO: ¿Cual es la direccion del servidor?
				servers.put(protocol, server_address); 
				sendOK(clientAddr);
				System.out.println("SND(REGISTER_SERVER_OK)");
				break;
				
			case DirectoryConnector.OPCODE_SELECT_ADDRESS:
				System.out.println("RCV(SELECT_ADDRESS)");
				int protocol_select = ret.getInt();
				InetSocketAddress serverAddress = servers.get(protocol_select); 
				
				if(serverAddress!=null) {
					sendServerInfo(serverAddress, clientAddr);
					System.out.println("SND(SELECT_ADDRESS_OK)");
				}else {
					sendEmpty(clientAddr);
					System.out.println("SND(SELECT_ADDRESS_EMPTY)");
				}

				break;
		}
		
		//TODO 3) Procesar el caso de que sea una consulta
		//TODO 3.1) Devolver una dirección si existe un servidor (sendServerInfo)
		//TODO 3.2) Devolver una notificación si no existe un servidor (sendEmpty)
	}

	//Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		//TODO Construir respuesta
		

		ByteBuffer bb = ByteBuffer.allocate(1); //Crea un buffer de 1 bytes
		bb.put(DirectoryConnector.OPCODE_SELECT_ADDRESS_EMPTY); //Inserta un campo de 1 byte (opcode es byte)
		byte[] data = bb.array();
		DatagramPacket pckt = new DatagramPacket(data, data.length,clientAddr);
		
		//TODO Enviar respuesta
		try {
			socket.send(pckt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	//Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		//Obtener la representación binaria de la dirección
		
		
		
		byte[] serverAddressBytes = serverAddress.getAddress().getAddress();
		int port = serverAddress.getPort();
		
		

	
		
		//Construir respuesta
		ByteBuffer bb = ByteBuffer.allocate(9); //Crea un buffer de 9 bytes
		bb.put(DirectoryConnector.OPCODE_SELECT_ADDRESS_OK); 
		for(Byte b:serverAddressBytes){
			bb.put(b);
		}
		bb.putInt(port);
		
		
		byte[] data = bb.array();
		DatagramPacket pckt = new DatagramPacket(data, data.length,clientAddr);
		
		//Enviar respuesta
		try {
			socket.send(pckt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	//Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr) throws IOException {
		//TODO Construir respuesta

		
		ByteBuffer bb = ByteBuffer.allocate(1); //Crea un buffer de 1 bytes
		bb.put(DirectoryConnector.OPCODE_REGISTER_SERVER_OK); //Inserta un campo de 1 byte (opcode es byte)
		byte[] data = bb.array();
		DatagramPacket pckt = new DatagramPacket(data, data.length,clientAddr);
		
		//TODO Enviar respuesta
		try {
			socket.send(pckt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
