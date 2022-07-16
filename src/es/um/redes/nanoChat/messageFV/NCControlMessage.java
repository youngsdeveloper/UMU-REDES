package es.um.redes.nanoChat.messageFV;

/*
 * Control
----

operation: <operation>
*/

public class NCControlMessage extends NCMessage {

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCControlMessage(byte type) {
		this.opcode = type;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCControlMessage readFromString(byte code) {
		return new NCControlMessage(code);
	}

	
}
