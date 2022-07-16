package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;

public abstract class NCRoomManager {
	String roomName;
	
	
	

	//Método para registrar a un usuario u en una sala (se anota también su socket de comunicación)
	public abstract boolean registerUser(String u, Socket s);
	//Método para hacer llegar un mensaje enviado por un usuario u
	public abstract void broadcastMessage(String u, String message) throws IOException;
	//Método para eliminar un usuario de una sala
	public abstract void removeUser(String u);
	//Método para nombrar una sala
	public abstract void setRoomName(String roomName);
	//Método para devolver la descripción del estado actual de la sala
	public abstract NCRoomDescription getDescription();

	//Método para devolver el número de usuarios conectados a una sala
	public abstract int usersInRoom();
	
	//Método para hacer llegar un mensaje privado a un usuario user_receiver enviado por un usuario u
	public abstract boolean broadcastPrivateMessage(String u, String user_receiver, String message) throws IOException;


	
	public abstract void broadcastRoomNameChanged(String new_room_name);
	
	
	public abstract boolean broadcastFileRequest(String u, String user_receiver, String filename, long filesize, long filetransfer) throws IOException;
	public abstract boolean broadcastDownloadFile(String u, String user_receiver, String filename, long filesize, long filetransfer) throws IOException;
	public abstract boolean broadcastDataFile(String u, String data) throws IOException;
	public abstract boolean broadcastFileRequestDeny(String u, String user_receiver, String filename, long filesize, long filetransfer) throws IOException;

}
