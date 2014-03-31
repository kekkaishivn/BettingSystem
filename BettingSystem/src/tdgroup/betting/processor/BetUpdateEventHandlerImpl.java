package tdgroup.betting.processor;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.*;
import javax.script.*;

import tdgroup.betting.util.GsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;

public class BetUpdateEventHandlerImpl implements BetUpdateEventHandler {
	
	static String data = "$M('odds-display').onUpdate(2,[104235,0,[[1,'Football',1],[2,'Basketball',0]],[[1,'03/28 [Today]',0],[2,'03/29 [Tomorrow]',0],[3,'03/30',0],[4,'03/31',0],[8,'All Dates',1]],[,,,,,[[23702401,,[1.86,2.04]],[23684198,,[1.83,2.07]],[23684202,,[2.5,,2.83]],[23688179,,[2.09,1.81]],[23683759,,[1.82,2.08]],[23683763,,[1.82,3.3,4.3]],[23683765,,[2.06,1.8]],[23683766,,[2.6,1.94,4.5]]],,,'Football',0],[,,[1408079],0],,0]);";
	private final static Pattern sbobetHandicapOddDisplayPattern = Pattern.compile("onUpdate\\((.+?),(.*)\\);");
	private final static Pattern sbobetHandicapTicketPattern = Pattern.compile("onUpdate\\((.*]),(.*)\\);");
	private final static Pattern ibetUnderOverNewLinePattern = Pattern.compile("(N(e|l|t)|Ins(e|l|t))\\[(.+?)\\]=(\\[.*\\]);");	
	private final static Pattern ibetUnderOverDeletePattern = Pattern.compile("Del(e|l|t)=(\\[.*\\]);");	
	private final static Pattern ibetUnderOverUpdatePattern = Pattern.compile("uO(e|l|t)\\[(.+?)\\]=(\\[.*\\]);");	
	private final static Pattern ibetBetProcessingPattern = Pattern.compile("Data=([^;]*);");
	
	@Override	
	public void onReceiveSbobetHandicapOddsDisplayUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		System.out.println(data);
		String jsonString = null;
		
		Matcher matcher = sbobetHandicapOddDisplayPattern.matcher(data);
		while (matcher.find())	{
			jsonString = matcher.group(2);
		}
		
		if (jsonString==null) 
				throw new UnsupportedResponseFormatException("Cannot match json string");
	
		System.out.println(jsonString);
		
		JsonElement jelement = new JsonParser().parse(jsonString);
		
		// Check the main array index for two different type of request
		int mainArrIndex = (jelement.getAsJsonArray().get(2).isJsonArray() ? 2 : 3);
		
		////////////////////// Sbobet Handicap MainInfo Array structure //////////////////////////////////////////////
		// 0-league-arrays : 1-match-arrays : 2-frame-arrays : 3-frame-info-arrays : 4-deleting-frameid-array : 
		// 5-bet-info-arrays : 6-deleting-bet-ids-array
		// [
		//    [
		//     [12060, 'C\u200Co\u200C\u200C\u200Cl\u200C\u200C\u200C\u200C\u200C\u200C\u200Co\u200C\u200C\u200C\u200C\u200C\u200Cm\u200C\u200C\u200C\u200C\u200Cb\u200C\u200C\u200C\u200C\u200C\u200Ci\u200Ca\u200C\u200C Liga Postobon', '', ''],
		//     [12677, 'Costa Rica Campeonato Primera Division', '', ''],
		//     [28339, 'Copa Inca', '', '']
		// ],
		// [
		//     [1408067, 1, 12060, 'D\u200Ce\u200C\u200C\u200Cp\u200C\u200C\u200C\u200C\u200C\u200C\u200Co\u200C\u200C\u200C\u200C\u200C\u200Cr\u200C\u200C\u200C\u200C\u200Ct\u200C\u200C\u200C\u200C\u200C\u200Ce\u200Cs\u200C\u200C Tolima', 'F\u200Co\u200C\u200C\u200Cr\u200C\u200C\u200C\u200C\u200C\u200C\u200Ct\u200C\u200C\u200C\u200C\u200C\u200Ca\u200C\u200C\u200C\u200C\u200Cl\u200C\u200C\u200C\u200C\u200C\u200Ce\u200Cz\u200C\u200Ca FC', '1.333', 10, '03/31/2014 06:30', 1, '', 2],
		//     [1408132, 1, 12677, 'CS Herediano', 'CS Cartagines', '1.364', 10, '03/31/2014 06:30', 1, '', 2],
		//     [1408143, 1, 28339, 'Universitario Deportes', 'Sport Huancayo', '1.338', 10, '03/31/2014 06:45', 1, '', 3]
		// ],
		// [
		//     [558286, 1408067, 0, 1, 1, 2],
		//     [558365, 1408132, 0, 2, 0, 2],
		//     [558377, 1408143, 0, 1, 2, 3]
		// ],
		// [
		//     [558286, 1, 2, 41, 45, 0, 0, 0, {
		//         5: 1
		//     }, {
		//         1: 45,
		//         2: 45,
		//         3: 15,
		//         4: 15
		//     }],
		//     [558365, 1, 2, 37, 45, 0, 0, 0, 0, {
		//         1: 45,
		//         2: 45,
		//         3: 15,
		//         4: 15
		//     }],
		//     [558377, 1, 2, 32, 45, 0, 0, 0, 0, {
		//         1: 45,
		//         2: 45,
		//         3: 15,
		//         4: 15
		//     }]
		// ], 
		// [558286], 
		// [
		//     [16735761, [558286, 1, 1, 2000.00, 0.25],
		//         [-0.41, 0.29]
		//     ],
		//     [16735763, [558286, 3, 1, 2000.00, 2.50],
		//         [-0.31, 0.17]
		//     ],
		//     [16736896, [558365, 1, 1, 1000.00, 0.25],
		//         [-0.56, 0.4]
		//     ],
		//     [16736898, [558365, 3, 1, 1000.00, 2.50],
		//         [-0.56, 0.38]
		//     ],
		//     [16737085, [558377, 1, 1, 2000.00, 0.25],
		//         [-0.77, 0.61]
		//     ],
		//     [16737087, [558377, 3, 1, 2000.00, 3.50],
		//         [-0.78, 0.6]
		//     ]
		// ], [16735763,16735761] , , 'Football', 0
		//],
		//[
		// [],
		// [],
		// [1408067, 1408132, 1408143], 0
		//]
		//////////////////////////////////////////////////////////////////////////////////////////////////
		if (!jelement.getAsJsonArray().get(mainArrIndex).isJsonNull())	{
			JsonArray mainArr = jelement.getAsJsonArray().get(mainArrIndex).getAsJsonArray();
			
			// Extract information from league arrays
			if (!mainArr.get(0).isJsonNull())	{
				JsonArray leagueArrs = mainArr.get(0).getAsJsonArray();
				Iterator<JsonElement> leagueIter = leagueArrs.iterator();
				while (leagueIter.hasNext())	{
					handleLeagueElement((JsonArray)leagueIter.next());
				}
			}
			
			// Extract information from match arrays
			if (!mainArr.get(1).isJsonNull())	{
				JsonArray matchArrs = mainArr.get(1).getAsJsonArray();
				Iterator<JsonElement> matchIter = matchArrs.iterator();
				while (matchIter.hasNext())	{
					handleMatchElement((JsonArray)matchIter.next());
				}		
			}
			
			// Extract information from frame arrays
			if (!mainArr.get(2).isJsonNull())	{
				JsonArray frameArrs = mainArr.get(2).getAsJsonArray();
				Iterator<JsonElement> frameIter = frameArrs.iterator();
				while (frameIter.hasNext())	{
					handleFrameElement((JsonArray)frameIter.next());
				}		
			}
			
			// Extract information from bet arrays
			if (!mainArr.get(5).isJsonNull())	{
				JsonArray betArrs = mainArr.get(5).getAsJsonArray();
				Iterator<JsonElement> betIter = betArrs.iterator();
				while (betIter.hasNext())	{
					handleBetElement((JsonArray)betIter.next());
				}			
			}
			
			// Extract information from deleting bet array
			if (!mainArr.get(6).isJsonNull())	{
				JsonArray betDelArr = mainArr.get(6).getAsJsonArray();
				Iterator<JsonElement> betIter = betDelArr.iterator();
				System.out.print("Delete following bet: ");
				while (betIter.hasNext())	{
					System.out.print(betIter.next().getAsString() + " ");
				}			
				System.out.println("");
			}
		}
		//GsonUtil.printJsonNode(arr1);
	}
	
	////////////////////// Sbobet FrameElement structure //////////////////////////////////////////////
	// 0-frame-id : 1-match-id : unknown : 3-home-score : 4-away-score : 5-total-bets (included some hidden bets)
	// [558256, 1408038, 0, 0, 2, 7]
	//////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleFrameElement(JsonArray frameInfo) {
		// TODO Auto-generated method stub
		int frameid = frameInfo.get(0).getAsInt();
		int matchid = frameInfo.get(1).getAsInt();
		System.out.println(frameid);
		System.out.println(matchid);
	}
	
	///////////////////// Sbobet LeagueElement structure ////
	// 0-League-id : 1-league-name : 2-unknown : 3-unknown
	// [12060, 'Colombia Liga Postobon', '', '']
	/////////////////////////////////////////////////////////
	private void handleLeagueElement(JsonArray leagueInfo)	{
		System.out.println(leagueInfo.get(0).getAsInt());
		System.out.println(leagueInfo.get(1).getAsString());
	}
	
	////////////////////// Sbobet MatchElement structure /////////////////////////////////////////////////////////////////////
	// 0-match-id : unknown : 2-league-id : 3-home-team : 4-away-team : unknown : unknown : 7-timestamp : unknown  : unknown : 10-total bets 
	// [1408695, 1, 12060, 'Universidad Autonoma (n)', 'Junior Barranquilla', '1.069', 10, '03/28/2014 09:00', 1, '', 6]
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleMatchElement(JsonArray matchInfo)	{
		System.out.println(matchInfo.get(0).getAsInt());
		System.out.println(matchInfo.get(2).getAsInt());
		System.out.println(matchInfo.get(3).getAsString());
		System.out.println(matchInfo.get(4).getAsString());
	}
	
	////////////////////// Sbobet BetElement structure /////////////////////////////////////////////////////////////////////
	// 0-Bet-id : 1-arr-[frame-id : column : row : unknown : bet-kind-value] : 2-[over-odd-val : under-odd-val] (Over-under)
	//																																				 2-[home-odd-val : away-odd-val] (handicap)
	// [25238074, [558709, 1, 1, 2000.00, 0.00], [-0.84, 0.76]
	// Column decide where the bet would be set in the web interface, and infer bet-kind
	// Column: 1-Handicap-full-time, 3-Over-under-full-time, 7-First-half-Handicap, 9-First-half-Over-under
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleBetElement(JsonArray betInfo)	{
		JsonArray oddValues = betInfo.get(2).getAsJsonArray();
		
		if (oddValues.size()>2)
			return;
		
		float over = oddValues.get(0).getAsFloat();
		float under = oddValues.get(1).getAsFloat();
		
		System.out.println(betInfo.get(0).getAsInt());
		if (!betInfo.get(1).isJsonNull())	{
			System.out.println(betInfo.get(1).getAsJsonArray().get(0).getAsInt());
			System.out.println(betInfo.get(1).getAsJsonArray().get(4).getAsFloat());
		}
		
		System.out.println(betInfo.get(2).getAsJsonArray().get(0).getAsFloat());
		System.out.println(betInfo.get(2).getAsJsonArray().get(1).getAsFloat());
	}

	@Override
	public void onReceiveSbobetHandicapTicketUpdate(String data) throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		data = data.replace('\n', ' ');
		System.out.println(data);
		String jsonString = null;
		
		Matcher matcher = sbobetHandicapTicketPattern.matcher(data);
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
		// odds-value
		System.out.println(ticketInfo.get(9).getAsFloat());
		// bet-id
		System.out.println(ticketInfo.get(25).getAsInt());
		// 'h' = over, 'a' = under
		System.out.println(ticketInfo.get(27).getAsCharacter());
		// frame-id
		System.out.println(ticketInfo.get(28).getAsInt());
	}

/*	@Override
	public void onReceiveIbetUnderOverUpdate(String data)
			throws UnsupportedResponseFormatException	{
		
		long before = System.currentTimeMillis();
		data = "//" + data;
    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();
    // create JavaScript engine
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    // evaluate JavaScript code from given file - specified by first argument
    try {
			engine.eval(data);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    System.out.println(System.currentTimeMillis()-before);
	}*/
		
	@Override
	public void onReceiveIbetOddsValueUpdate(String data)
			throws UnsupportedResponseFormatException {
		// TODO Auto-generated method stub
		//System.out.println(data);
		// Handling delete line part
		// Deal with Dell structure in javascript file
		JsonArray dellArrays = extractJsonArray(data, ibetUnderOverDeletePattern);
		{
			Iterator iter = dellArrays.iterator();
			while (iter.hasNext())	{
				JsonElement child = (JsonElement)iter.next();
				if (!child.isJsonNull())
					handleIbetDell(child.getAsJsonArray());				
			}
		}
		// End of handling init
		
		// Handling init new line part
		// Deal with Nl structure in javascript file
		JsonArray nlArrays = extractJsonArray(data, ibetUnderOverNewLinePattern);
		{
			System.out.println("Num of new line: " + (nlArrays.size()-1));
			Iterator iter = nlArrays.iterator();
			while (iter.hasNext())	{
				JsonElement child = (JsonElement)iter.next();
				if (child.isJsonArray())
					handleIbetNewLineElement(child.getAsJsonArray());
			}
		}
		// End of handling init	
		
		// Handling update line part
		// Deal with uOl structure in javascript file
		JsonArray uOlArrays = extractJsonArray(data, ibetUnderOverUpdatePattern);	
		{
			System.out.println("Num of update bet: " + (uOlArrays.size()-1));
			Iterator iter = uOlArrays.iterator();
			while (iter.hasNext())	{
				JsonElement child = (JsonElement)iter.next();
				if (child.isJsonArray())
					handleIbetUOl(child.getAsJsonArray());
			}
		} 
	// End of handling update uOl
	}
	
	private JsonArray extractJsonArray(String data, Pattern pattern) throws UnsupportedResponseFormatException	{
		StringBuilder jsonString = new StringBuilder();
		jsonString.append('[');
		Matcher matcher = pattern.matcher(data);
		while (matcher.find())	{
			jsonString.append(matcher.group(matcher.groupCount()));
			jsonString.append(',');
		}		
		jsonString.append(']');		

		JsonElement jelement = new JsonParser().parse(jsonString.toString());
		if (jelement.isJsonArray())	{
			return jelement.getAsJsonArray();
		} else {
			throw new UnsupportedResponseFormatException("Fail to parse nl arrays");
		}
	}
	
	
	static final int[] matchInfoIndex = {5, 6, 7};
	static final int[] handicapFullTimeIndex = {25, 26, 27, 28, 29};
	static final int[] handicapHalfTimeIndex = {38, 39, 40, 41, 42};
	static final int[] overunderFullTimeIndex = {30, 31, 32, 33};
	static final int[] overunderHalfTimeIndex = {43, 44, 45, 46};
	static final int[] europeanFullTimeIndex = {34, 35, 36, 37};
	static final int[] europeanHalfTimeIndex = {47, 48, 49, 50};
	private void handleIbetNewLineElement(JsonArray ibetNl)	{
		
		// Data structure index 
		// 0: match-full-id, 1: match-id, 2,3,4: unknown, 5: League-name, 
		// 6: home-team, 7: away-tream, 8: timestamp, 9, 10, 11: unknown 12: match-time, 
		// 13,14, 15, 16, 17, 18, 19 ,20, 21, 22 ,23, 24: unknown, 
		// 25: bet-id, 26: bet-value, 27: home-odd, 28: away-odd, 29: home or away ('h' or 'a')
		// 30: bet-id, 31: bet-value, 32: over-odd, 33: under-odd, 
		// 34: bet-id, 35, 36, 37: home-away-draw-odd, 
		// 38,39,40,41,42: handicap bet of full match, 
		// 43, 44, 45, 46: over-under-fullmatch, 
		// 47-48-49-50: european full-match
		String id = ibetNl.get(0).getAsString();
		String[] matchInfo = extractString(ibetNl, matchInfoIndex);
		String[] handicapHalfMatch = extractString(ibetNl, handicapHalfTimeIndex);
		String[] handicapFullMatch = extractString(ibetNl, handicapFullTimeIndex);
		String[] overunderHalfMatch = extractString(ibetNl, handicapHalfTimeIndex);
		String[] overunderFullMatch = extractString(ibetNl, handicapFullTimeIndex);
		String[] europeanHalfMatch = extractString(ibetNl, europeanFullTimeIndex);
		String[] europeanFullMatch = extractString(ibetNl, europeanHalfTimeIndex);
		
	}
	
	private String[] extractString(JsonArray array, final int[] indexes)	{
		String[] result = new String[indexes.length];
		for (int i=0; i<result.length; i++)	{
			result[i] = array.get(indexes[i]).getAsString();
		}
		return result;
	}
	
	static final int[] betInfoUpdateIndexes = {0, 2, 3, 4, 5};
	private void handleIbetUOl(JsonArray uOl)	{
		// Data structure index 
		// 0: match-full-id, 1: column, 2: bet-id,
		// column: 5 - european-full-time, 15 - european-half-time
		// 				 1 - handicap-full-time, 7 - handicap-half-time
		// 				 3 - over-under-full-time, 8 - over-under-half-time
		// European: 3 - home-odd-value, 4 - away-odd-value, 5 - draw-odd
		// Asian: 3 - bet-value, 4 - over-odd, 5 -under-odd
		String betType = "Unknown";
		switch(uOl.get(1).getAsInt())	{
		case 5:
		case 15:
			betType = "European";
			break;
		case 1:
		case 7:
			betType = "Handicap";
			break;
		case 3:
		case 8:
			betType = "Over-Under";
			break;
		default:
		}
		String[] info = extractString(uOl, betInfoUpdateIndexes);
	}
	
	private void handleIbetDell(JsonArray Dell)	{
		Iterator iter = Dell.iterator();
		System.out.print("Delete line: ");
		while (iter.hasNext())	{
			JsonElement matchId = (JsonElement)iter.next();
			System.out.print(matchId.getAsString() + " ");
		}
		System.out.println("");
	}

	@Override
	public void onReceiveIbetBetProcessing(String data)
			throws UnsupportedResponseFormatException {

			String jsonString = null;
			Matcher matcher = ibetBetProcessingPattern.matcher(data);
			while (matcher.find())	{
				jsonString = matcher.group(1);
			}		
	
			HashMap<String,String> betInfo = new Gson().fromJson(jsonString, HashMap.class);
			System.out.println(betInfo.get("matchid"));
			System.out.println(betInfo.get("lblBetKind"));
			System.out.println(betInfo.get("lblBetKindValue"));
			System.out.println(betInfo.get("lblChoiceValue"));
			System.out.println(betInfo.get("lblOddsValue"));
	}

}
