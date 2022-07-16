package es.um.redes.nanoChat.directory.connector;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import es.um.redes.nanoChat.directory.server.Directory;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	//Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	//Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	//Valor del TIMEOUT
	private static final int TIMEOUT = 1000;
	//Valor máximo de intentos para recuperar los mensajes
	private static final int MAX_TRIES = 30;
	
	//OPCODE registro servidor
	public static final byte OPCODE_REGISTER_SERVER = (byte)1;
	public static final byte OPCODE_REGISTER_SERVER_OK = (byte)2;

	//OPCODE consultar direccion
	public static final byte OPCODE_SELECT_ADDRESS = (byte)3;
	public static final byte OPCODE_SELECT_ADDRESS_OK = (byte)4;
	public static final byte OPCODE_SELECT_ADDRESS_EMPTY = (byte)5;

	private static final int MYPROTOCOL = 108415633;

	
	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // dirección del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		//TODO A partir de la dirección y del puerto generar la dirección de conexión para el Socket

		
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress),DEFAULT_PORT);
		
		//DONE Crear el socket UDP
		socket = new DatagramSocket();
		
		
		//registerServerForProtocol(MYPROTOCOL, DEFAULT_PORT);
		
			
		
	}

	/**
	 * Envía una solicitud para obtener el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(int protocol) throws IOException {
		boolean recibido = false;
		int intentos = 0;
		
		//DONE Generar el mensaje de consulta llamando a buildQuery()
		
		byte[] messsage = buildQuery(protocol);

		
		//DONE Construir el datagrama con la consulta
		DatagramPacket packet = new DatagramPacket(messsage, messsage.length, directoryAddress);
		
		//DONE Enviar datagrama por el socket
		socket.send(packet);

		//DONE preparar el buffer para la respuesta
		byte[] response = new byte [PACKET_MAX_SIZE];
		DatagramPacket packet_response = new DatagramPacket(response, response.length);
		
		//DONE Establecer el temporizador para el caso en que no haya respuesta
		socket.setSoTimeout(TIMEOUT);
		
		while(!recibido&&intentos<MAX_TRIES) {
			try{
				
			//DONE Recibir la respuesta
			socket.receive(packet_response);
			recibido=true;
			
			}
			catch( java.net.SocketTimeoutException e) {
				socket.send(packet);
				intentos++;
			}
		}

		
		//DONE Procesamos la respuesta para devolver la dirección que hay en ella
		return getAddressFromResponse(packet_response);
	}


	//Método para generar el mensaje de consulta (para obtener el servidor asociado a un protocolo)
	private byte[] buildQuery(int protocol) {
		//DONE Devolvemos el mensaje codificado en binario según el formato acordado
		ByteBuffer bb = ByteBuffer.allocate(5); //Crea un buffer de 5 bytes
		bb.put(OPCODE_SELECT_ADDRESS); //Inserta un campo de 1 byte (opcode es byte)
		bb.putInt(protocol); //Inserta campo de 4 bytes (protocol es int)	
		return bb.array();
	}

	//Método para obtener la dirección de internet a partir del mensaje UDP de respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		//Analizar si la respuesta no contiene dirección (devolver null)
		
		ByteBuffer res = ByteBuffer.wrap(packet.getData()); 

		if(res==null) {
			return null;
		}
		
		byte opcode = res.get();
		if(opcode != OPCODE_SELECT_ADDRESS_OK) {
			return null;
		}
		
				
		byte[] addressBytes = new byte[]{res.get(), res.get(), res.get(), res.get()};
		
		//Si la respuesta no está vacía, devolver la dirección (extraerla del mensaje)
		InetAddress address = InetAddress.getByAddress(addressBytes);
		
		int port = res.getInt();
		
		return new InetSocketAddress(address, port);
	}
	
	/**
	 * Envía una solicitud para registrar el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(int protocol, int port) throws IOException {
		boolean recibido = false;
		int intentos = 0;
		//TODO Construir solicitud de registro (buildRegistration)
		byte[] registration = buildRegistration(protocol, port);
		
		//TODO Enviar solicitud
		// allocate buffer and prepare message to be sent
		
		DatagramPacket packet = new DatagramPacket(registration, registration.length, directoryAddress);
		//Se envía el mensaje
		socket.send(packet);
		
		//TODO Recibe respuesta
		
		byte[] response = new byte [PACKET_MAX_SIZE];
		DatagramPacket packet_response = new DatagramPacket(response, response.length);
		socket.setSoTimeout(TIMEOUT);
		
		
		while(!recibido&&intentos<MAX_TRIES) {
			try{
				
			//TODO Recibir la respuesta
			socket.receive(packet_response);
			recibido=true;
			
			}
			catch( java.net.SocketTimeoutException e) {
				socket.send(packet);
				intentos++;
			}
		}
		
		
		ByteBuffer res = ByteBuffer.wrap(response); //Toma como entrada men
		byte opcode = res.get(); //Obtiene un campo de 1 byte
		System.out.println("OPCODE RECEIVED: " + opcode);
		return opcode==DirectoryConnector.OPCODE_REGISTER_SERVER_OK;
	}


	//Método para construir una solicitud de registro de servidor
	//OJO: No hace falta proporcionar la dirección porque se toma la misma desde la que se envió el mensaje
	private byte[] buildRegistration(int protocol, int port) {
		//TODO Devolvemos el mensaje codificado en binario según el formato acordado
		
		ByteBuffer bb = ByteBuffer.allocate(9); //Crea un buffer de 5 bytes
		bb.put(OPCODE_REGISTER_SERVER); //Inserta un campo de 1 byte (opcode es byte)
		bb.putInt(protocol); //Inserta campo de 4 bytes (parameter es int)	
		bb.putInt(port); //Inserta campo de 4 bytes (parameter es int)	
		return bb.array();
	}

	public void close() {
		socket.close();
	}
	/*
	public static void main(String[] args) {
		System.out.println("Probando");
		try {
			DirectoryConnector directory = new DirectoryConnector("localhost");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
