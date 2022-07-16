package es.um.redes.nanoChat.server.roomManager;

import java.util.Date;
import java.util.List;

public class NCRoomDescription {
	//Campos de los que, al menos, se compone una descripción de una sala
	public String roomName;
	public List<String> members;
	public long timeLastMessage;

	//Constructor a partir de los valores para los campos
	public NCRoomDescription(String roomName, List<String> members, long timeLastMessage) {
		this.roomName = roomName;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}

	//Método que devuelve una representación de la Descripción lista para ser impresa por pantalla
	public String toPrintableString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Room Name: "+roomName+"\t Members ("+members.size()+ ") : ");
		for (String member: members) {
			sb.append(member+" ");
		}
		if (timeLastMessage != 0)
			sb.append("\tLast message: "+new Date(timeLastMessage).toString());
		else
			sb.append("\tLast message: not yet");
		return sb.toString();
	}
	
	
	@Override
	public String toString() {
		return toPrintableString();
	}
	
	
}
