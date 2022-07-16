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
public class NCPrivateChatMessage extends NCMessage {

	private String user = "";
	private String user_receiver = "";
	private String message = "";

	//Campo específico de este tipo de mensaje
	static protected final String USER_FIELD = "user";
	static protected final String USER_RECEIVER_FIELD = "user_receiver";
	static protected final String MESSAGE_FIELD = "message";

	
	public NCPrivateChatMessage(byte type, String user, String user_receiver, String message) {
		this.opcode = type;
		this.user = user;
		this.user_receiver = user_receiver;
		this.message = message;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(USER_FIELD+DELIMITER+user+END_LINE); //Construimos el campo
		sb.append(USER_RECEIVER_FIELD+DELIMITER+user_receiver+END_LINE); //Construimos el campo
		sb.append(MESSAGE_FIELD+DELIMITER+message+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCPrivateChatMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		String user = null;
		String user_receiver = null;
		String message_chat = null;

		int idx = lines[1].indexOf(DELIMITER); // Posición del delimitador
		String field = lines[1].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		String value = lines[1].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(USER_FIELD))
			user = value;
		
		idx = lines[2].indexOf(DELIMITER);
		field = lines[2].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[2].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(USER_RECEIVER_FIELD))
			user_receiver = value;
		
		idx = lines[3].indexOf(DELIMITER);
		field = lines[3].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[3].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(MESSAGE_FIELD))
			message_chat = value;
		
		

		return new NCPrivateChatMessage(code, user, user_receiver, message_chat);
	}

	
	public String getUser() {
		return user;
	}

	public String getMessage() {
		return message;
	}
	
	public String getUser_receiver() {
		return user_receiver;
	}
}
