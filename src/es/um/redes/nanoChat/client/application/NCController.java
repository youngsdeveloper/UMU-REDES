package es.um.redes.nanoChat.client.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageFV.NCChatMessage;
import es.um.redes.nanoChat.messageFV.NCFileDataMessage;
import es.um.redes.nanoChat.messageFV.NCFileRequestMessage;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCPrivateChatMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	//Diferentes estados del cliente de acuerdo con el autómata
	
	
	enum Status {
		DISCONNECTED,
		PRE_CONNECTION,
		PRE_REGISTRATION,
		OUT_ROOM,
		IN_ROOM
	}
	
	//Código de protocolo implementado por este cliente
	//DONE Cambiar para cada grupo
	private static final int PROTOCOL = 108415633;
	//Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	//Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	//Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	//Último comando proporcionado por el usuario
	private byte currentCommand;
	//Nick del usuario
	private String nickname;
	//Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String room;
	
	//Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String create_room;

	//Mensaje enviado o por enviar al chat
	private String chatMessage;
	
	//Usuario a enviar el mensaje
	private String user_receiver;
	
	// Nuevo nombre para la sala
	private String new_room_name;
	
	// Path del archivo a enviar
	private String file_path;
	
	private boolean is_downloading = false;
	private int download_num_messages = 0;
	private int download_num_messages_received = 0;
	private String download_data = "";

	
	//Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	//Estado actual del cliente, de acuerdo con el autómata
	private Status clientStatus = Status.PRE_CONNECTION;
	
	
	private NCFileRequestMessage lastFileRquest = null;

	//Constructor
	public NCController() {
		shell = new NCShell();
	}

	//Devuelve el comando actual introducido por el usuario
	public byte getCurrentCommand() {		
		return this.currentCommand;
	}

	//Establece el comando actual
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	//Registra en atributos internos los posibles parámetros del comando tecleado por el usuario
	public void setCurrentCommandArguments(String[] args) {
		//Comprobaremos también si el comando es válido para el estado actual del autómata
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == Status.PRE_REGISTRATION)
				nickname = args[0];
			break;
		case NCCommands.COM_ENTER:
			room = args[0];
			break;
		case NCCommands.COM_CREATE_ROOM:
			create_room = args[0];
			break;
		case NCCommands.COM_SEND:
			chatMessage = args[0];
			break;
		case NCCommands.COM_SEND_PRIVATE:
			user_receiver = args[0];
			chatMessage = args[1];
			break;
		case NCCommands.COM_RENAME_ROOM:
			new_room_name = args[0];
			break;
		case NCCommands.COM_SEND_FILE:
			user_receiver = args[0];
			file_path = args[1].trim();
			break;
		default:
		}
	}

	//Procesa los comandos introducidos por un usuario que aún no está dentro de una sala
	public void processCommand() {
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == Status.PRE_REGISTRATION)
				registerNickName();
			else
				System.out.println("* You have already registered a nickname ("+nickname+")");
			break;
		case NCCommands.COM_ROOMLIST:
			//DONE LLamar a getAndShowRooms() si el estado actual del autómata lo permite
			if (clientStatus == Status.OUT_ROOM)
				getAndShowRooms();
			else
				System.out.println("Debes estar registrado y fuera de una sala para usar este comando");
			//DONE Si no está permitido informar al usuario
			break;
			
		case NCCommands.COM_ENTER:
			//DONE LLamar a enterChat() si el estado actual del autómata lo permite
			if (clientStatus == Status.OUT_ROOM)
				enterChat();
			else
				System.out.println("Debes estar registrado y fuera de una sala para usar este comando");
				//DONE Si no está permitido informar al usuario
			break;
			
		case NCCommands.COM_CREATE_ROOM:
			//DONE LLamar a createRoom() si el estado actual del autómata lo permite
			if (clientStatus == Status.OUT_ROOM)
				createRoom();
			else
				System.out.println("Debes estar registrado y fuera de una sala para usar este comando");
			//DONE Si no está permitido informar al usuario
			
			break;
		case NCCommands.COM_QUIT:
			//Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
			if (clientStatus != Status.IN_ROOM) {
				ncConnector.disconnect();
				clientStatus = Status.DISCONNECTED;
				directoryConnector.close();
			}else
				System.out.println("Debes estar registrado y fuera de una sala para usar este comando");
			break;
		default:
		}
	}
	
	//Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			//Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname);
			
			
			if (registered) {
				//DONE Si el registro fue exitoso pasamos al siguiente estado del autómata
				System.out.println("* Your nickname is now "+nickname);
				clientStatus = Status.OUT_ROOM;
			}
			else
				//En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");			
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	//Método que solicita al servidor de NanoChat la lista de salas e imprime el resultado obtenido
	private void getAndShowRooms() {
		//DONE Lista que contendrá las descripciones de las salas existentes
		//DONE Le pedimos al conector que obtenga la lista de salas ncConnector.getRooms()
		try {
			List<NCRoomDescription> rooms =  ncConnector.getRooms();
			
			//TODO Una vez recibidas iteramos sobre la lista para imprimir información de cada sala
			for(NCRoomDescription room:rooms) {
				System.out.println(room.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	//Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() {
		//DONE Se solicita al servidor la entrada en la sala correspondiente ncConnector.enterRoom()
		//DONE Si la respuesta es un rechazo entonces informamos al usuario y salimos
		//DONE En caso contrario informamos que estamos dentro y seguimos
		//DONE Cambiamos el estado del autómata para aceptar nuevos comandos
		
		try {
			boolean inRoom = ncConnector.enterRoom(room);
			if(inRoom){
				System.out.println("* ¡Estas dentro de la sala!");
				clientStatus = Status.IN_ROOM;
			}else {
				System.out.println("* Se ha producido un error al entrar en la sala. Revisa que esté bien escrita.");
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		do {
			//Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
			readRoomCommandFromShell();
			processRoomCommand();
		} while (currentCommand != NCCommands.COM_EXIT);
		System.out.println("* Your are out of the room");
		clientStatus = Status.OUT_ROOM;
		//DONE Llegados a este punto el usuario ha querido salir de la sala, cambiamos el estado del autómata
	}

	

	//Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void createRoom() {
		
		try {
			if(ncConnector.createRoom(create_room)){
				System.out.println("La sala ha sido creada correctamente");
			}else {
				System.out.println("Se ha producido un error al crear la sala. Pruebe con otro nombre.");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	//Método para procesar los comandos específicos de una sala, si el autómata los permite
	private void processRoomCommand() {
		
		
		// Todos estos comandos solo se pueden ejectura si estas dentro de una sala
		if(clientStatus != Status.IN_ROOM)
			return;
		
			
					
		switch (currentCommand) {
		//El usuario ha solicitado información sobre la sala y llamamos al método que la obtendrá
		case NCCommands.COM_ROOMINFO:
				getAndShowInfo();
			
			break;
			
		//El usuario quiere enviar un mensaje al chat de la sala
		case NCCommands.COM_SEND:
				sendChatMessage();
			break;
			
			
		case NCCommands.COM_SEND_PRIVATE:
			sendPrivateChatMessage();
			break;
		
		case NCCommands.COM_SEND_FILE:
			sendFile();
			break;
			
		case NCCommands.COM_CONFIRM_FILE:
			confirmFile();
			break;
		case NCCommands.COM_DENY_FILE:
			denyFile();
			break;
		//En este caso lo que ha sucedido es que hemos recibido un mensaje desde la sala y hay que procesarlo
		case NCCommands.COM_SOCKET_IN:
				processIncommingMessage();
			break;
			
		case NCCommands.COM_RENAME_ROOM:
				renameRoom();
				break;
			
			//El usuario quiere salir de la sala
		case NCCommands.COM_EXIT:
				exitTheRoom();
			break;
		}		
	}

	//Método para solicitar al servidor la información sobre una sala y para mostrarla por pantalla
	private void getAndShowInfo() {
		//DONE Pedimos al servidor información sobre la sala en concreto
		//DONE Mostramos por pantalla la información
		
		try {
			NCRoomDescription roomDesc = ncConnector.getRoomInfo();
			System.out.println(roomDesc.toString());
		} catch (IOException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
		
		
		
	}

	//Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() {
		//DONE Mandamos al servidor el mensaje de salida
		//DONE Cambiamos el estado del autómata para indicar que estamos fuera de la sala
		try {
			ncConnector.leaveRoom();
			clientStatus = Status.OUT_ROOM;
			room = null;
			this.lastFileRquest = null; //Invalidate last file request

			
		} catch (IOException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
		
	}

	//Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() {
		//DONE Mandamos al servidor un mensaje de chat
		
		try {
			ncConnector.sendChatMessage(nickname, chatMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void sendPrivateChatMessage() {
		try {
			
			
			
			NCPrivateChatMessage ncPrivateChatMessage = ncConnector.sendPrivateChatMessage(nickname, user_receiver, chatMessage);
						
			if(ncPrivateChatMessage.getOpcode() == NCMessage.OP_SEND_PRIVATE_OK) {
				
				System.out.println("(private) " + ncPrivateChatMessage.getUser() + " => " + ncPrivateChatMessage.getUser_receiver() +": "+ncPrivateChatMessage.getMessage());
				
			}else {
				System.out.println("[Error al enviar este mensaje, por favor comprueba que el usuario existe y  esta conectado en esta sala] => (privado) " + ncPrivateChatMessage.getUser()+": "+ncPrivateChatMessage.getMessage());

			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void sendFile(){
		
		
		try {
		      File file = new File("Ficheros/"+file_path);
		      //Scanner myReader = new Scanner(file);
		      /*
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        System.out.println(data);
		      }
		      myReader.close();
		      */
		      
		      // Enviamos la peticion
		      
	          long filesize = file.length();
	          
	          byte[] fileContent = Files.readAllBytes(file.toPath());
	          String base64_file = Base64.getEncoder().encodeToString(fileContent);

	          long filetransfer = base64_file.getBytes().length;
	          
	          
		      NCFileRequestMessage res = ncConnector.sendFileRequestMessage(nickname, user_receiver, file_path, filesize, filetransfer);
		      
		      

		      
		      if(res.getOpcode() == NCMessage.OP_SEND_FILE_REQUEST_OK) {
					
		    	  //OK

					System.out.println("(file request) " + res.getUser() + " => " + res.getUser_receiver() +": "+res.getFilename() + " [Esperando confirmación...]");

		      }else {
					System.out.println("[Error al enviar este archivo, por favor comprueba que el usuario existe y  esta conectado en esta sala] => (privado) " + res.getUser()+": "+res.getFilename());

		      }
		      
	    }catch (NoSuchFileException e) {
	    	// El fichero no existe
	    	System.out.println("[ERROR] No existe el fichero que intentas enviar. \"" + "Ficheros/"+file_path +"\"");
	    	System.out.println("[AYUDA] El fichero deben estar en la carpeta /Ficheros.");
	    } 
		catch (FileNotFoundException e) {
	    	// El fichero no existe
	    	System.out.println("[ERROR] No existe el fichero que intentas enviar. \"" + "Ficheros/"+file_path +"\"");
	    	System.out.println("[AYUDA] El fichero deben estar en la carpeta /Ficheros.");
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	private void confirmFile() {
		if(this.lastFileRquest==null) {
			System.out.println("No existe ninguna petición de envío de fichero que confirmar.");
			return;
		}

		is_downloading = true;

		
		int num_transfer_kb = (int)this.lastFileRquest.getFiletransfer()/1024;
		
        int num_messages = (num_transfer_kb/63);
        
        if(num_transfer_kb%63>0 || num_messages==0) {
        	num_messages++;
        }
        
        download_num_messages = num_messages;


		
		try {
			NCFileRequestMessage req = ncConnector.downloadFile(this.lastFileRquest.getUser_receiver(), this.lastFileRquest.getUser(), this.lastFileRquest.getFilename(), this.lastFileRquest.getFilesize(), this.lastFileRquest.getFiletransfer());
			System.out.println("Descargando archivo...");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void denyFile() {
		if(this.lastFileRquest==null) {
			System.out.println("No existe ninguna petición de envío de fichero que denegar.");
			return;
		}
		
		
		try {
			
			
			ncConnector.sendFileRequestDeny(this.lastFileRquest.getUser_receiver(), this.lastFileRquest.getUser(), this.lastFileRquest.getFilename(), this.lastFileRquest.getFilesize(), this.lastFileRquest.getFiletransfer());
			System.out.println("Enviando el rechazo de la petición...");
			this.lastFileRquest = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void renameRoom() {
		try {
			
			
			
			if(ncConnector.renameRoom(new_room_name)){
				System.out.println("Nombre de sala cambiada a : " + new_room_name);
			}else {
				System.out.println("Ha habido un error al cambiar el nombre de la sala. ¿Quiza exista ya una sala con ese nombre?");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	//Método para procesar los mensajes recibidos del servidor mientras que el shell estaba esperando un comando de usuario
	private void processIncommingMessage() {		
		//DONE Recibir el mensaje
		
		
		try {
			NCMessage message = ncConnector.getIncommingMessage();
			
			switch(message.getOpcode()) {
				case NCMessage.OP_SEND_MESSAGE_CLIENTS:
					
					NCChatMessage chatMessage = (NCChatMessage)message;
					System.out.println(chatMessage.getUser() + ": " + chatMessage.getMessage());

				
					break;
					
				case NCMessage.OP_NOTIFY_ROOM_ENTER:
					
					NCRoomMessage roomMessage = (NCRoomMessage)message;
					System.out.println(roomMessage.getName() + " se ha conectado a la sala de chat.");

				
					break;
					
				case NCMessage.OP_NOTIFY_ROOM_EXIT:
					
					NCRoomMessage roomMessage2 = (NCRoomMessage)message;
					System.out.println(roomMessage2.getName() + " se ha desconectado de la sala de chat.");

				
					break;
					
				case NCMessage.OP_SEND_PRIVATE_MESSAGE_CLIENTS:
					
					NCPrivateChatMessage privateChatMessage = (NCPrivateChatMessage)message;
					System.out.println("(private) " + privateChatMessage.getUser() + " => " + privateChatMessage.getUser_receiver() + ": " + privateChatMessage.getMessage());

				
					break;
				case NCMessage.OP_RENAME_ROOM_CLIENTS:
					
					NCRoomMessage roomMessage3 = (NCRoomMessage)message;

					this.room = roomMessage3.getName();

					System.out.println("El nombre de la sala de chat ha cambiado a: " + this.room);

					ncConnector.infoRoomRenamed(this.room);
				
					break;
				case NCMessage.OP_SEND_FILE_REQUEST_CLIENTS:
					
					NCFileRequestMessage res = (NCFileRequestMessage)message;

					System.out.println("(file request) " + res.getUser() + " => " + res.getUser_receiver() +": "+res.getFilename() + " [Esperando confirmación...]");
					System.out.println("Peso: " + res.getFilesize()/1024 + "kB");
					System.out.println("Transferencia: " + res.getFiletransfer()/1024 + "kB");

					System.out.println("Escribe \"confirm_file\" para confirmar, \"deny_file\" para rechazar.");
					this.lastFileRquest = res;
					break;
					
				case NCMessage.OP_DOWNLOAD_FILE_CLIENT:
					
					NCFileRequestMessage res2 = (NCFileRequestMessage)message;

					
					System.out.println("(file transfer) Solicitado envio de fichero");
					System.out.println("[Enviando...] " + res2.getUser_receiver() + " => " +res2.getUser());
					
					try {
					      File file = new File("Ficheros/"+file_path);
					      Scanner myReader = new Scanner(file);
					      
					      
					      
					      
					      // Enviamos la peticion
					      
				          long filesize = file.length();
				          
				          byte[] fileContent = Files.readAllBytes(file.toPath());
				          String base64_file = Base64.getEncoder().encodeToString(fileContent);
				          
				          int num_kb = base64_file.getBytes().length/1024;
				          
				          
				          int num_messages = num_kb / 63;
			        	  
			        	  if(num_kb%63>0 || num_messages==0) {
			      			num_messages = num_messages + 1;
			      		  }
			        	  
			        	  			        	  
			        	  // Trozeamos paquetes
				          for(int j=0;j<num_messages;j++) {
				        	  int kb = 1024;
				        	  String data;
				        	  if(j+1==num_messages) {
				        		  // Si es el ultimo, mostrar hasta el final
				        		  data = base64_file.substring(j*63*kb);

				        	  }else {
				        		  data = base64_file.substring(j*63*kb, (j+1)*63*kb);

				        	  }

				        	  ncConnector.sendFileData(res2.getUser(), data); // Enviamos la data troceada
				        	  
				        	  float current_bytes = (j+1)*63*kb;
				        	  
				        	  if(j+1==num_messages) {
					        		// Si es la ultima, 100%
					        		current_bytes = num_kb;
					        }
				        	  
				        	  float total_bytes = num_kb;
				        	  float percent = (current_bytes/total_bytes)*100;
				        	  
				        	  
				        	  System.out.println(percent + "%" + " " + current_bytes/kb + "kb de " + total_bytes +"kb enviados...");
				          }
				          System.out.println("Transferencia completada");
				          
				          
				          //System.out.println(base64_file);
					      
				    } catch (FileNotFoundException e) {
				    	// El fichero no existe
				    	System.out.println("[ERROR] No existe el fichero que intentas enviar. \"" + "Ficheros/"+file_path +"\"");
				    	System.out.println("[AYUDA] El fichero deben estar en la carpeta /Ficheros.");
				    } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					break;
				case NCMessage.OP_SEND_FILE_DATA_CLIENTS:
					
						if(is_downloading) {
							
							NCFileDataMessage ncFileDataMessage = (NCFileDataMessage)message;
							
							download_num_messages_received++;
							
							
							this.download_data = download_data.concat(ncFileDataMessage.getData());
							
							System.out.println("");
							
							int kb = 1024;
				        	float current_bytes = (download_num_messages_received)*63*kb;
				        	
				        	if(download_num_messages_received==download_num_messages) {
				        		// Si es la ultima, 100%
				        		current_bytes = lastFileRquest.getFiletransfer();
				        	}
				        	
				        	float total_bytes = lastFileRquest.getFiletransfer();
				        	float percent = (current_bytes/total_bytes)*100;
				        	  
				        	  
				        	System.out.println(percent + "%" + " " + current_bytes/kb + "kb de " + total_bytes/kb +"kb enviados...");

				        	
							if(download_num_messages_received==download_num_messages) {
								System.out.println("Descarga finalizada, guardando fichero...");
								
								

					        	// Guardamos fichero
					        	try {
					        		
					        	  
				        	      File file = new File("Descargas/"+this.lastFileRquest.getFilename());
				        	      if (!file.createNewFile()) {
				        	    	  int rand_number = new Random().nextInt(500);
				        	    	  file= new File("Descargas/"+rand_number+"_"+lastFileRquest.getFilename());
				        	      }
				        	      
				        	      
				        	      try (FileOutputStream outputStream = new FileOutputStream(file)) {
				        	          outputStream.write(Base64.getDecoder().decode(download_data));
				        	      }
				        	      
				        	      
									System.out.println("Fichero disponible en " + file.getPath());


				        	    } catch (IOException e) {
				        	      System.out.println("An error occurred.");
				        	      e.printStackTrace();
				        	    }
					        	

					        	
								
								// Limpiamos variables
								is_downloading=false;
								download_num_messages=0;
								download_num_messages_received=0;
								download_data = "";
								this.lastFileRquest = null;
								
							}

							
						}
						break;
						
					case NCMessage.OP_SEND_FILE_REQUEST_DENY_CLIENTS:
						
						NCFileRequestMessage ncFileRequestMessage = (NCFileRequestMessage)message;
						
						System.out.println("El usuario ["+ncFileRequestMessage.getUser()+"] ha denegado tu petición de envio de archivo para el fichero: \""+ncFileRequestMessage.getFilename()+"\"");


					
						break;
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//DONE En función del tipo de mensaje, actuar en consecuencia
		//DONE (Ejemplo) En el caso de que fuera un mensaje de chat de broadcast mostramos la información de quién envía el mensaje y el mensaje en sí
	}

	//MNétodo para leer un comando de la sala 
	public void readRoomCommandFromShell() {
		//Pedimos un nuevo comando de sala al shell (pasando el conector por si nos llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		//Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		//Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		//Pedimos el comando al shell
		shell.readGeneralCommand();
		//Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		//Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		//Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		//Intentamos obtener la dirección del servidor de NanoChat que trabaja con nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			serverAddress = null;
		}
		//Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");		
			return false;
		}
		else return true;
	}
	
	//Método para establecer la conexión con el servidor de Chat (a través del NCConnector)
	public boolean connectToChatServer() {
			try {
				//Inicializamos el conector para intercambiar mensajes con el servidor de NanoChat (lo hace la clase NCConnector)
				ncConnector = new NCConnector(serverAddress);
			} catch (IOException e) {
				System.out.println("* Check your connection, the game server is not available.");
				serverAddress = null;
			}
			//Si la conexión se ha establecido con éxito informamos al usuario y cambiamos el estado del autómata
			if (serverAddress != null) {
				System.out.println("* Connected to "+serverAddress);
				clientStatus = Status.PRE_REGISTRATION;
				return true;
			}
			else return false;
	}

	//Método que comprueba si el usuario ha introducido el comando para salir de la aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.COM_QUIT;
	}

}
