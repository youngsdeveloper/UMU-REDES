package es.um.redes.nanoChat.messageFV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

/*
 * ROOM DESCIPTION
----

operation: <operation>
room: <name> 
members: <member1> <member2> <member3> ... 
time_last_message: <time> 


*/

public class NCRoomDescriptionMessage extends NCMessage {

	private String room;
	private String members;
	private String timeLastMessage;

	//Campo específico de este tipo de mensaje
	static protected final String ROOM_FIELD = "room";
	static protected final String MEMBERS_FIELD = "members";
	static protected final String TIME_LAST_MESSAGE_FIELD = "timeLastMessage";

	/**
	 * Creamos un mensaje de tipo RoomDescriptionMessage a partir del código de operación y del nombre
	 */
	
	public NCRoomDescriptionMessage(byte type, NCRoomDescription roomDesc) {
		this.opcode = type;	
		this.room = roomDesc.roomName; 
		this.members = String.join(",", roomDesc.members);
		this.timeLastMessage = String.valueOf(roomDesc.timeLastMessage);
	}
	
	//Constructor sobrecargado
	public NCRoomDescriptionMessage(byte type, String room, String members, String timeLastMessage) {
		this.opcode = type;
		this.room = room;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(ROOM_FIELD+DELIMITER+room+END_LINE); //Construimos el campo
		sb.append(MEMBERS_FIELD+DELIMITER+members+END_LINE); //Construimos el campo
		sb.append(TIME_LAST_MESSAGE_FIELD+DELIMITER+timeLastMessage+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCRoomDescriptionMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		String room = null;
		String members = null;
		String timeLastMessage = null;

		int idx = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		String value = lines[1].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(ROOM_FIELD))
			room = value;
		
		idx = lines[2].indexOf(DELIMITER);
		field = lines[2].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[2].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(MEMBERS_FIELD))
			members = value;
		
		idx = lines[3].indexOf(DELIMITER);
		field = lines[3].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[3].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(TIME_LAST_MESSAGE_FIELD))
			timeLastMessage = value;

		return new NCRoomDescriptionMessage(code, room, members, timeLastMessage);
	}

	public String getRoom() {
		return room;
	}

	public String getMembers() {
		return members;
	}
	
	public String getTimeLastMessage() {
		return timeLastMessage;
	}
	
	public NCRoomDescription getRoomDesc() {
		
		String room = this.room;
		String[] roomMembers = this.members.split(",");
		String roomTimeLastMessage = this.timeLastMessage;


		List<String> members = Arrays.asList(roomMembers);
		Long timeLastMessage = Long.parseLong(roomTimeLastMessage);
		NCRoomDescription roomDesc = new NCRoomDescription(room, members,timeLastMessage);
		return roomDesc;
	}
	
	
}
