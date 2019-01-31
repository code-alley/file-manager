package kr.co.inslab.codealley.filemanager.app.utils;

public class Response {

	private boolean success;
	private String key;
	private String value;
	private final String SUCCESS = "success";
	private final String FAIL = "fail";
	
	public Response(boolean success) {
		this.success = success;
	}
	
	public Response(String key, String value) {
		this.success = true;
		this.key = key;
		this.value = value;
	}
	
	public Response(boolean success, String msg) {
		this.success = success;
		this.key = "message";
		this.value = msg;
	}

	@Override
	public String toString() {
		String result = success ? SUCCESS : FAIL;
		String response = String.format("{\"result\": \"%s\"}", result);
		if(key != null) {
			if(value == null) {
				value = "{}";
			}
			
			response = String.format("{\"result\": \"%s\", \"%s\": \"%s\"}", result, key, value);
		}
		
		return response;
	}
}
