package services;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;


import android.os.AsyncTask;

public class PathFetchService extends AsyncTask<String, Void, Document > {



	@Override
	protected Document  doInBackground(String... queryStrings) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(queryStrings[0]);
			return document;
		
		} catch (Exception e) {
			return null;
		}	
	
		
	}

}
