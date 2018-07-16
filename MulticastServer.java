import	java.io.*;
import	java.net.*;
import java.nio.*;
import java.util.*;

public class MulticastServer
{

	public static int packet_size = 1028;
	public static HashMap<Integer, ReceiverInfo> recmap = new HashMap<Integer, ReceiverInfo>();


    public static void main(String[] arg) throws IOException 
    {
      int	port = Integer.parseInt(arg[0]);
    	String fname = arg[1];
      int numofreceivers = Integer.parseInt(arg[2]);
     	Thread send = new Thread( new Sender(port, fname, numofreceivers) );
      //Thread listen = new Thread( new Listen(port, fname, numofreceivers) );
     	send.start();
      //listen.start();
    }

}