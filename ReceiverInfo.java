import	java.net.*;
import	java.util.*;
import	java.io.*;

public class ReceiverInfo
{
	public int id;
	public int ack;
	public InetAddress addr;

	public ReceiverInfo(int id, int ack, InetAddress addr)
	{
		this.id = id;
		this.ack = ack;
		this.addr = addr;
	}

	public int getId()
	{
		return this.id;
	}

	public int getAck()
	{
		return this.ack;
	}

	public InetAddress getaddr()
	{
		return this.addr;
	}

	public void changeAck(int newack)
	{
		this.ack = newack;
	}
}