package demo;

import demo.Proxy;
import net.sf.json.JSONObject;
import cmd.Registration;

import java.rmi.UnknownHostException;
import java.io.IOException;

import cmd.Message;
import cmd.RoundAction;

public class Demo {
	public static void main(String args[]) {
		String ip = args[1];
		int port = Integer.parseInt(args[2]);
		Proxy p = new Proxy(ip, port);
		p.connect();

		int team_id = Integer.parseInt(args[0]);
		String team_name = args[0];
		Client client = new Client(team_id, team_name);

		/* registration */
		Registration r = new Registration(team_id, team_name);
		Message m = new Message("registration", r);
		p.send(m.toString());

		while (true) {
			String str = p.recieve();
			JSONObject json;
			try {
				json = JSONObject.fromObject(str);
			} catch (Exception e) {
				System.out.println(e.toString());
				continue;
			}

			String msg_name = json.getString("msg_name");
			if (msg_name.equals("leg_start")) {
				client.legStart(json.getJSONObject("msg_data"));
			}
			else if (msg_name.equals("round")) {

				long start = System.currentTimeMillis();

				client.round(json.getJSONObject("msg_data"));

				RoundAction action = client.act();

				System.out.println("时间：" + (System.currentTimeMillis() - start) + "ms");

				Message am = new Message("action", action);
				String send = am.toString();
				p.send(send);
			}
			else if (msg_name.equals("leg_end")) {
				client.legEnd(json.getJSONObject("msg_data"));
			}
			else if (msg_name.equals("game_over")) {
				System.out.println("game_over");
				break;
			}
			else {
				System.out.println("unkown message name " + msg_name);
			}
		}
	}

}
