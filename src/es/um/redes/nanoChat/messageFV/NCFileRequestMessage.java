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
public class NCFileRequestMessage extends NCMessage {

	private String user = "";
	private String user_receiver = "";
	private String filename = "";
	private long filesize = 0;
	private long filetransfer = 0;

	//Campo específico de este tipo de mensaje
	static protected final String USER_FIELD = "user";
	static protected final String USER_RECEIVER_FIELD = "user_receiver";
	static protected final String FILENAME_FIELD = "filename";
	static protected final String FILESIZE_FIELD = "filesize";
	static protected final String FILETRANSFER_FIELD = "filetranfser";

	
	public NCFileRequestMessage(byte type, String user, String user_receiver,String filename, long filesize, long filetransfer) {
		this.opcode = type;
		this.user = user;
		this.user_receiver = user_receiver;
		this.filename = filename;
		this.filesize = filesize;
		this.filetransfer = filetransfer;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(USER_FIELD+DELIMITER+user+END_LINE); //Construimos el campo
		sb.append(USER_RECEIVER_FIELD+DELIMITER+user_receiver+END_LINE); //Construimos el campo
		sb.append(FILENAME_FIELD+DELIMITER+filename+END_LINE); //Construimos el campo
		sb.append(FILESIZE_FIELD+DELIMITER+filesize+END_LINE); //Construimos el campo
		sb.append(FILETRANSFER_FIELD+DELIMITER+filetransfer+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCFileRequestMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		
		String user = null;
		String user_receiver = null;
		String filename = null;
		long filesize = 0;
		long filetransfer = 0;

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
		if (field.equalsIgnoreCase(FILENAME_FIELD))
			filename = value;
		
		
		idx = lines[4].indexOf(DELIMITER);
		field = lines[4].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[4].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(FILESIZE_FIELD))
			filesize = Long.parseLong(value);
		
		idx = lines[5].indexOf(DELIMITER);
		field = lines[5].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
		value = lines[5].substring(idx + 1).trim();
		if (field.equalsIgnoreCase(FILETRANSFER_FIELD))
			filetransfer = Long.parseLong(value);
		
		

		return new NCFileRequestMessage(code, user, user_receiver, filename, filesize, filetransfer);
	}

	
	public String getUser() {
		return user;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public long getFilesize() {
		return filesize;
	}
	
	public long getFiletransfer() {
		return filetransfer;
	}
	
	public String getUser_receiver() {
		return user_receiver;
	}
}
