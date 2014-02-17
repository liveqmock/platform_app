package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.List;

public class EpgResponse {
	public List<EpgContent> contents = new ArrayList<EpgContent>();
	
	public EpgResponse(List<EpgContent> contents) {
		this.contents = contents;
	}

}
