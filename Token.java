/*	COP4620 - Project 4
 *	Stephen Repper
 */

public class Token {

	String token = null;
	String type = null;
		
	public Token()
	{
	}
		
	public Token(String tok, String typ)
	{
		token = tok;
		type = typ;
	}
		
	public String getToken()
	{
		return token;
	}
	
	public String getType()
	{
		return type;
	}
}