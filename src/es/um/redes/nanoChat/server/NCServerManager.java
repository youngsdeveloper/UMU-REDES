package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;
import es.um.redes.nanoChat.server.roomManager.NCRoomManagerImpl;

/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada con cada sala particular)
 */
class NCServerManager {

	//Primera habitación del servidor
	final static byte INITIAL_ROOM = 'A';
	final static String ROOM_PREFIX = "Room";
	//Siguiente habitación que se creará
	byte nextRoom;
	//Usuarios registrados en el servidor
	private Set<String> users = new HashSet<String>();
	//Habitaciones actuales asociadas a sus correspondientes RoomManagers
	private Map<String,NCRoomManager> rooms = new HashMap<String,NCRoomManager>();

	NCServerManager() {
		nextRoom = INITIAL_ROOM;
	}

	//Método para registrar un RoomManager
	
	public void registerRoomManager(NCRoomManager rm) {
		//DONE Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = ROOM_PREFIX + (char) nextRoom;
		rooms.put(roomName, rm);
		rm.setRoomName(roomName);
		nextRoom++; //Preparar siguiente sala

	}

	
	public boolean registerRoomManager(NCRoomManager rm, String name) {
		//Crear sala con nombre personalizado
		
		

		
		if(rooms.containsKey(name)) {
			return false;
		}
		
		rooms.put(name, rm);
		rm.setRoomName(name);
		
		return true;
	}

	//Devuelve la descripción de las salas existentes
	public synchronized List<NCRoomDescription> getRoomList() {
		//DONE Pregunta a cada RoomManager cuál es la descripción actual de su sala
		//DONE Añade la información al ArrayList
		
		List<NCRoomDescription> roomList = new ArrayList<NCRoomDescription>(this.rooms.keySet().size());
		
		for(String roomKey:rooms.keySet()) {
			NCRoomManager roomManager = rooms.get(roomKey);
			NCRoomDescription des = roomManager.getDescription();
			roomList.add(des);
		}
		
		return roomList;
	}
	
	public synchronized NCRoomDescription getRoomDesc(String room) {
		return rooms.get(room).getDescription();
	}


	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		//DONE Devuelve true si no hay otro usuario con su nombre
		
		//DONE Devuelve false si ya hay un usuario con su nombre
		return users.add(user);
	}

	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//TODO Elimina al usuario del servidor
		users.remove(user);
	}

	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//DONE Verificamos si la sala existe
		
		NCRoomManager roomManager = rooms.getOrDefault(room, null);
		if(roomManager == null) {
			return null;
		}
		
		//DONE Decidimos qué hacer si la sala no existe (devolver error O crear la sala)
		//DONE Si la sala existe y si es aceptado en la sala entonces devolvemos el RoomManager de la sala
		
		
	
		return roomManager;

	}

	//Un usuario deja la sala en la que estaba
	public synchronized void leaveRoom(String u, String room) {
		//TODO Verificamos si la sala existe
		NCRoomManager roomManager = rooms.getOrDefault(room, null);
		
		//TODO Si la sala existe sacamos al usuario de la sala
		if(roomManager!=null) {
			roomManager.removeUser(u);
		}
		//TODO Decidir qué hacer si la sala se queda vacía
	}
	
	
	public synchronized boolean setRoomName(String room, String new_room_name){
		
		NCRoomManager roomManager = rooms.getOrDefault(room, null);

		if(roomManager==null)
			return false; // La sala actual no existe, devolver error
		
		
		if(rooms.containsKey(new_room_name))
			return false; // La sala nueva ya existe, devolver error
		
		roomManager.setRoomName(new_room_name);
		rooms.put(new_room_name, roomManager);
		rooms.remove(room);		
		
		return true;
		
		
	}
}
