/*	COP4620 - Project 4
 *	Stephen Repper
 */

import java.util.Vector;

public class Output {
	Vector<String[]> output;
	int index = -1;
	
	public Output()
	{
		output = new Vector<String[]>();
	}
	
	public void add()
	{
		index++;
		String[] s = {"", "", "", "", ""};
		s[0] = Integer.toString(index + 1);
		output.add(s);
	}
	
	public void add(String[] s)
	{
		index++;
		s[0] = Integer.toString(index + 1);
		output.add(s);
	}
	
	public String[] get(int index)
	{
		return output.get(index);
	}
	
	public String[] getData()
	{
		return output.get(output.size() - 1);
		
	}
	
	public int getIndex()
	{
		return index + 1;
	}
	
	public String getOperation()
	{
		return output.get(index)[1];
	}
	
	public String getOp1()
	{
		return output.get(index)[2];
	}
	
	public String getOp2()
	{
		return output.get(index)[3];
	}
	
	public String getResult()
	{
		return output.get(index)[4];
	}
	
	public void print()
	{
		String out = "";
		for(int i = 0; i < output.size(); i++)
		{
			for(int j = 0; j < 4; j++)
			{
				String s = output.get(i)[j];
				for(; s.length() < 10; s += " ");
				out += s;
			}
			out += output.get(i)[4];
			System.out.println(out);
			out = "";
		}
	}
	
	public void setIndex(String index)
	{
		output.get(this.index)[0] = index;
	}
	
	public void setOperation(String op)
	{
		output.get(index)[1] = op;
	}
	
	public void setOp1(String op1)
	{
		output.get(index)[2] = op1;
	}
	
	public void setOp2(String op2)
	{
		output.get(index)[3] = op2;
	}
	
	public void setResult(String result)
	{
		output.get(index)[4] = result;
	}
	
	public void setResult(int i, String result)
	{
		output.get(i)[4] = result;
	}
	
	public int size()
	{
		return output.size();
	}
}
