package es.um.redes.nanoChat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.server.roomManager.NCRoomManagerImpl;


public class NanoChatServer implements Runnable {

	public static final int PORT = 6969;

	private static final int MYPROTOCOL = 108415633;
	
    private InetSocketAddress socketAddress;
    private ServerSocket serverSocket = null;
    private NCServerManager manager;
    
	//Clase para comunicarse con el directorio Directory
	DirectoryConnector directory;
	
	//Dirección del directorio
	private static String directoryHostname;

    public static NanoChatServer create(int port) throws IOException
    {
    	return new NanoChatServer(new InetSocketAddress(port));
    }
    
    //Constructor del servidor
    private NanoChatServer(InetSocketAddress a)
    {
    	//Socket de comunicación del servidor de chat
    	this.socketAddress = a;
    	//Manager del servidor (compartido entre los Thread)
    	manager = new NCServerManager();
    	manager.registerRoomManager(new NCRoomManagerImpl());
    	//DONE Registramos una sala de chat en el servidor (subclase de NCRoomManager)
    	//DONE manager.registerRoomManager();
    }


    //Código principal del servidor
	public void run()
	{
   		try {
   			//El servidor realiza continuamente estas tareas en bucle
   			while (true)
   			{
   				// Espera nuevas conexiones
   				// Las accepta y obtiene el socket específico de cada conexión
   				Socket s = serverSocket.accept();
   				System.out.println("New client connected from " + s.getInetAddress().toString() + ":" + s.getPort());

   				// Se inicia un thread por cada conexión que recibe el Manager compartido y el socket por el que debe comunicarse
   				new NCServerThread(manager,s).start();
   			}
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
	}
    
    /**
     * Inicialización del servidor
     */
    public void init()
    {
        try {
        	// Se crea el socket de servidor y se asocia al puerto en el que debe escuchar
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " 
            		+ socketAddress.getPort() + ".");
            System.exit(-1);
        }

        //Después tenemos que registrar el servidor en el directorio para que los clientes lo puedan encontrar
		try {
			directory = new DirectoryConnector(directoryHostname);
			boolean registered = directory.registerServerForProtocol(MYPROTOCOL, PORT);
			if (!registered) {
				System.err.println("Could not register the server in the Directory: " 
						+ directoryHostname + ".");
				throw new IOException();
			}
   			directory.close();
		} catch (IOException e) {
            System.err.println("Could not communicate with the Directory: " 
            		+ directoryHostname + ".");
            System.exit(-1);
		}

        
        //Si todo ha ido bien entonces iniciamos el servidor en segundo plano
    	new Thread(this).start();
    	
    	System.out.println("Server running on port " +
    			socketAddress.getPort() + ".");
    }

    public static void main(String[] args) throws IOException
    {
    	//Verificamos que se nos proporcionan los parámetros necesarios, en este caso la dirección del directorio
       	if (args.length != 1) {
    		System.out.println("* Correct use: java NanoChatServer <DirectoryServer>");
    		return;
    	}
    	else 
    		directoryHostname = args[0];
       	NanoChatServer server = NanoChatServer.create(PORT);
     	server.init();
    }
}
