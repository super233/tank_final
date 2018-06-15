package demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;

public class Proxy {

	private String ip = "";
	private int port = 0;

	private Socket socket = null;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;

	public Proxy(String ip, int port){
		this.ip = ip;
		this.port = port;
	}

	public boolean connect()
	{
		while(true) {
			try {
				socket = new Socket(this.ip, this.port);
				break;
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}
		System.out.println("connect to server!");
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean send(String message) {
		message = getLen(message) + message;
		try {
			this.writer.write(message);
			this.writer.flush();
			return true;
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private String getLen(String result) {
		int len = result.length();
		String r = String.valueOf(len);
		int last = 5 - r.length();
		for (int index = 0; index < last; index++) {
			r = '0' + r;
		}
		return r;
	}

	public String recieve() {
		String result = "";
		StringBuffer buf = new StringBuffer();
		try {
			int bracketCount = 0;
			while(true)
			{
				int a = reader.read();
				buf.append((char)a);
				if(a == '{')
				{
					bracketCount++;
					break;
				}
			}
			
			while(bracketCount != 0)
			{
				int a = reader.read();
				buf.append((char)a);
				if(a == '{')
				{
					bracketCount++;
				}
				if(a == '}')
				{
					bracketCount--;
				}
			}
		} catch (IOException e) {
			System.out.println("Read server response failed!.");
			e.printStackTrace();
			return "";
		}
		result = buf.toString();
		System.out.println("get the result from server:" + result);
		if (!result.startsWith("{")) {
			result = result.substring(result.indexOf("{"));
		}

		return result;
	}

	/*
	 * public void run() { String result = ""; while (true) { try { result =
	 * result + reader.readLine(); } catch (IOException e) {
	 * System.out.println("Read server response failed!."); e.printStackTrace();
	 * break; } // result = result.append(buf);
	 * System.out.println("get the result from server:" + result); if
	 * (!result.startsWith("{")) { result =
	 * result.substring(result.indexOf("{")); } JSONObject object = null; try {
	 * object = JSONObject.fromObject(result); result = ""; } catch (Exception
	 * e) { continue; } if (object.getString("msg_name").equals("leg_start")) {
	 * dealLegStart(object); } if (object.getString("msg_name").equals("round"))
	 * { dealRound(object); sendAction(); } if
	 * (object.getString("msg_name").equals("game_over")) { break; } }
	 * System.out.println("game over!");
	 * 
	 * close();
	 * 
	 * }
	 * 
	 * public void dealLegStart(JSONObject object) {
	 * System.out.println("do the leg start job....");
	 * 
	 * Information.getInstance().saveLegStart(object); }
	 * 
	 * public void dealRound(JSONObject object) {
	 * System.out.println("do the round job....");
	 * Information.getInstance().refreshRoundInfo(object); }
	 * 
	 * public void sendAction() { ActionBean bean =
	 * Information.getInstance().getActionInfo(); send(bean.toJson("action")); }
	 */

	public void close() {
		try {
			this.writer.close();
		} catch (Exception e) {
		}
		try {
			this.reader.close();
		} catch (Exception e) {
		}
		try {
			this.socket.close();
		} catch (Exception e) {
		}
	}
}