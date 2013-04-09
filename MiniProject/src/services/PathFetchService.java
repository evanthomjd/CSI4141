package services;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import communication.Direction;


import android.os.AsyncTask;

public class PathFetchService extends AsyncTask<String, Void, ArrayList<Direction> > {



	@Override
	protected ArrayList<Direction>  doInBackground(String... queryStrings) {
		NodeList lat, lng, steps;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		ArrayList<Direction> directionList = new ArrayList<Direction>();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(queryStrings[0]);
			
			NodeList latNodeList = document.getElementsByTagName("lat");       
			NodeList lngNodeList = document.getElementsByTagName("lng");
			NodeList instructions = document.getElementsByTagName("html_instructions");

			Direction direction = new Direction();
			lat = latNodeList.item(0).getChildNodes(); 
			lng = lngNodeList.item(0).getChildNodes();
			direction.setLat(Double.parseDouble(lat.item(0).getNodeValue()));
			direction.setLog(Double.parseDouble(lng.item(0).getNodeValue()));
			directionList.add(direction);
			//Create direction objects and give them their latitude and longitude values
			for(int i=1; i<latNodeList.getLength()-3; i++){
				if(i%2!=0){
					direction = new Direction();
					lat = latNodeList.item(i).getChildNodes(); 
					lng = lngNodeList.item(i).getChildNodes();
					direction.setLat(Double.parseDouble(lat.item(0).getNodeValue()));
					direction.setLog(Double.parseDouble(lng.item(0).getNodeValue()));
					directionList.add(direction);
				}              
			}



			String message, stripped;
			List<String> messageContent = new ArrayList<String>();
			Pattern pattern = Pattern.compile("<b>(.+?)</b>");
			for (int j = 0; j < instructions.getLength(); j++) {
				message ="";
				steps = instructions.item(j).getChildNodes();
				String step = steps.item(0).getNodeValue();


				Matcher matcher = pattern.matcher(step);
				while (matcher.find()) {
					stripped = matcher.group(1);
					if (stripped.contains("/")) {
						stripped = stripped.substring(0, stripped.indexOf('/'));
					}
					messageContent.add(stripped);
				}

				if(messageContent.size()==2){
					if(messageContent.get(0).toLowerCase().equals("left")||messageContent.get(0).toLowerCase().equals("right")){
						directionList.get(j).setDirection(messageContent.get(0));
						directionList.get(j).setStreet(messageContent.get(1));
					}else{
						directionList.get(j).setStreet(messageContent.get(1));
					}

				}else if(messageContent.size() == 1){
					directionList.get(j).setStreet(messageContent.get(0));

				}

				messageContent.clear();

			}
			return directionList;
			
			
			
		
		} catch (Exception e) {
			return null;
		}	
	
		
	}

}
