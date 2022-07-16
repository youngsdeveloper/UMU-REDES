package es.um.redes.nanoChat.messageFV;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public abstract class NCMessage {
	protected byte opcode;

	//DONE Implementar el resto de los opcodes para los distintos mensajes
	public static final byte OP_INVALID_CODE = 0;
	public static final byte OP_NICK = 1;
	public static final byte OP_DUPLICATED_NICK = 2;
	public static final byte OP_REGISTERED_NICK = 3;
	public static final byte OP_QUERY_ROOMS = 4;
	public static final byte OP_QUERY_ROOMS_RESPONSE = 5;
	public static final byte OP_ENTER_ROOM = 6;
	public static final byte OP_IN_ROOM = 7;
	public static final byte OP_ENTER_ROOM_REJECTED = 8;
	public static final byte OP_SEND_MESSAGE = 9;
	public static final byte OP_SEND_MESSAGE_CLIENTS = 10;
	public static final byte OP_INFO_ROOM_MESSAGE = 11;
	public static final byte OP_INFO_ROOM_MESSAGE_RESPONSE = 12;
	public static final byte OP_EXIT_ROOM_MESSAGE = 13;
	public static final byte OP_QUIT_MESSAGE = 14;
	//Mensajes para funcionalidades opcionales
	public static final byte OP_CREATE_ROOM = 15;
	public static final byte OP_CREATE_ROOM_OK = 16;
	public static final byte OP_CREATE_ROOM_REJECTED = 17;

	public static final byte OP_NOTIFY_ROOM_ENTER = 18;
	public static final byte OP_NOTIFY_ROOM_EXIT = 19;

	
	public static final byte OP_SEND_PRIVATE_MESSAGE = 20;
	public static final byte OP_SEND_PRIVATE_MESSAGE_CLIENTS = 21;
	public static final byte OP_SEND_PRIVATE_OK = 22;
	public static final byte OP_SEND_PRIVATE_REJECTED= 23;

	
	public static final byte OP_RENAME_ROOM = 24;
	public static final byte OP_RENAME_ROOM_OK = 25;
	public static final byte OP_RENAME_ROOM_REJECTED = 26;

	
	public static final byte OP_RENAME_ROOM_CLIENTS = 27;
	public static final byte OP_RENAME_ROOM_SERVERS = 28;

	public static final byte OP_SEND_FILE_REQUEST = 29;
	public static final byte OP_SEND_FILE_REQUEST_CLIENTS = 30;
	public static final byte OP_SEND_FILE_REQUEST_OK = 31;
	public static final byte OP_SEND_FILE_REQUEST_REJECTED = 32;

	public static final byte OP_DOWNLOAD_FILE = 33;
	public static final byte OP_DOWNLOAD_FILE_CLIENT = 34;

	
	public static final byte OP_SEND_FILE_DATA = 35;
	public static final byte OP_SEND_FILE_DATA_CLIENTS = 36;

	public static final byte OP_SEND_FILE_REQUEST_DENY = 37;
	public static final byte OP_SEND_FILE_REQUEST_DENY_CLIENTS = 38;

	
	//Constantes con los delimitadores de los mensajes de field:value
	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea

	public static final String OPCODE_FIELD = "operation";

	/**
	 * Códigos de los opcodes válidos  El orden
	 * es importante para relacionarlos con la cadena
	 * que aparece en los mensajes
	 */
	private static final Byte[] _valid_opcodes = {
		OP_NICK,
		OP_DUPLICATED_NICK,
		OP_REGISTERED_NICK,
		OP_QUERY_ROOMS,
		OP_QUERY_ROOMS_RESPONSE,
		OP_ENTER_ROOM,
		OP_IN_ROOM,
		OP_ENTER_ROOM_REJECTED,
		OP_SEND_MESSAGE,
		OP_SEND_MESSAGE_CLIENTS,
		OP_INFO_ROOM_MESSAGE,
		OP_INFO_ROOM_MESSAGE_RESPONSE,
		OP_EXIT_ROOM_MESSAGE,
		OP_QUIT_MESSAGE,
		
		OP_CREATE_ROOM,
		OP_CREATE_ROOM_OK,
		OP_CREATE_ROOM_REJECTED,
		
		OP_NOTIFY_ROOM_ENTER,
		OP_NOTIFY_ROOM_EXIT,
		
		OP_SEND_PRIVATE_MESSAGE,
		OP_SEND_PRIVATE_MESSAGE_CLIENTS,
		
		OP_SEND_PRIVATE_OK,
		OP_SEND_PRIVATE_REJECTED,
		
		OP_RENAME_ROOM,
		OP_RENAME_ROOM_OK,
		OP_RENAME_ROOM_REJECTED,
		OP_RENAME_ROOM_CLIENTS,
		
		OP_RENAME_ROOM_SERVERS,
		OP_SEND_FILE_REQUEST,
		OP_SEND_FILE_REQUEST_CLIENTS,
		OP_SEND_FILE_REQUEST_OK,
		OP_SEND_FILE_REQUEST_REJECTED,
		
		OP_DOWNLOAD_FILE,
		OP_DOWNLOAD_FILE_CLIENT,
		
		OP_SEND_FILE_DATA,		
		OP_SEND_FILE_DATA_CLIENTS,
		
		OP_SEND_FILE_REQUEST_DENY,
		OP_SEND_FILE_REQUEST_DENY_CLIENTS

		

	};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_operations_str = {
		"Nick",
		"DuplicatedNick",
		"RegisteredNick",
		"QueryRooms",
		"QueryRoomsResponse",
		"EnterRoom",
		"InRoom",
		"EnterRoomReject",
		"SendMessage",
		"SendMessageClients",
		"InfoRoomMessage",
		"InfoRoomMessageResponse",
		"ExitRoomMessage",
		"QuitMessage",
		
		"CreateRoomMessage",
		"CreateRoomMessageOK",
		"CreateRoomMessageRejected",
		
		"NotifyRoomEnter",
		"NotifyRoomExit",
		
		
		"SendPrivateMessage",
		"SendPrivateMessageClients",
		
		"SendPrivateMessageOK",
		"SendPrivateMessageRejected",
		
		"RenameRoom",
		"RenameRoomOK",
		"RenameRoomRejected",
		
		"RenameRoomClients",
		"RenameRoomServers",
		
		"SendFileRequestMessage",
		"SendFileRequestMessageClients",
		
		"SendFileRequestMessageOK",
		"SendFileRequestMessageRejected",
		
		"DownloadFile",
		"DownloadFileClient",
		
		"SendFileData",
		"SendFileDataClients",
		
		"SendFileRequestDeny",
		"SendFileRequestDenyClients"


	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;
	
	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0 ; i < _valid_operations_str.length; ++i)
		{
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OP_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	protected static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}

	//Devuelve el opcode del mensaje
	public byte getOpcode() {
		return opcode;
	}

	//Método que debe ser implementado específicamente por cada subclase de NCMessage
	protected abstract String toEncodedString();

	//Extrae la operación del mensaje entrante y usa la subclase para parsear el resto del mensaje
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {
		String message = dis.readUTF();
		String[] lines = message.split(String.valueOf(END_LINE));
		
		
		if (!lines[0].isEmpty()) { // Si la línea no está vacía
			int idx = lines[0].indexOf(DELIMITER); // Posición del delimitador
			String field = lines[0].substring(0, idx).toLowerCase(); 																		// minúsculas
			String value = lines[0].substring(idx + 1).trim();
			

			if (!field.equalsIgnoreCase(OPCODE_FIELD))
				return null;
			byte code = operationToOpcode(value);
			if (code == OP_INVALID_CODE)
				return null;
			switch (code) {
			case OP_NICK:
			{

				return NCRoomMessage.readFromString(code, message);
			}
			case OP_REGISTERED_NICK:{

				return NCControlMessage.readFromString(code);
			}
			case OP_DUPLICATED_NICK:{
				return NCControlMessage.readFromString(code);
			}
			case OP_QUERY_ROOMS: {
				return NCControlMessage.readFromString(code);
			}
			
			case OP_QUERY_ROOMS_RESPONSE: {
				return NCRoomsMessage.readFromString(code, message);
			}
			case OP_ENTER_ROOM: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_IN_ROOM: {
				return NCControlMessage.readFromString(code);
			}
			case OP_ENTER_ROOM_REJECTED: {
				return NCControlMessage.readFromString(code);
			}
			
			case OP_SEND_MESSAGE: {
				return NCChatMessage.readFromString(code, message);
			}
			case OP_SEND_MESSAGE_CLIENTS: {
				return NCChatMessage.readFromString(code, message);
			}
			
			case OP_INFO_ROOM_MESSAGE: {
				return NCControlMessage.readFromString(code);
			}
			case OP_INFO_ROOM_MESSAGE_RESPONSE: {
				return NCRoomDescriptionMessage.readFromString(code, message);
			}
			
			case OP_EXIT_ROOM_MESSAGE: {
				return NCControlMessage.readFromString(code);
			}
			
			case OP_QUIT_MESSAGE: {
				return NCControlMessage.readFromString(code);
			}
			
			
			
			
			case OP_CREATE_ROOM: {
				return NCRoomMessage.readFromString(code, message);
			}
			
			case OP_CREATE_ROOM_OK: {
				return NCControlMessage.readFromString(code);
			}
			
			case OP_CREATE_ROOM_REJECTED: {
				return NCControlMessage.readFromString(code);
			}
			

			case OP_NOTIFY_ROOM_ENTER: {
				return NCRoomMessage.readFromString(code, message);
			}
			
			case OP_NOTIFY_ROOM_EXIT: {
				return NCRoomMessage.readFromString(code, message);
			}
			
			case OP_SEND_PRIVATE_MESSAGE: {
				return NCPrivateChatMessage.readFromString(code, message);
			}
			
			case OP_SEND_PRIVATE_MESSAGE_CLIENTS: {
				return NCPrivateChatMessage.readFromString(code, message);
			}
			
			case OP_SEND_PRIVATE_OK: {
				return NCPrivateChatMessage.readFromString(code, message);
			}
			
			
			case OP_SEND_PRIVATE_REJECTED: {
				return NCPrivateChatMessage.readFromString(code, message);
			}
			
			
			case OP_RENAME_ROOM: {
				return NCRoomMessage.readFromString(code, message);
			}
			
			case OP_RENAME_ROOM_OK: {
				return NCControlMessage.readFromString(code);
			}
			
			case OP_RENAME_ROOM_REJECTED: {
				return NCControlMessage.readFromString(code);
			}

			case OP_RENAME_ROOM_CLIENTS: {
				return NCRoomMessage.readFromString(code, message);
			}

			case OP_RENAME_ROOM_SERVERS: {
				return NCRoomMessage.readFromString(code, message);
			}
			
			case OP_SEND_FILE_REQUEST: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			case OP_SEND_FILE_REQUEST_CLIENTS: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			case OP_SEND_FILE_REQUEST_OK: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			case OP_SEND_FILE_REQUEST_REJECTED: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			
			case OP_DOWNLOAD_FILE: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			
			case OP_DOWNLOAD_FILE_CLIENT: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			
			case OP_SEND_FILE_DATA: {
				return NCFileDataMessage.readFromString(code, message);
			}
			
			case OP_SEND_FILE_DATA_CLIENTS: {
				return NCFileDataMessage.readFromString(code, message);
			}
			
			case OP_SEND_FILE_REQUEST_DENY: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			
			case OP_SEND_FILE_REQUEST_DENY_CLIENTS: {
				return NCFileRequestMessage.readFromString(code, message);
			}
			
			default:
				System.err.println("Unknown message type received:" + code);
				return null;
			}
		} else
			return null;
	}

	//Método para construir un mensaje de tipo NCMessage a partir del opcode
	public static NCMessage makeControlMessage(byte code) {
		return new NCControlMessage(code);
	}
	
	//Método para construir un mensaje de tipo Room a partir del opcode y del nombre
	public static NCMessage makeRoomMessage(byte code, String name) {
		return new NCRoomMessage(code, name);
	}
	
	public static NCMessage makeRoomsMessage(byte code, String rooms, String members, String timeLastRoomMessage) {
		return new NCRoomsMessage(code, rooms, members, timeLastRoomMessage);
	}
	
	public static NCMessage makeChatMessage(byte code, String user, String message) {
		return new NCChatMessage(code, user, message);
	}
	
	public static NCMessage makeChaPrivateMessage(byte code, String user, String user_receiver, String message) {
		return new NCPrivateChatMessage(code, user,user_receiver, message);
	}
	
	public static NCMessage makeRoomDescriptionMessage(byte code, String name, String members, String timeLastMessage) {
		return new NCRoomDescriptionMessage(code, name, members, timeLastMessage);
	}
	
	public static NCMessage makeFileRequestMessage(byte code, String user, String user_receiver, String filename, long filesize, long filetransfer) {
		return new NCFileRequestMessage(code, user,user_receiver, filename, filesize, filetransfer);
	}
	
	public static NCMessage makeFileDataMessage(byte code, String user, String data) {
		return new NCFileDataMessage(code,user, data);
	}
}
