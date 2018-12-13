package entity;

import java.io.Serializable;

public class Result implements Serializable {
	// 状态:是否成功
	private boolean success;
	// 相关信息
	private String msg;

	public Result(boolean success, String msg) {
		super();
		this.success = success;
		this.msg = msg;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
