package com.github.foxnic.generatorV2.config;

import com.github.foxnic.generatorV2.builder.PoClassFile;
import com.github.foxnic.generatorV2.builder.VOClassFile;

public class MduCtx {
	
	private GlobalSettings settings;
	private PoClassFile poClassFile;
	private VOClassFile voClassFile;

	public MduCtx(GlobalSettings settings) {
		this.settings=settings;
	}
	
	public PoClassFile getPoClassFile() {
		return poClassFile;
	}
	public void setPoClassFile(PoClassFile poClassFile) {
		this.poClassFile = poClassFile;
	}
	public VOClassFile getVoClassFile() {
		return voClassFile;
	}
	public void setVoClassFile(VOClassFile voClassFile) {
		this.voClassFile = voClassFile;
	}

	public GlobalSettings getSettings() {
		return settings;
	}

 
	
	
}
