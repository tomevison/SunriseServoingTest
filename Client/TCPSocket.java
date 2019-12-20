package ServoingTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.inject.Inject;

import com.kuka.task.ITaskLogger;

public class TCPSocket {

	Socket s = null;
	@Inject	
	private ITaskLogger log;

	public TCPSocket(int PORT, String HOST) throws UnknownHostException, IOException {
		//super();
		
		//getLogger().info("Attempt to connect to the server: " + HOST + ":" + PORT);
		s = new Socket(HOST, PORT);
		//getLogger().info("Connected");
		
	}
	
	// write a String to the TCP socket
	public void write(String packets) throws IOException{
		
		OutputStream out = s.getOutputStream();
		PrintWriter writer = new PrintWriter(out, true);
		writer.println(packets);
	}
	
	// read a String from the TCP socket
	public String read() throws IOException{
		
		InputStream in = s.getInputStream();
		
		// keep reading from socket until all bytes are consumed
		String raw = "";
		int c;
		do {
			c = in.read();
			raw+=(char)c;
		} while(in.available()>0);
		
		return raw;
	}
	


}
