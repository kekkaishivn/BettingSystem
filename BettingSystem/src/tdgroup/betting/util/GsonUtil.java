package tdgroup.betting.util;

import java.util.Iterator;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class GsonUtil {

	public static void printJsonNode(JsonElement node)	{
		
		if (node.isJsonArray())	{
			Iterator<JsonElement> iter = node.getAsJsonArray().iterator();
			while (iter.hasNext())	{
				JsonElement element = iter.next();
				if (element.isJsonArray())	{
					System.out.println("Array");
					printJsonNode(element);
					System.out.println("Endarray");					
				} else {
					printJsonNode(element);
				}
			}			
		} else {
				if (node.isJsonNull())	{
					System.out.println("null");
				} if (node.isJsonPrimitive())	{
					JsonPrimitive primitive = node.getAsJsonPrimitive();
					if (primitive.isBoolean())	{
						System.out.println(primitive.getAsBoolean());
					} else if (primitive.isNumber())	{
						System.out.println(primitive.getAsFloat());
					} else if (primitive.isString())	{
						System.out.println(primitive.getAsString());
					}
			}
		}

	}

}
