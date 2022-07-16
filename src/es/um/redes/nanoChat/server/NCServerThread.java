package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCPrivateChatMessage;
import es.um.redes.nanoChat.messageFV.NCRoomDescriptionMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCChatMessage;
import es.um.redes.nanoChat.messageFV.NCControlMessage;
import es.um.redes.nanoChat.messageFV.NCFileDataMessage;
import es.um.redes.nanoChat.messageFV.NCFileRequestMessage;
import es.um.redes.nanoChat.messageFV.NCRoomsMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;
import es.um.redes.nanoChat.server.roomManager.NCRoomManagerImpl;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread  extends Thread {
	
	private Socket socket = null;
	
	//Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Usuario actual al que atiende este Thread
	String user;
	//RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	//Sala actual
	String currentRoom;
	
	
	private boolean shouldExit = false;
	
		
	//Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	//Main loop
	public void run() {
		try {
			//Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			//Mientras que la conexión esté activa entonces...
			while (!shouldExit) {
				

				//TODO Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				
				
				switch (message.getOpcode()) {
					
				
				case NCMessage.OP_NICK:

				break;
				
				case NCMessage.OP_QUERY_ROOMS:
					System.out.println("RCV(QUERY_ROOMS)");
					sendRoomList();
					System.out.println("SND(QUERY_ROOMS_RESPONSE)");
					break;
					
					
				case NCMessage.OP_ENTER_ROOM:
					System.out.println("RCV(ENTER_ROOM)");

					
					NCRoomMessage ncRoomMessage = (NCRoomMessage)message;
					String room = ncRoomMessage.getName();

					roomManager = serverManager.enterRoom(user, room, socket);
					if(roomManager==null){
						sendEnterRoomReject();
						System.out.println("SND(ENTER_ROOM_REJECTED)");
					}else{
						roomManager.registerUser(user, socket);
						
						
						//Actualizamos la sala actual
						currentRoom = room;
						sendInRoom();
						System.out.println("SND(IN_ROOM)");
						processRoomMessages();
					}
					
					break;
					
				case NCMessage.OP_CREATE_ROOM:
					System.out.println("RCV(CREATE_ROOM)");
				
					createRoom(message);
					
					
					break;
					
					
										
				case NCMessage.OP_QUIT_MESSAGE:
					System.out.println("Client disconnected from " + socket.getInetAddress().toString() + ":" + socket.getPort());
					socket.close();
					shouldExit = true;
					break;
					
				
				//DONE 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
				//DONE 2) Si se nos pide entrar en la sala entonces obtenemos el RoomManager de la sala,
				//DONE 2) notificamos al usuario que ha sido aceptado y procesamos mensajes con processRoomMessages()
				//DONE 2) Si el usuario no es aceptado en la sala entonces se le notifica al cliente
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected.");
			if(user!=null) {
				serverManager.leaveRoom(user, currentRoom);
				serverManager.removeUser(user);
			}
			
			if (!socket.isClosed())
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			shouldExit = true;
		}
	}

	//Obtenemos el nick y solicitamos al ServerManager que verifique si está duplicado
	private void receiveAndVerifyNickname() {
		//La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
		//TODO Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado

		
		boolean nickOK = false;
		
		
		try {
			while(!nickOK){
				NCMessage message;
			
				message = NCMessage.readMessageFromSocket(dis);
				if(message.getOpcode()==NCMessage.OP_QUIT_MESSAGE) {
					System.out.println("Client disconnected from " + socket.getInetAddress().toString() + ":" + socket.getPort());
					return;
				}
				
				if(message instanceof NCRoomMessage) {
					NCRoomMessage ncRoomMessage = (NCRoomMessage)message;
					String nick = ncRoomMessage.getName();
					System.out.println("RCV(REGISTER_NICK)");
					if(serverManager.addUser(nick)){
						// Salimos del bucle
						nickOK=true;
						user = nick;
					}else{
						// Contestar al cliente con resultado duplicado
						sendNickDuplicated();
						System.out.println("SND(DUPLICATED_NICK)");
					}
				}
				
				
			} 
		}catch (Exception e) {
					
			
		}
		
		if(nickOK) {
			sendNickOk();
			System.out.println("SND(REGISTERED_NICK)");
		}

		

		//DONE Extraer el nick del mensaje
		//DONE Validar el nick utilizando el ServerManager - addUser()
		//DONE Contestar al cliente con el resultado (éxito o duplicado)
	}


	private void sendNickOk(){
		
		// System.out.println("SEND(nick)");
		
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_REGISTERED_NICK);
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendNickDuplicated(){

		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_DUPLICATED_NICK);
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList()  {
		//TODO La lista de salas debe obtenerse a partir del RoomManager y después enviarse mediante su mensaje correspondiente
	
		
		
		List<NCRoomDescription> rooms = serverManager.getRoomList();
		

		
		NCRoomsMessage roomsMessage = new NCRoomsMessage(NCMessage.OP_QUERY_ROOMS_RESPONSE, rooms);
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = roomsMessage.toEncodedString();
		
		
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createRoom(NCMessage message){

				
		NCRoomMessage ncRoomMessage = (NCRoomMessage)message;
		String room = ncRoomMessage.getName();


		if(serverManager.registerRoomManager(new NCRoomManagerImpl(), room)){
			sendCreateRoomOK();
			System.out.println("SND(CREATE_ROOM_OK)");
		}else{
			sendCreateRoomRejected();
			System.out.println("SND(CREATE_ROOM_REJECTED)");
		}
		
	}
	
	
	private void sendInRoom(){

		//Funcionamiento resumido: SEND(IN_ROOM)
		
		
		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_IN_ROOM);
		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendCreateRoomOK(){

		//Funcionamiento resumido: SEND(IN_ROOM)
		

		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_CREATE_ROOM_OK);
		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		
		
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendCreateRoomRejected(){

		//Funcionamiento resumido: SEND(IN_ROOM)
		
		//System.out.println("E1");

		
		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_CREATE_ROOM_REJECTED);
		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//System.out.println("Message: " + rawMessage);

		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void sendEnterRoomReject(){

		
				
		//Funcionamiento resumido: SEND(ENTER_ROOM_REJECTED)
		
		
		NCControlMessage message = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_ENTER_ROOM_REJECTED);
		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		
		
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendRoomInfo(String room) {
		//Creamos un nuevo mensaje tipo RoomDescription
		NCRoomDescriptionMessage roomInfo = new NCRoomDescriptionMessage(NCMessage.OP_INFO_ROOM_MESSAGE_RESPONSE, serverManager.getRoomDesc(room));
		String rawMessage = roomInfo.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void processRoomMessages()  {
		//TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de la sala
		boolean exit = false;
		while (!exit) {
			
			
			
			NCMessage message;
			try {
				message = NCMessage.readMessageFromSocket(dis);
				
				switch(message.getOpcode()){
					case NCMessage.OP_SEND_MESSAGE:
						System.out.println("RCV(SEND_MESSAGE)");
						
						NCChatMessage chatMessage = (NCChatMessage)message;
						roomManager.broadcastMessage(chatMessage.getUser(), chatMessage.getMessage());
						
						break;
					
					case NCMessage.OP_INFO_ROOM_MESSAGE:
						System.out.println("RCV(INFO_ROOM_MESSAGE)");
						
						sendRoomInfo(currentRoom);
						System.out.println("SND(INFO_ROOM_MESSAGE_RESPONSE)");
						
						break;
						
					case NCMessage.OP_EXIT_ROOM_MESSAGE:
						System.out.println("RCV(EXIT_ROOM_MESSAGE)");
						
						serverManager.leaveRoom(user, currentRoom);
						currentRoom = null;
						exit = true;
						break;
						

					case NCMessage.OP_SEND_PRIVATE_MESSAGE:
						System.out.println("RCV(SEND_PRIVATE_MESSAGE)");
					
						NCPrivateChatMessage chatPrivateMessage = (NCPrivateChatMessage)message;
						
						

						boolean userExists = roomManager.broadcastPrivateMessage(chatPrivateMessage.getUser(), chatPrivateMessage.getUser_receiver(), chatPrivateMessage.getMessage());

						if(userExists){
							System.out.println("SND(SEND_PRIVATE_MESSAGE_OK)");

							sendMessagePrivateOk(chatPrivateMessage.getUser(), chatPrivateMessage.getUser_receiver(), chatPrivateMessage.getMessage());
						}else {
							System.out.println("SND(SEND_PRIVATE_MESSAGE_REJECTED)");
							sendMessagePrivateRejected(chatPrivateMessage.getUser(), chatPrivateMessage.getUser_receiver(), chatPrivateMessage.getMessage());
						}
						break;
						
					case NCMessage.OP_SEND_FILE_REQUEST:
						System.out.println("RCV(SEND_FILE_REQUEST)");
					
						NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)message;
											

						boolean userExists_fr = roomManager.broadcastFileRequest(ncFileRequestMessage.getUser(), ncFileRequestMessage.getUser_receiver(), ncFileRequestMessage.getFilename(), ncFileRequestMessage.getFilesize(), ncFileRequestMessage.getFiletransfer());

						if(userExists_fr){
							System.out.println("SND(SEND_FILE_REQUEST_OK)");

							sendFileRequestOk(ncFileRequestMessage.getUser(), ncFileRequestMessage.getUser_receiver(), ncFileRequestMessage.getFilename(), ncFileRequestMessage.getFilesize(), ncFileRequestMessage.getFiletransfer());
						}else {
							System.out.println("SND(SEND_FILE_REQUEST_REJECTED)");
							sendFileRequestRejected(ncFileRequestMessage.getUser(), ncFileRequestMessage.getUser_receiver(), ncFileRequestMessage.getFilename(), ncFileRequestMessage.getFilesize(),ncFileRequestMessage.getFiletransfer());
						}
						break;
					case NCMessage.OP_DOWNLOAD_FILE:
						System.out.println("RCV(DOWNLOAD_FILE)");
					
						NCFileRequestMessage ncFileRequestMessage2 = (NCFileRequestMessage)message;

						roomManager.broadcastDownloadFile(ncFileRequestMessage2.getUser(), ncFileRequestMessage2.getUser_receiver(), ncFileRequestMessage2.getFilename(), ncFileRequestMessage2.getFilesize(), ncFileRequestMessage2.getFiletransfer());

						
						break;
						
					case NCMessage.OP_SEND_FILE_REQUEST_DENY:
						System.out.println("RCV(SEND_FILE_REQUEST_DENY)");
					
						NCFileRequestMessage ncFileRequestMessage3 = (NCFileRequestMessage)message;

						roomManager.broadcastFileRequestDeny(ncFileRequestMessage3.getUser(), ncFileRequestMessage3.getUser_receiver(), ncFileRequestMessage3.getFilename(), ncFileRequestMessage3.getFilesize(), ncFileRequestMessage3.getFiletransfer());

						
						break;
					
					case NCMessage.OP_SEND_FILE_DATA:
						System.out.println("RCV(SEND_FILE_DATA)");
						
						
						NCFileDataMessage ncFileDataMessage = (NCFileDataMessage)message;
					

						roomManager.broadcastDataFile(ncFileDataMessage.getUser(), ncFileDataMessage.getData());

						
						break;
						
					case NCMessage.OP_RENAME_ROOM:
						System.out.println("RCV(RENAME_ROOM)");
					
						NCRoomMessage ncRoomMessage = (NCRoomMessage)message;
						
						boolean isOk = serverManager.setRoomName(currentRoom, ncRoomMessage.getName());
						if(isOk) {
							System.out.println("SND(RENAME_ROOM_OK)");

							sendRenameRoomOk();
							this.currentRoom = ncRoomMessage.getName();
							roomManager.broadcastRoomNameChanged(currentRoom);

						}else {
							System.out.println("SND(RENAME_ROOM_REJECTED)");

							sendRenameRoomRejected();
						}
						break;
						
					case NCMessage.OP_RENAME_ROOM_SERVERS:
						System.out.println("RCV(RENAME_ROOM)");
					
						NCRoomMessage ncRoomMessage2 = (NCRoomMessage)message;
						this.currentRoom = ncRoomMessage2.getName();
						break;
				}
				
				
			} catch (Exception e) {
				//If an error occurs with the communications the user is removed from all the managers and the connection is closed
				System.out.println("* User "+ user + " disconnected.");
				serverManager.leaveRoom(user, currentRoom);
				serverManager.removeUser(user);
				exit=true;
				shouldExit=true;
			}
			
			//TODO Se recibe el mensaje enviado por el usuario
			//TODO Se analiza el código de operación del mensaje y se trata en consecuencia
		}
	}
	
	

	private void sendMessagePrivateOk(String user, String user_receiver, String message) {
		NCPrivateChatMessage privateChatMessage = (NCPrivateChatMessage)NCMessage.makeChaPrivateMessage(NCMessage.OP_SEND_PRIVATE_OK, user, user_receiver, message);
		String rawMessage = privateChatMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void sendMessagePrivateRejected(String user, String user_receiver, String message) {
		NCPrivateChatMessage privateChatMessage = (NCPrivateChatMessage)NCMessage.makeChaPrivateMessage(NCMessage.OP_SEND_PRIVATE_REJECTED, user, user_receiver, message);
		String rawMessage = privateChatMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void sendFileRequestOk(String user, String user_receiver, String filename, long filesize, long filetransfer) {
		NCFileRequestMessage privateChatMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST_OK, user, user_receiver, filename, filesize, filetransfer);
		String rawMessage = privateChatMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void sendFileRequestRejected(String user, String user_receiver, String filename, long filesize, long filetransfer) {
		NCFileRequestMessage privateChatMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST_REJECTED, user, user_receiver, filename, filesize, filetransfer);
		String rawMessage = privateChatMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendRenameRoomOk() {
		NCControlMessage controlMessage = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_RENAME_ROOM_OK);
		String rawMessage = controlMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendRenameRoomRejected() {
		NCControlMessage controlMessage = (NCControlMessage)NCMessage.makeControlMessage(NCMessage.OP_RENAME_ROOM_REJECTED);
		String rawMessage = controlMessage.toEncodedString();
		
		//Enviamos el mensaje
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
