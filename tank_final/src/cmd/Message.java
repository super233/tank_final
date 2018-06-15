package cmd;

import net.sf.json.JSONObject;

public class Message {
	private String msg_name = "";
	private Object msg_data;

	public Message(String msg_name, Object msg_data) {
		this.msg_name = msg_name;
		this.msg_data = msg_data;
	}
	
	public String toString()
	{
		JSONObject json = JSONObject.fromObject(this);
		return json.toString();
	}

	public String getMsg_name() {
		return msg_name;
	}

	public void setMsg_name(String msg_name) {
		this.msg_name = msg_name;
	}

	public Object getMsg_data() {
		return msg_data;
	}

	public void setMsg_data(Object msg_data) {
		this.msg_data = msg_data;
	}

}
