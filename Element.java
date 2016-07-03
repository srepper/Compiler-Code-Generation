/*	COP4620 - Project 4
 *	Stephen Repper
 */

public class Element
{
	private String[] data = {"", "", "", "", ""};
	private String var;
	private String comp;
	private int breakIndex;
	private final String[] breakType = {
			"BRGT", "BRGTEQ", "BRLTEQ", "BRLT", "BRNEQ", "BREQ"};
	
	public Element(){}
	
	public Element(String[] s)
	{
		data = s;
	}
		
	public void setComp(String s)
	{
		comp = s;
		breakIndex = (s.contentEquals("<=") ? 0 :
			(s.contentEquals("<")? 1 : 
				(s.contentEquals(">")? 2 :
					(s.contentEquals(">=")? 3 :
						(s.contentEquals("==")? 4 :
							(s.contentEquals("!=")? 5 : null))))));
	}
	public void setOperation(String s)
	{
		data[1] = s;
	}
	public void setOp1(String s)
	{
		data[2] = s;
	}
	
	public void setOp2(String s)
	{
		data[3] = s;
	}
	
	public void setResult(String s)
	{
		data[4] = s;
	}
	
	public void setVar(String s)
	{
		var = s;
	}
	
	public String[] getArray()
	{
		return data;
	}
	
	public String getBreak()
	{
		return breakType[breakIndex];
	}
	public String getData(int i)
	{
		return data[i];
	}
	
	public String getOp1()
	{
		return data[2];
	}
	
	public String getOp2()
	{
		return data[3];
	}
	
	public String getResult()
	{
		return data[4];
	}
	
	public String getVar()
	{
		return var;
	}
}
