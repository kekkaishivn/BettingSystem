package tdgroup.betting.processor;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.*;

import tdgroup.betting.util.GsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class BetUpdateEventHandlerImpl implements BetUpdateEventHandler {
	
	static String data = "$M('odds-display').onUpdate(2,[104235,0,[[1,'Football',1],[2,'Basketball',0]],[[1,'03/28 [Today]',0],[2,'03/29 [Tomorrow]',0],[3,'03/30',0],[4,'03/31',0],[8,'All Dates',1]],[,,,,,[[23702401,,[1.86,2.04]],[23684198,,[1.83,2.07]],[23684202,,[2.5,,2.83]],[23688179,,[2.09,1.81]],[23683759,,[1.82,2.08]],[23683763,,[1.82,3.3,4.3]],[23683765,,[2.06,1.8]],[23683766,,[2.6,1.94,4.5]]],,,'Football',0],[,,[1408079],0],,0]);";
	@Override	
	public void onReceiveSbobetOddDisplayUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		System.out.println(data);
		Pattern pattern = Pattern.compile("onUpdate\\((.+?),(.*)\\);");
		String jsonString = null;
		
		Matcher matcher = pattern.matcher(data);
		while (matcher.find())	{
			jsonString = matcher.group(2);
		}
		
		if (jsonString==null) 
				throw new UnsupportedResponseFormatException("Cannot match json string");
	
		System.out.println(jsonString);
		
		JsonElement jelement = new JsonParser().parse(jsonString);
		
		// Check the main array index for two different type of request
		int mainArrIndex = (jelement.getAsJsonArray().get(2).isJsonArray() ? 2 : 3);
		
		if (!jelement.getAsJsonArray().get(mainArrIndex).isJsonNull())	{
			JsonArray arr1 = jelement.getAsJsonArray().get(mainArrIndex).getAsJsonArray();
			//GsonUtil.printJsonNode(arr1);
			if (!arr1.get(0).isJsonNull())	{
				JsonArray leagueArrs = arr1.get(0).getAsJsonArray();
				Iterator<JsonElement> leagueIter = leagueArrs.iterator();
				while (leagueIter.hasNext())	{
					handleLeagueElement((JsonArray)leagueIter.next());
				}
			}
			
			if (!arr1.get(1).isJsonNull())	{
				JsonArray matchArrs = arr1.get(1).getAsJsonArray();
				Iterator<JsonElement> matchIter = matchArrs.iterator();
				while (matchIter.hasNext())	{
					handleMatchElement((JsonArray)matchIter.next());
				}		
			}
			
			if (!arr1.get(2).isJsonNull())	{
				JsonArray frameArrs = arr1.get(2).getAsJsonArray();
				Iterator<JsonElement> frameIter = frameArrs.iterator();
				while (frameIter.hasNext())	{
					handleFrameElement((JsonArray)frameIter.next());
				}		
			}
			
			if (!arr1.get(5).isJsonNull())	{
				JsonArray betArrs = arr1.get(5).getAsJsonArray();
				Iterator<JsonElement> betIter = betArrs.iterator();
				while (betIter.hasNext())	{
					handleBetElement((JsonArray)betIter.next());
				}			
			}
		}
		//GsonUtil.printJsonNode(arr1);
	}
	
	private void handleFrameElement(JsonArray frameInfo) {
		// TODO Auto-generated method stub
		int frameid = frameInfo.get(0).getAsInt();
		int matchid = frameInfo.get(1).getAsInt();
		System.out.println(frameid);
		System.out.println(matchid);
	}

	private void handleLeagueElement(JsonArray leagueInfo)	{
		System.out.println(leagueInfo.get(0).getAsInt());
		System.out.println(leagueInfo.get(1).getAsString());
	}
	
	private void handleMatchElement(JsonArray matchInfo)	{
		System.out.println(matchInfo.get(0).getAsInt());
		System.out.println(matchInfo.get(2).getAsInt());
		System.out.println(matchInfo.get(3).getAsString());
		System.out.println(matchInfo.get(4).getAsString());
	}
	
	private void handleBetElement(JsonArray betInfo)	{
		JsonArray oddValues = betInfo.get(2).getAsJsonArray();
		
		if (oddValues.size()>2)
			return;
		
		float over = oddValues.get(0).getAsFloat();
		float under = oddValues.get(1).getAsFloat();
		
		// Filter european bet
//		if (over>1 || under>1)
//			return;
		
		System.out.println(betInfo.get(0).getAsInt());
		if (!betInfo.get(1).isJsonNull())	{
			System.out.println(betInfo.get(1).getAsJsonArray().get(0).getAsInt());
			System.out.println(betInfo.get(1).getAsJsonArray().get(4).getAsFloat());
		}
		
		System.out.println(betInfo.get(2).getAsJsonArray().get(0).getAsFloat());
		System.out.println(betInfo.get(2).getAsJsonArray().get(1).getAsFloat());
	}

	@Override
	public void onReceiveSbobetTicketUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		System.out.println(data);
		Pattern pattern = Pattern.compile("onUpdate\\((.*]),(.*)\\);");
		String jsonString = null;
		
		Matcher matcher = pattern.matcher(data);
		while (matcher.find())	{
			jsonString = matcher.group(1);
		}
		
		if (jsonString==null) 
				throw new UnsupportedResponseFormatException("Cannot match json string");
	
		System.out.println(jsonString);
		
		JsonElement jelement = new JsonParser().parse(jsonString);		
		handleTicketElement(jelement.getAsJsonArray());
	}
	
	private void handleTicketElement(JsonArray ticketInfo)	{
		if (ticketInfo.get(9).isJsonNull())	{
			System.out.println("null ticket");
			return;
		}
		System.out.println(ticketInfo.get(9).getAsFloat());
		System.out.println(ticketInfo.get(25).getAsInt());
		System.out.println(ticketInfo.get(27).getAsCharacter());
		System.out.println(ticketInfo.get(28).getAsInt());
	}

	@Override
	public void onReceiveSbobetSportMarketUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		
	}

}
