package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import es.um.redes.nanoChat.messageFV.NCChatMessage;
import es.um.redes.nanoChat.messageFV.NCControlMessage;
import es.um.redes.nanoChat.messageFV.NCFileDataMessage;
import es.um.redes.nanoChat.messageFV.NCFileRequestMessage;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCPrivateChatMessage;
import es.um.redes.nanoChat.messageFV.NCRoomDescriptionMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCRoomsMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		//DONE Se crea el socket a partir de la dirección proporcionada 
		
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		
		//DONE Se extraen los streams de entrada y salida
		
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());

		
	}


	
	//Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {

		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_NICK, nick);
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(rawMessage);
		//DONE Leemos el mensaje recibido como respuesta por el flujo de entrada 

		NCControlMessage res = (NCControlMessage)NCMessage.readMessageFromSocket(dis);

		//DONE Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
		return res.getOpcode()==NCMessage.OP_REGISTERED_NICK;
	}
	
	//Método para obtener la lista de salas del servidor
	public List<NCRoomDescription> getRooms() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		//DONE completar el método
		
		
		NCControlMessage message = (NCControlMessage) NCMessage.makeControlMessage(NCMessage.OP_QUERY_ROOMS);

		String rawMessage = message.toEncodedString();

		
		
		dos.writeUTF(rawMessage);

		NCRoomsMessage res = (NCRoomsMessage)NCMessage.readMessageFromSocket(dis);
		return res.getRoomsList();
	}
	
	//Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		//DONE completar el método
		
		
		
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_ENTER_ROOM, room);
		
		String rawMessage = message.toEncodedString();

		
		dos.writeUTF(rawMessage);

		NCControlMessage res = (NCControlMessage) NCMessage.readMessageFromSocket(dis);

		return res.getOpcode()==NCMessage.OP_IN_ROOM;
	}
	
	//Método para salir de una sala
	//public void leaveRoom(String room) throws IOException {
	public void leaveRoom() throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		//DONE completar el método
		NCControlMessage message =(NCControlMessage) NCMessage.makeControlMessage(NCMessage.OP_EXIT_ROOM_MESSAGE);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
	}
	
	//Método para solicitar la creacion de una sala
	public boolean createRoom(String room) throws IOException {
		
		
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_CREATE_ROOM, room);
		
		String rawMessage = message.toEncodedString();

			
		dos.writeUTF(rawMessage);

		NCMessage res = NCMessage.readMessageFromSocket(dis);

		return res.getOpcode()==NCMessage.OP_CREATE_ROOM_OK;
	}
	
	//Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	//IMPORTANTE!!
	//DONE Es necesario implementar métodos para recibir y enviar mensajes de chat a una sala
	
	
	public void sendChatMessage(String nickname, String chatMessage) throws IOException {
		
		
		NCChatMessage ncChatMessage = (NCChatMessage)NCMessage.makeChatMessage(NCMessage.OP_SEND_MESSAGE, nickname, chatMessage);

		String rawMessage = ncChatMessage.toEncodedString();
		
		dos.writeUTF(rawMessage);
	}
	
	public NCPrivateChatMessage sendPrivateChatMessage(String nickname, String user_reciver, String chatMessage) throws IOException {
		
		
		NCPrivateChatMessage ncPrivateChatMessage = (NCPrivateChatMessage)NCMessage.makeChaPrivateMessage(NCMessage.OP_SEND_PRIVATE_MESSAGE, nickname, user_reciver, chatMessage);

		
		String rawMessage = ncPrivateChatMessage.toEncodedString();

		
		dos.writeUTF(rawMessage);
		
		
		NCPrivateChatMessage res = (NCPrivateChatMessage)NCMessage.readMessageFromSocket(dis);

		return res;

		
	}
	
	
	public NCFileRequestMessage sendFileRequestMessage(String nickname, String user_reciver, String filename, long filesize, long filetransfer) throws IOException {
		
		
		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST, nickname, user_reciver, filename, filesize, filetransfer);

		
		String rawMessage = ncFileRequestMessage.toEncodedString();
		
		dos.writeUTF(rawMessage);
	
		
		NCFileRequestMessage res = (NCFileRequestMessage)NCMessage.readMessageFromSocket(dis);

		return res;
		
	}
	
	public void sendFileRequestDeny(String nickname, String user_reciver, String filename, long filesize, long filetransfer) throws IOException {
		
		
		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST_DENY, nickname, user_reciver, filename, filesize, filetransfer);

		
		String rawMessage = ncFileRequestMessage.toEncodedString();
		
		dos.writeUTF(rawMessage);
		
		
	}
	
	public NCFileRequestMessage downloadFile(String nickname, String user_reciver, String filename, long filesize, long filetransfer) throws IOException {
		
		

		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_DOWNLOAD_FILE, nickname, user_reciver, filename, filesize, filetransfer);

		
		String rawMessage = ncFileRequestMessage.toEncodedString();

		
		dos.writeUTF(rawMessage);
		
		return ncFileRequestMessage;
		
		/*
		
        int num_kb_transfer = (int) (ncFileRequestMessage.getFiletransfer()/1024);

		

        
		int num_messages = (int) (num_kb_transfer/63);
		if(num_kb_transfer%63>0) {
			num_messages = num_messages + 1;
		}
		
		System.out.println("Voy a necesitar " + num_messages  + " mensajes... Esperando...");
		
		String data;
		
		for(int j=0;j<num_messages;j++) {
			
		}
		
		NCFileRequestMessage res = (NCFileRequestMessage)NCMessage.readMessageFromSocket(dis);

		return res;*/
		
	}
	
public void sendFileData(String user, String data) throws IOException {
		
		

		NCFileDataMessage ncFileDataMessage = (NCFileDataMessage)NCMessage.makeFileDataMessage(NCMessage.OP_SEND_FILE_DATA, user, data);
	
		
		String rawMessage = ncFileDataMessage.toEncodedString();

		
		dos.writeUTF(rawMessage);
		
	}
	
	
	public boolean renameRoom(String newRoomname) throws IOException {
		
		
		NCRoomMessage message = (NCRoomMessage)NCMessage.makeRoomMessage(NCMessage.OP_RENAME_ROOM, newRoomname);

		
		String rawMessage = message.toEncodedString();

		
		dos.writeUTF(rawMessage);
		
		
		NCMessage res = NCMessage.readMessageFromSocket(dis);

		return res.getOpcode()==NCMessage.OP_RENAME_ROOM_OK;

		
	}
	
	public void infoRoomRenamed(String newRoomname) throws IOException {
		
		
		NCRoomMessage message = (NCRoomMessage)NCMessage.makeRoomMessage(NCMessage.OP_RENAME_ROOM_SERVERS, newRoomname);

		
		String rawMessage = message.toEncodedString();

		
		dos.writeUTF(rawMessage);		
	}
	
	
	
	public NCMessage getIncommingMessage() throws IOException{
		
		
		return NCMessage.readMessageFromSocket(dis);
		
	}
	
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		//DONE Construimos el mensaje de solicitud de información de la sala específica

		NCControlMessage ncControlMessage = (NCControlMessage) NCControlMessage.makeControlMessage(NCMessage.OP_INFO_ROOM_MESSAGE);
		String rawMessage = ncControlMessage.toEncodedString();
		dos.writeUTF(rawMessage);
		//DONE Recibimos el mensaje de respuesta
		//DONE Devolvemos la descripción contenida en el mensaje
		
		NCRoomDescriptionMessage response = (NCRoomDescriptionMessage) NCMessage.readMessageFromSocket(dis);
		
		return response.getRoomDesc();
	}
	
	//Método para cerrar la comunicación con la sala
	//DONE (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			if (socket != null) {
				
				NCControlMessage ncControlMessage = (NCControlMessage) NCMessage.makeControlMessage(NCMessage.OP_QUIT_MESSAGE);
				String rawMessage = ncControlMessage.toEncodedString();
				dos.writeUTF(rawMessage);
				
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null;
		}
	}

}
