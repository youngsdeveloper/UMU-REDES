package es.um.redes.nanoChat.messageFV;


/*
 * ChatMessage
----

operation: <operation>
user: <user>
user_receiver: <user_receiver>
message: <message>
\n

*/
public class NCFileDataMessage extends NCMessage {
	private String user = "";

	private String data = "";

	//Campo específico de este tipo de mensaje
	static protected final String USER_FIELD = "user";
	static protected final String DATA_FIELD = "data";

	
	public NCFileDataMessage(byte type, String user, String data) {
		this.opcode = type;
		this.user = user;
		this.data = data;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(USER_FIELD+DELIMITER+user+END_LINE); //Construimos el campo
		sb.append(DATA_FIELD+DELIMITER+data+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCFileDataMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		
		String user = null;
		String data = null;

		int idx = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		String value = lines[1].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(USER_FIELD))
			user = value;
		
		idx = lines[2].indexOf(DELIMITER); // Posición del delimitador
		field = lines[2].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[2].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(DATA_FIELD))
			data = value;
		
		

		return new NCFileDataMessage(code, user, data);
	}

	public String getUser() {
		return user;
	}
	
	public String getData() {
		return data;
	}
}
