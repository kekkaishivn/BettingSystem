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
	
	final static String data = "$M('odds-display').onUpdate(1, [37477, 0, 0, [, , , , , [[17774065, , [0.78, -0.86]], [17740958, , [0.94, 0.96]], [17740959, , [-0.95, 0.85]], [17755448, , [0.92, 0.9]] ], , , 'Football', 0], [, , [1405125, 1404074, 1401427, 1404276, 1404275, 1403112], 0], , , 0 ]);";
			
	@Override	
	public void onReceiveSbobetOddDisplayUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
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
		
		if (!jelement.getAsJsonArray().get(3).isJsonNull())	{
			JsonArray arr1 = jelement.getAsJsonArray().get(3).getAsJsonArray();
			
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
	
	protected void handleLeagueElement(JsonArray leagueInfo)	{
		//System.out.println(leagueInfo.get(0).getAsInt());
		//System.out.println(leagueInfo.get(1).getAsString());
	}
	
	protected void handleMatchElement(JsonArray matchInfo)	{
		//System.out.println(matchInfo.get(0).getAsInt());
		//System.out.println(matchInfo.get(2).getAsInt());
		//System.out.println(matchInfo.get(3).getAsString());
		//System.out.println(matchInfo.get(4).getAsString());
	}
	
	protected void handleBetElement(JsonArray betInfo)	{
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
		
	}

	@Override
	public void onReceiveSbobetSportMarketUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		
	}

}
