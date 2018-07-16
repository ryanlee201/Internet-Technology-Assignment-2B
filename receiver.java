import	java.util.*;
import	java.io.*;
import	java.net.*;
import java.nio.*;

class receiver {

	public static int packet_size = 1028;

	public static void main( String [] arg ) throws Exception
	{
		String port = arg[0]; 
		int portnum = Integer.parseInt(port);
		DatagramSocket	socket = new DatagramSocket( portnum );

		byte [] seqnum = new byte[4];
		byte [] data = new byte[packet_size];
		byte[] color = new byte[2];
		byte []	seqdataarr = new byte[seqnum.length + data.length + color.length];
		byte []	ack = new byte[4];
		byte [] filesize = new byte[4];
		BufferedReader stdIn = new BufferedReader( new InputStreamReader( System.in ) );
		
		DatagramPacket	receivePacket = new DatagramPacket( seqdataarr, seqdataarr.length );
		DatagramPacket ackpacket;
		int i;
		DatagramPacket prevpacket;
		float percentage = Float.parseFloat(arg[1])/100;
		float percentage = 0;
		int num = 1;
		int senderseqnum;
		int prevsenderseqnum = -1;
		String s;
		char c;
		float randnum;
		File file = new File("receivedFile.");
		FileOutputStream out = new FileOutputStream(file);



		try
		{
			socket.setReuseAddress( true );
			socket.setSoTimeout(5000);
			socket.receive(receivePacket);
			
			for( ; ;) //i = 1;i <= 100;i++
			{
				if(stdIn.ready())
				{
					s = stdIn.readLine();
					if(s.equals("leave"))
					{
						ack = ByteBuffer.allocate(4).putInt(2).array();
						ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
						socket.send( ackpacket );
					}
				}

				randnum = (float) Math.random();

				socket.receive( receivePacket );


				System.arraycopy(seqdataarr, 0, color, 0, color.length);
				System.arraycopy(seqdataarr, color.length, seqnum, 0, seqnum.length);
				System.arraycopy(seqdataarr, (color.length + seqnum.length), data, 0, data.length);

				c = ByteBuffer.wrap(color).getChar();
				senderseqnum = ByteBuffer.wrap(seqnum).getInt();
	
				// System.out.println(c);
				//System.out.println("sequence number: " + senderseqnum + " Internal:  " + num + " Previous: " + prevsenderseqnum);
				
				//System.out.println(new String( receivePacket.getData(), 0, receivePacket.getLength() ));

				if(prevsenderseqnum == senderseqnum)
				{
					continue;
				}				
				else if(senderseqnum == 0)
				{

					String extension = new String(data, 0, data.length);
					String ext = extension.substring(0,3);
					//System.out.println(ext);
					String filename = "receivedFile." + ext; 
					//System.out.println(filename);
					String path = System.getProperty("user.dir");
					File filewithext = new File(filename);
					file.renameTo(filewithext);
					//System.out.println(file.getAbsolutePath());

					

					ack = ByteBuffer.allocate(4).putInt(9).array();
					ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
					socket.send( ackpacket );
		
					prevsenderseqnum = senderseqnum;
				}
				else
				{
					if(randnum < percentage)
					{
						ack = ByteBuffer.allocate(4).putInt(1).array();
						ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
						socket.send( ackpacket );
	
						//System.out.println("Lost packet " + senderseqnum);
					}
					else
					{
						/*if(senderseqnum != num)
						{
							System.out.println("here");
							ack = ByteBuffer.allocate(4).putInt(1).array();
							ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
							socket.send( ackpacket );
						}
						else
						{*/
							ack = ByteBuffer.allocate(4).putInt(0).array();
							ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
							socket.send( ackpacket );
							out.write(data);
							num++;
							System.out.println("here");
							prevsenderseqnum = senderseqnum;
						
					}
				}
			}
		}
		catch ( Exception e )
		{
			if(e.toString().equals("java.net.SocketTimeoutException: Receive timed out"))
			{
				ack = ByteBuffer.allocate(4).putInt(1).array();
				ackpacket = new DatagramPacket(ack, ack.length, receivePacket.getSocketAddress());
				socket.send( ackpacket );
			}
			else
			{
				System.out.println( "Exception in receiver:" + e.toString() );
				e.printStackTrace();
			}
		}

		System.out.println("Normal end of receiver");
	}
}










