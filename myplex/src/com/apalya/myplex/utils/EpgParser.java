package com.apalya.myplex.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class EpgParser 
{
	private static final String ns = null;

	public InputStream fetchInputStream(String urlString) {
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			return conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<EpgContent> parse(InputStream inputStream){
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inputStream, null);
			parser.nextTag();
			return readFeed(parser);
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private List<EpgContent> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		
	List<EpgContent> entries = new ArrayList<EpgContent>();
	parser.require(XmlPullParser.START_TAG, ns, "Fragments");
	while (parser.next() != XmlPullParser.END_TAG) {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			continue;
		}
		String name = parser.getName();
		// Starts by looking for the EpgContent tag
		if (name.equals("Content")) {
			entries.add(readEntry(parser));
		} else {
			skip(parser);
		}
	}  
	return entries;
	}
	
	private EpgContent readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "Content");
	    String title = null;
	    String summary = null;
	    String link = null;
	    String assetType = null;
	    String assetUrl = null;
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("Name")) {
	            title = readName(parser);
	        } else if (name.equals("StartTime")) {
	            summary = readStartTime(parser);
	        } else if (name.equals("EndTime")) {
	            link = readEndTime(parser);
	        }else if(name.equals("assetType")){
	        	assetType = readAssetType(parser);
	        }else if(name.equals("assetUrl")){
	        	assetUrl = readAssetUrl(parser);
	        }else {
	            skip(parser);
	        }
	    }
	    return new EpgContent(title, summary, link,assetType,assetUrl);
	}

	private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Name");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "Name");
		return title;
	}

	private String readStartTime(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "StartTime");
		String StartTime = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "StartTime");
		return StartTime;
	}
	
	private String readEndTime(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "EndTime");
		String EndTime = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "EndTime");
		return EndTime;
	}
	private String readAssetType(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "assetType");
		String EndTime = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "assetType");
		return EndTime;
	}
	private String readAssetUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "assetUrl");
		String EndTime = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "assetUrl");
		return EndTime;
	}	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	
}
