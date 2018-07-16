import	java.io.*;
import	java.net.*;
import java.nio.*;
import java.util.*;

public class Sender implements Runnable
{

	public static int packet_size = 1028;
	public static HashMap<InetAddress, ReceiverInfo> recmap = new HashMap<InetAddress, ReceiverInfo>();
	public static int port;
	public static String fname;
	public static int numofreceivers;

	public Sender(int p, String name, int receivers)
	{
		port = p;
		fname = name;
		numofreceivers = receivers;
	}


	public void run() 
	{
		try
		{
			InetAddress group = InetAddress.getByName( "255.255.255.255" );

			DatagramPacket ackpacket;
			DatagramPacket prevpacket;
			DatagramPacket sendPacket;
			DatagramSocket socket = new DatagramSocket();

			String s;
			int dead = 0;
			char color;
			int idnum = 100;
			int broadcastnext = 0;
			int seq_num = 0;
			int ack_num = -1;
			InetAddress addr;
			ReceiverInfo currentreceiver;

			byte[] c = new byte[2];
			byte[] ack = new byte[4];
			byte[] seqnum;

			File file = new File(fname);
			FileInputStream reader;
			reader = new FileInputStream(file);

			String[] filename = fname.split("\\.");
			String extension = filename[1];
			int filesize = (int)file.length();
			double num_packets = (filesize/1028);
		
			
			System.out.println( "datagram target is " + group + " port " + port );
			socket.setBroadcast( true );
			

			c = ByteBuffer.allocate(2).putChar('z').array();
			seqnum =  ByteBuffer.allocate(4).putInt(seq_num).array();
			byte[] ext = extension.getBytes();
			byte[] firstpacket = new byte[c.length + ext.length + seqnum.length];

			System.arraycopy(c, 0, firstpacket, 0, c.length);
			System.arraycopy(seqnum, 0, firstpacket, c.length, seqnum.length);
			System.arraycopy(ext, 0, firstpacket, (seqnum.length + c.length), ext.length);
			sendPacket = new DatagramPacket(firstpacket , firstpacket.length, group, port );
			prevpacket = sendPacket;
			
			
			c = ByteBuffer.allocate(2).putChar('r').array();
			socket.setSoTimeout(1000);
			while(recmap.size() != numofreceivers )
			{
				socket.send(sendPacket);
				Thread.sleep(1000);
				

				try
				{
					ackpacket = new DatagramPacket(ack, ack.length);
					socket.receive( ackpacket );
					ack_num = ByteBuffer.wrap(ack).getInt();

					if(ack_num == 9)
					{
						
						ReceiverInfo info = new ReceiverInfo(idnum, ack_num, ackpacket.getAddress()); 
						recmap.put(ackpacket.getAddress(), info);
						idnum++;
					}
				}
				catch(Exception e)
				{
					if(e.toString().equals("java.net.SocketTimeoutException: Receive timed out"))
					{
						continue;
					}
					else
					{
						System.out.println( "Exception in sender:" + e.toString() );
						e.printStackTrace();
					}
				}		
			}
			

			socket.setSoTimeout(5000);
		
			while(reader.available() != 0)
			{
				try
				{	
					if(broadcastnext == 0)
					{
						byte[] data = new byte[packet_size];
						reader.read(data);
						seq_num++;

						//System.out.println("\n" + new String(data, 0, data.length) + "\n");

						seqnum =  ByteBuffer.allocate(4).putInt(seq_num).array();
					
						color = ByteBuffer.wrap(c).getChar();

						if(color == 'r')
						{
							if(seq_num == Integer.MAX_VALUE)
							{	
								c = ByteBuffer.allocate(2).putChar('b').array();
							}	
							else
							{
								c = ByteBuffer.allocate(2).putChar('r').array();
							}
						}
						else if(color == 'b')
						{
							if(seq_num == Integer.MAX_VALUE)
							{	
								c = ByteBuffer.allocate(2).putChar('r').array();
							}	
							else
							{
								c = ByteBuffer.allocate(2).putChar('b').array();
							}
						}
						

						byte[] seqdataarr = new byte[data.length + seqnum.length + c.length];

						System.arraycopy(c, 0, seqdataarr, 0, c.length);
						System.arraycopy(seqnum, 0, seqdataarr, c.length, seqnum.length);
						System.arraycopy(data, 0, seqdataarr, (seqnum.length + c.length), data.length);

						sendPacket = new DatagramPacket( seqdataarr, seqdataarr.length, group, port );
	
						//System.out.println("Sending next");
						socket.send( sendPacket );
						//Thread.sleep(1000);
						prevpacket = sendPacket;
						broadcastnext = 1;
					}
					else if(broadcastnext == 1)
					{
						ackpacket = new DatagramPacket(ack, ack.length);
						socket.receive( ackpacket );

						dead = 0;
						ack_num = ByteBuffer.wrap(ack).getInt();

						addr = ackpacket.getAddress();
						currentreceiver = recmap.get(addr);
						currentreceiver.changeAck(ack_num);
						//System.out.println(ack_num);

						if(ack_num == 2)
						{
							System.out.println("A receiver has left");
							recmap.remove(addr);
							continue;
						}

						if(checkMap())
						{
							//System.out.println("broadcastnext");
							broadcastnext = 0;
						}
						else
						{
							//System.out.println("Sending previous");
							socket.send(prevpacket);
							//Thread.sleep(1000);
						}

					}
				}
				catch ( Exception e )
				{
					if(e.toString().equals("java.net.SocketTimeoutException: Receive timed out"))
					{
						
						if(dead == 3)
						{
							System.out.println("Reciever is offline\nBye");
							System.exit(0);
						}
						else
						{
							socket.send(prevpacket);
							//Thread.sleep(1000);
							dead++;
							continue;
						}

					}
					else
					{
						System.out.println( "Exception in sender:" + e.toString() );
						e.printStackTrace();
					}
				}
			}
			c = ByteBuffer.allocate(2).putChar('e').array();
			byte[] end_num = ByteBuffer.allocate(4).putInt(0).array();
			byte[] end = new byte[c.length + end_num.length];
			System.arraycopy(c, 0, end, 0, c.length);
			System.arraycopy(end_num, 0, end, c.length, end_num.length);
			sendPacket = new DatagramPacket( end, end.length, group, port );
			socket.send( sendPacket );
				
		}
		catch( Exception e)
		{
			System.out.println( "Exception in sender:" + e.toString() );
			e.printStackTrace();
		}
		System.out.println( "Normal end of sender." );

	}
	public void printMap() {
	    Iterator it = recmap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
	}

	public boolean checkMap()
	{
		ReceiverInfo info;
		Iterator it = recmap.entrySet().iterator();
		int i = 0;
		while(it.hasNext())
		{
			Map.Entry pair = (Map.Entry)it.next();
			info = (ReceiverInfo) pair.getValue();
			//System.out.println(info.getaddr() + ": " + info.getAck());
			
			if(info.getAck() == 1)
			{
				//System.out.println("found nack");
				return false;
			}
		}

		return true;
	}


}






















