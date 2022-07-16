package es.um.redes.nanoChat.server.roomManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoChat.messageFV.NCChatMessage;
import es.um.redes.nanoChat.messageFV.NCFileDataMessage;
import es.um.redes.nanoChat.messageFV.NCFileRequestMessage;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCPrivateChatMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCRoomsMessage;

public class NCRoomManagerImpl extends NCRoomManager{

	
	List<String> users;
	long timelastMessage;
	private Map<String,Socket> sockets = new HashMap<String,Socket>();

	
	public NCRoomManagerImpl() {
		users = new LinkedList<String>();
		timelastMessage = 0;
	}
	
	@Override
	public boolean registerUser(String u, Socket s) {
			
		boolean result = users.add(u);
		sockets.put(u, s);
		broacastUserEnter(u);
		return result;
	}

	@Override
	public void broadcastMessage(String u, String message) throws IOException {

		
		NCChatMessage ncChatMessage = (NCChatMessage)NCMessage.makeChatMessage(NCMessage.OP_SEND_MESSAGE_CLIENTS, u, message);
		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncChatMessage.toEncodedString();
		
		
		this.timelastMessage = System.currentTimeMillis();

			
		for(String user:sockets.keySet()){
			
		
			System.out.println("SND(SEND_MESSAGE_CLIENTS)");

			Socket s = sockets.get(user);
			
			
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			try {
				dos.writeUTF(rawMessage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	
	@Override
	public boolean broadcastPrivateMessage(String u, String user_receiver, String message) throws IOException {


		
		if(u.equals(user_receiver))
			return false;
		
		NCPrivateChatMessage ncPrivateChatMessage = (NCPrivateChatMessage)NCMessage.makeChaPrivateMessage(NCMessage.OP_SEND_PRIVATE_MESSAGE_CLIENTS, u, user_receiver,  message);
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncPrivateChatMessage.toEncodedString();
		
		Socket s = sockets.getOrDefault(user_receiver, null);
		
		if(s==null)
			return false;
		
		
		
		System.out.println("SND(SEND_PRIVATE_MESSAGE_CLIENTS)");

		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		try {
			dos.writeUTF(rawMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		
	}


	@Override
	public void removeUser(String u) {

		users.remove(u);
		broacastUserExit(u);
		
	}
	
	
	private void broacastUserEnter(String u){
		NCRoomMessage ncRoomMessage = (NCRoomMessage)NCMessage.makeRoomMessage(NCMessage.OP_NOTIFY_ROOM_ENTER, u);
		String rawMessage = ncRoomMessage.toEncodedString();

		for(String user:sockets.keySet()){
			
			if(user.equals(u)) {
				continue; // No notificar al mismo usuario que acaba de entrar
			}
			System.out.println("SND(NOTIFY_ROOM_ENTER)");

			
			Socket s = sockets.get(user);
			
			
			DataOutputStream dos;
			try {
				dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(rawMessage);

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	
	private void broacastUserExit(String u){
		NCRoomMessage ncRoomMessage = (NCRoomMessage)NCMessage.makeRoomMessage(NCMessage.OP_NOTIFY_ROOM_EXIT, u);
		String rawMessage = ncRoomMessage.toEncodedString();

		for(String user:sockets.keySet()){
			
			if(user.equals(u)) {
				continue; // No notificar al mismo usuario que acaba de salir
			}
			
			System.out.println("SND(NOTIFY_ROOM_EXIT)");

			
			Socket s = sockets.get(user);
			
			
			DataOutputStream dos;
			try {
				dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(rawMessage);

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		
		// Eliminamos el socket del cliente que acaba de salir
		sockets.remove(u);
	}
	
	
	@Override
	public void broadcastRoomNameChanged(String new_room_name){
		NCRoomMessage ncRoomMessage = (NCRoomMessage)NCMessage.makeRoomMessage(NCMessage.OP_RENAME_ROOM_CLIENTS, new_room_name);
		String rawMessage = ncRoomMessage.toEncodedString();

		for(String user:sockets.keySet()){
			
			
			System.out.println("SND(RENAME_ROOM_CLIENTS)");

			
			Socket s = sockets.get(user);
			
			
			DataOutputStream dos;
			try {
				dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(rawMessage);

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	@Override
	public void setRoomName(String roomName) {
		this.roomName = roomName;		
	}
	

	@Override
	public NCRoomDescription getDescription() {	
		return new NCRoomDescription(roomName, users, timelastMessage);
	}

	@Override
	public int usersInRoom() {
		return users.size();
	}
	
	public void setName(String newName){
		
		
		this.roomName = newName;
	}

	@Override
	public boolean broadcastFileRequest(String u, String user_receiver, String filename, long filesize, long filetransfer) throws IOException {

		
		if(u.equals(user_receiver))
			return false;
		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST_CLIENTS, u, user_receiver,  filename, filesize, filetransfer);
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncFileRequestMessage.toEncodedString();
		
		Socket s = sockets.getOrDefault(user_receiver, null);
		
		if(s==null)
			return false;
		
		
		
		System.out.println("SND(SEND_FILE_REQUEST_CLIENTS)");

		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		try {
			dos.writeUTF(rawMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		
	}

	@Override
	public boolean broadcastDownloadFile(String u, String user_receiver, String filename, long filesize, long filetransfer)
			throws IOException {

		
		if(u.equals(user_receiver))
			return false;
		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_DOWNLOAD_FILE_CLIENT, u, user_receiver,  filename, filesize, filetransfer);
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncFileRequestMessage.toEncodedString();
		
		Socket s = sockets.getOrDefault(user_receiver, null);
		
		if(s==null)
			return false;
		
		
		
		System.out.println("SND(DOWNLOAD_FILE_CLIENTS)");

		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		try {
			dos.writeUTF(rawMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean broadcastDataFile(String u, String data) throws IOException {

		
		NCFileDataMessage ncFileDataMessage = (NCFileDataMessage)NCMessage.makeFileDataMessage(NCMessage.OP_SEND_FILE_DATA_CLIENTS, u, data);
		
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncFileDataMessage.toEncodedString();
		
		Socket s = sockets.getOrDefault(u, null);
		
		if(s==null)
			return false;
		
		
		
		System.out.println("SND(DATA_FILE_CLIENTS)");

		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		try {
			dos.writeUTF(rawMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean broadcastFileRequestDeny(String u, String user_receiver, String filename, long filesize,
			long filetransfer) throws IOException {


		if(u.equals(user_receiver))
			return false;
		
		NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)NCMessage.makeFileRequestMessage(NCMessage.OP_SEND_FILE_REQUEST_DENY_CLIENTS, u, user_receiver,  filename, filesize, filetransfer);
		

		
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = ncFileRequestMessage.toEncodedString();
		
		Socket s = sockets.getOrDefault(user_receiver, null);
		
		if(s==null)
			return false;
		
		
		
		System.out.println("SND(SEND_FILE_REQUEST_DENY_CLIENTS)");

		DataOutputStream dos = new DataOutputStream(s.getOutputStream());

		try {
			dos.writeUTF(rawMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	

	

}
