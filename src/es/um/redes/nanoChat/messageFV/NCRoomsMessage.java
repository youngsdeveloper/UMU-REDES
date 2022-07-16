package es.um.redes.nanoChat.messageFV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

/*
 * ROOMS DESCIPTION
----

operation: <operation>
room: <name1> <name2> <name3> ...
members: <room1.member1> <room1.member2> <room2.member1> ...
time_last_message: <time1> <time2> <time3> ...


*/


public class NCRoomsMessage extends NCMessage {

	private String rooms = "";
	private String members = "";
	private String timeLastRoomMessage = "";

	//Campo específico de este tipo de mensaje
	static protected final String ROOMS_FIELD = "rooms";
	static protected final String MEMBERS_FIELD = "members";
	static protected final String TIME_LAST_ROOM_MESSAGE_FIELD = "timeLastRoomMessage";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	
	
	public NCRoomsMessage(byte type, List<NCRoomDescription> roomList) {
		this.opcode = type;
		


		for(int i=0;i<roomList.size();i++){
			
			NCRoomDescription roomDescription = roomList.get(i);
			

			
			if(i>0){
				this.rooms = this.rooms.concat(",");
				this.members = this.members.concat(",");
				this.timeLastRoomMessage = this.timeLastRoomMessage.concat(",");

			}
			
			this.rooms = this.rooms.concat(roomDescription.roomName);
			this.members = this.members.concat(String.join("/", roomDescription.members));
			
			this.timeLastRoomMessage = this.timeLastRoomMessage.concat(String.valueOf(roomDescription.timeLastMessage));

		}
		
	}
	
	public NCRoomsMessage(byte type, String rooms, String members, String timeLastRoomMessage) {
		this.opcode = type;
		this.rooms = rooms;
		this.members = members;
		this.timeLastRoomMessage = timeLastRoomMessage;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(ROOMS_FIELD+DELIMITER+rooms+END_LINE); //Construimos el campo
		sb.append(MEMBERS_FIELD+DELIMITER+members+END_LINE); //Construimos el campo
		sb.append(TIME_LAST_ROOM_MESSAGE_FIELD+DELIMITER+timeLastRoomMessage+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCRoomsMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		String rooms = null;
		String members = null;
		String timeLastRoomMessage = null;

		int idx = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		String value = lines[1].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(ROOMS_FIELD))
			rooms = value;
		
		idx = lines[2].indexOf(DELIMITER);
		field = lines[2].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[2].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(MEMBERS_FIELD))
			members = value;
		
		idx = lines[3].indexOf(DELIMITER);
		field = lines[3].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[3].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(TIME_LAST_ROOM_MESSAGE_FIELD))
			timeLastRoomMessage = value;

		return new NCRoomsMessage(code, rooms, members, timeLastRoomMessage);
	}

	public String getRooms() {
		return rooms;
	}

	public String getMembers() {
		return members;
	}
	
	public String getTimeLastRoomMessage() {
		return timeLastRoomMessage;
	}
	
	public List<NCRoomDescription> getRoomsList() {
		
		String[] rooms_names = this.rooms.split(",");
		String[] rooms_members = this.members.split(",");
		String[] rooms_timeLastMessage = this.timeLastRoomMessage.split(",");

		List<NCRoomDescription> rooms = new ArrayList<NCRoomDescription>(rooms_names.length);
		
		for(int i=0;i<rooms_names.length;i++){

			String[] mem = {};
			
			
			if(rooms_members.length>0){
				if(i<rooms_members.length) {
					if(rooms_members[i].length()>0) {
						mem = rooms_members[i].split("/");
					}
				}
				
			}
			
			
			List<String> members = Arrays.asList(mem);

			Long timeLastMessage = Long.parseLong(rooms_timeLastMessage[i]);
			rooms.add(new NCRoomDescription(rooms_names[i], members, timeLastMessage));
		}
		
		return rooms;
	}
	
	
}
