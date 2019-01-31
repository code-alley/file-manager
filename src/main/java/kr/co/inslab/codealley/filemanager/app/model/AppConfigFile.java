package kr.co.inslab.codealley.filemanager.app.model;

import kr.co.inslab.codealley.filemanager.app.utils.ModelUtils;

public class AppConfigFile {

	private String name = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ModelUtils.toString(this);
	}
}
