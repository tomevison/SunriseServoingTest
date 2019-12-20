//************************************
// PROJECT: SunriseServoingTest
// BY:      Tom Evison
// DATE:    10/12/2019
//                                   
// tested running Sunrise 1.16.2.16
//************************************

package ServoingTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

public class client {

	private static int PORT = 8080;
	private static String HOST = "172.31.1.150"; // server IP address
	static int answer;
	static InputStream input;
	
	public static void main(String[] args) {
		Socket s = null;
		try {
			System.out.println("Attempt to connect to the server: " + HOST + ":" + PORT);
			s = new Socket(HOST, PORT);
			System.out.println("Connected");
			
			while(true){
				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream();
			
				PrintWriter writer = new PrintWriter(out, true);
				writer.println("Hi there im a ROBOT!");
				
				String raw = "";
				int c;
				do {
					c = in.read();
					raw+=(char)c;
				} while(in.available()>0);
			    System.out.println("From server:" + raw);
			}
			
		} catch (IOException e) {
			System.out.println("Connection Failed");
			e.printStackTrace();
		}

	}

}
