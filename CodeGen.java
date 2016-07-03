/*	COP4620 - Project 4
 *	Stephen Repper
 */


import java.io.File;
import java.io.FileNotFoundException;

public class CodeGen
{
	
	LexicalAnalyzer lex;
	Token nextToken;
	File file;
	Output output;
	int tempIndex;
	enum Relop {LEQ, LT, GT, GEQ, EQ, NEQ};
	

	public static void main(String[] args)
	{
		try
		{
			CodeGen cg = new CodeGen(args[0]);
			cg.run();				
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("No data file present.");
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Scanner failed to read file.");
		}
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	/*	Constructor opens file, sends it to LexicalAnalyzer, closes the
	 * 	local file, and calls for the first token from the file.
	 */
	public CodeGen(String arg) throws Exception
	{
		file = new File(arg);
		lex = new LexicalAnalyzer(file);
		file = null;
		nextToken = lex.getNextToken();
		output = new Output();
	}
	
	/*	Run calls the first grammar rule.  If no errors occur during the parse,
	 * 	program() will complete, and success will be printed to screen.
	 */
	public void run()
	{
		program();
		output.print();
	}
	
	/*	Accept current token by calling getNextToken() from LexicalAnalyzer
	 */
	private void accept()
	{
		nextToken = lex.getNextToken();
	}
	
	/*	All grammar functions are merely implementations of
	 * 	their respective grammar rules. 
	 */
	private void program()
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
			decList();
		else
			parseFail();
	}
	
	private void decList()
	{
		if(nextToken.getToken().contentEquals("int") ||
			nextToken.getToken().contentEquals("float") ||
			nextToken.getToken().contentEquals("void"))
		{
			dec();
			decListTwo();
		}
		else
			parseFail();
	}
	
	private void decListTwo()
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
		{
			dec();
			decListTwo();
		}
		else if(!nextToken.getToken().contentEquals("$"))
			parseFail();
	}
	
	private void dec()
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
		{
			output.add();
			typeSpec();
			if(nextToken.getType().contentEquals("ID"))
			{
				output.setOp1(nextToken.getToken());
				accept();
			}
			else
				parseFail();
			decTwo();
		}
	}
	
	private void decTwo()
	{
		if(nextToken.getToken().contentEquals("("))
			funDec();
		else if(nextToken.getToken().contentEquals("[") ||
				nextToken.getToken().contentEquals(";"))
			varDec();
		else
			parseFail();
	}
	
	private void typeSpec()
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
		{
			output.setOperation(nextToken.getToken());
			accept();
		}
	}
	
	private void varDec()
	{
		if(output.getOperation().contentEquals("void"))
		{
			System.out.println("Variable cannot be void type!");
			System.exit(1);
		}
		
		output.setResult(output.getOp1());
		output.setOperation("alloc");
		
		if(nextToken.getToken().contentEquals("["))
		{
			output.setResult(output.getResult() + "[");
			accept();
			if(nextToken.getType().contentEquals("NUM"))
			{
				output.setOp1(Integer.toString(4 * 
						Integer.parseInt(nextToken.getToken())));
				accept();
				if(nextToken.getToken().contentEquals("]"))
				{
					output.setResult(output.getResult() + "]");
					accept();
					if(nextToken.getToken().contentEquals(";"))
						accept();
					else
						parseFail();
				}
				else
					parseFail();
			}
			else
				parseFail();
		}
		else if(nextToken.getToken().contentEquals(";"))
		{
			output.setOp1("4");
			accept();			
		}
		else
			parseFail();
	}
	
	private void funDec()
	{
		int paramIndex  = output.getIndex() - 1;
		output.setOp2(output.getOperation());
		output.setOperation("func");
			
		Element e = new Element(output.getData());
		
		if(nextToken.getToken().contentEquals("("))
		{
			accept();
			int numParams = params();
			if(nextToken.getToken().contentEquals(")"))
			{
				accept();
				cmpndStmt();
				output.add();
				output.setOperation("end");
				output.setOp1("func");
				output.setOp2(e.getOp1());
				output.setResult(paramIndex, Integer.toString(numParams));
			}
			else
				parseFail();
		}
		else
			parseFail();
	}
	
	private int params()
	{
		int numParams = 0;
		if(nextToken.getToken().contentEquals("int") ||
			nextToken.getToken().contentEquals("float"))
		{
			output.add();
			output.setOperation("alloc");
			output.setOp1("4");
			accept();
			numParams = paramList(numParams);
		}
		else if(nextToken.getToken().contentEquals("void"))
		{
			accept();
			voidParam();
		}
		else
			parseFail();
		
		return numParams;
	}
	
	private int paramList(int numParams)
	{
		if(nextToken.getType().contentEquals("ID"))
		{
			output.setResult(nextToken.getToken());
			numParams++;
			accept();
			paramPost();
			numParams = pListTwo(numParams);
		}
		else
			parseFail();
		
		return numParams;
	}
	
	private int pListTwo(int numParams)
	{
		if(nextToken.getToken().contentEquals(","))
		{
			accept();
			numParams = param(numParams);
			numParams = pListTwo(numParams);
		}
		else if(!nextToken.getToken().contentEquals(")"))
			parseFail();
		
		return numParams;
	}
	
	private int param(int numParams)
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
		{
			output.add();
			typeSpec();
			output.setOperation("alloc");
			output.setOp1("4");
			if(nextToken.getType().contentEquals("ID"))
			{
				output.setResult(nextToken.getToken());
				numParams++;
				accept();
			}
			else
				parseFail();
			paramPost();
		}
		else
			parseFail();
		
		return numParams;
	}
	
	private void paramPost()
	{
		if(nextToken.getToken().contentEquals("["))
		{
			output.setResult(output.getResult() + "[");
			accept();
			if(nextToken.getToken().contentEquals("]"))
			{
				output.setResult(output.getResult() + "]");
				accept();
			}
			else
				parseFail();
		}
		else if(!(nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals(",")))
			parseFail();
	}
	
	private void voidParam()
	{
		if(nextToken.getType().contentEquals("ID"))
		{
			accept();
			paramPost();
			pListTwo(0);
		}
		else if(!nextToken.getToken().contentEquals(")"))
			parseFail();
	}
	
	private void cmpndStmt()
	{
		if(nextToken.getToken().contentEquals("{"))
		{
			accept();
			localDecs();
			stmtList();
			if(nextToken.getToken().contentEquals("}"))
				accept();
			else
				parseFail();
			
		}
		else
			parseFail();
	}
	
	private void localDecs()
	{
		if(nextToken.getToken().contentEquals("int") ||
				nextToken.getToken().contentEquals("float") ||
				nextToken.getToken().contentEquals("void"))
		{
			output.add();
			typeSpec();
			if(nextToken.getType().contentEquals("ID"))
			{
				output.setOp1(nextToken.getToken());
				accept();
			}
			else
				parseFail();
			varDec();
			localDecs();
		}
		else if(nextToken.getToken().contentEquals("if") ||
				nextToken.getToken().contentEquals("while") ||
				nextToken.getToken().contentEquals("return") ||
				nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("(") ||
				nextToken.getToken().contentEquals("{") ||
				nextToken.getToken().contentEquals("}") ||
				nextToken.getToken().contentEquals(";"))
			;
		else
			parseFail();
	}
	
	private void stmtList()
	{
		if(nextToken.getToken().contentEquals("if") ||
				nextToken.getToken().contentEquals("while") ||
				nextToken.getToken().contentEquals("return") ||
				nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("(") ||
				nextToken.getToken().contentEquals("{") ||
				nextToken.getToken().contentEquals(";"))
		{
			statement();
			stmtList();
		}
		else if(!nextToken.getToken().contentEquals("}"))
			parseFail();
	}
	
	private void statement()
	{
		if(nextToken.getToken().contentEquals("if"))
			selStmt();
		else if(nextToken.getToken().contentEquals("while"))
			iterStmt();
		else if(nextToken.getToken().contentEquals("return"))
			returnStmt();
		else if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("(") ||
				nextToken.getToken().contentEquals(";"))
			expnStmt();
		else if(nextToken.getToken().contentEquals("{"))
			cmpndStmt();
		else
			parseFail();
	}
	
	private void expnStmt()
	{
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			expression();
			if(nextToken.getToken().contentEquals(";"))
					accept();
			else
				parseFail();
		}
		else if(nextToken.getToken().contentEquals(";"))
			accept();
		else
			parseFail();
	}
	
	private void selStmt()
	{
		if(nextToken.getToken().contentEquals("if"))
		{
			int index = output.getIndex() + 1;
			accept();
			if(nextToken.getToken().contentEquals("("))
				accept();
			else
				parseFail();
			
			Element e = expression();
			
			if(nextToken.getToken().contentEquals(")"))
				accept();
			else
				parseFail();
			statement();
			selStmtTwo(index, e);
		}
		else
			parseFail();
	}
	
	private void selStmtTwo(int ifIndex, Element e)
	{
		if(nextToken.getToken().contentEquals("else"))
		{
			String[] s = {"", "BR", "", "", ""};
			int elseIndex = output.getIndex();
			output.add(s);
			output.setResult(Integer.parseInt(e.getResult()) - 1,
					Integer.toString(output.getIndex() + 1));
			accept();
			statement();
			output.get(elseIndex)[4] = Integer.toString(output.getIndex() + 1);
		}
		else if(nextToken.getToken().contentEquals("if") ||
				nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("while") ||
				nextToken.getToken().contentEquals("return") ||
				nextToken.getToken().contentEquals("(") ||
				nextToken.getToken().contentEquals("{") ||
				nextToken.getToken().contentEquals("}") ||
				nextToken.getToken().contentEquals(";"))
			output.setResult(Integer.parseInt(e.getResult()) - 1,
					Integer.toString(output.getIndex() + 1));
		else
			parseFail();
	}
	
	private void iterStmt()
	{
		if(nextToken.getToken().contentEquals("while"))
		{
			int index = output.getIndex() + 1;
			accept();
			if(nextToken.getToken().contentEquals("("))
				accept();
			else
				parseFail();
			Element e = expression();
			if(nextToken.getToken().contentEquals(")"))
				accept();
			else
				parseFail();
			
			
			statement();
			String[] s = {"", "BR", "", "", Integer.toString(index)};
			output.add(s);
			output.setResult(Integer.parseInt(e.getResult()) - 1,
					Integer.toString(output.getIndex() + 1));
		}
		else
			parseFail();
	}
	
	private void returnStmt()
	{
		if(nextToken.getToken().contentEquals("return"))
		{
			accept();
			returnTwo();
		}
		else
			parseFail();
	}
	
	private void returnTwo()
	{
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			Element e = expression();
			if(nextToken.getToken().contentEquals(";"))
			{
				output.add();
				output.setOperation("return");
				output.setResult(e.getResult());
				accept();
			}
			else
				parseFail();
		}
		else if(nextToken.getToken().contentEquals(";"))
		{
			output.add();
			output.setOperation("return");
			accept();
		}
		else
			parseFail();
	}
	
	private Element expression()
	{
		Element exp = new Element();
		if(nextToken.getType().contentEquals("ID"))
			exp = expressID(exp);
		else if(nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
			exp = expressOther(exp);
		else
			parseFail();
		
		return exp;
	}
	
	private Element expressOther(Element exp)
	{
		if(nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			exp = factorAlt(exp);
			exp = termTwo(exp);
			exp = addExpnTwo(exp);
			exp = simpleExpn(exp);
		}
		
		return exp;
	}
	
	private Element expressID(Element exp)
	{
		if(nextToken.getType().contentEquals("ID"))
		{
			exp.setVar(nextToken.getToken());
			accept();
		}
		else
			parseFail();
		exp = expressIDTwo(exp);
		
		return exp;
	}
	
	private Element expressIDTwo(Element exp)
	{
		if(nextToken.getToken().contentEquals("("))
		{
			exp = call(exp);
			expressIDCall(exp);
		}
		else if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/") ||
				nextToken.getToken().contentEquals("=") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!=") ||
				nextToken.getToken().contentEquals("["))
		{
			exp = var(exp);
			exp = expressIDVar(exp);
		}
		else if(nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") ||
				nextToken.getToken().contentEquals(";"))
			exp.setResult(exp.getVar());
		else
			parseFail();
		
		return exp;
	}
	
	private Element expressIDVar(Element exp)
	{
		if(nextToken.getToken().contentEquals("="))
		{
			exp.setResult(exp.getVar());
			exp.setOperation("assign");
			accept();
			exp.setOp1(expression().getResult());
			output.add(exp.getArray());
			output.setOperation("assign");
			
			int x = 0;
			x = x + 5;
		}
		else if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!="))
		{
			exp.setOp1(exp.getVar());
			exp.setResult(exp.getVar());
			exp = termTwo(exp);
			exp = addExpnTwo(exp);
			exp = simpleExpn(exp);
		}
		else if(nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(";") ||
				nextToken.getToken().contentEquals(","))
		{
			if(exp.getResult().contentEquals("") && exp.getVar() != null)
				exp.setResult(exp.getVar());
		}
		else
			parseFail();
		
		return exp;
	}
	
	private Element expressIDCall(Element exp)
	{
		if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!="))
		{
			exp = termTwo(exp);
			exp = addExpnTwo(exp);
			exp = simpleExpn(exp);
		}
		else if(nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") ||
				nextToken.getToken().contentEquals(";"))
			;
		else
			parseFail();
		
		return exp;
	}
	
	private Element var(Element exp)
	{
		if(nextToken.getToken().contentEquals("["))
		{
			if(exp.getVar() == null)
				exp.setResult(exp.getResult() + "[");
			else
				exp.setVar(exp.getVar() + "[");
			accept();
			String e = expression().getResult();
			if(exp.getVar() == null)
				exp.setResult(exp.getResult() + e);
			else
				exp.setVar(exp.getVar() + e);
			if(nextToken.getToken().contentEquals("]"))
			{
				if(exp.getVar() == null)
					exp.setResult(exp.getResult() + "]");
				else
					exp.setVar(exp.getVar() + "]");
				accept();
			}
			else
				parseFail();
		}
		else if(!(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/") ||
				nextToken.getToken().contentEquals("=") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!=")))
				parseFail();
		
		return exp;
	}
	
	private Element simpleExpn(Element exp)
	{
		if(nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!="))
		{
			exp = relop(exp);
			exp.setOperation("comp");
			exp.setOp2(addExpn(new Element()).getResult());
			exp.setResult("_t" + tempIndex++);
			String br = exp.getBreak();
			output.add(exp.getArray());
			exp = new Element();
			exp.setOperation(br);
			exp.setOp1(output.getResult());
			exp.setResult(Integer.toString(output.getIndex() + 1));
			output.add(exp.getArray());
		}
		else if(!(nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") ||
				nextToken.getToken().contentEquals(";")))
			parseFail();
		
		return exp;
	}
	
	private Element relop(Element exp)
	{
		if(nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!="))
		{
			exp.setComp(nextToken.getToken());
			accept();
		}
		
		return exp;
	}
	
	private Element addExpn(Element exp)
	{
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			exp.setOp1(term().getResult());
			exp = addExpnTwo(exp);
		}
		else
			parseFail();
		
		return exp;
	}
	
	private Element addExpnTwo(Element exp)
	{
		if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-"))
		{
			exp = addop(exp);
			exp.setOp2(term().getResult());
			exp.setResult("_t" + tempIndex);
			output.add(exp.getArray());
			exp = new Element();
			exp.setResult("_t" + tempIndex++);
			exp.setOp1(exp.getResult());
			exp = addExpnTwo(exp);
		}
		else if(nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!=") ||
				nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") ||
				nextToken.getToken().contentEquals(";"))
		exp.setResult(exp.getOp1());
		else
			parseFail();
		
		return exp;
	}
	
	private Element addop(Element exp)
	{
		if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-"))
		{
			exp.setOperation((nextToken.getToken().contentEquals("+")?
					"add" : "sub"));
			accept();
		}
		else
			parseFail();
		
		return exp;
	}
	
	private Element term()
	{
		Element exp = new Element();
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			exp.setResult(factor().getResult());
			exp = termTwo(exp);
		}
		else
			parseFail();
		
		return exp;
	}
	
	private Element termTwo(Element exp)
	{
		if(nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/"))
		{
			exp.setOp1(exp.getResult());
			exp = mulop(exp);
			exp.setOp2(factor().getResult());
			exp.setResult("_t" + tempIndex);
			output.add(exp.getArray());
			exp = new Element();
			exp.setResult("_t" + tempIndex++);
			exp.setOp1(exp.getResult());
			exp = termTwo(exp);
		}
		else if(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!=") ||
				nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") |
				nextToken.getToken().contentEquals(";"))
			exp.setOp1(exp.getResult());
		else
			parseFail();
		
		return exp;
	}
	
	private Element mulop(Element exp)
	{
		if(nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/"))
		{
			exp.setOperation((nextToken.getToken().contentEquals("*")?
					"mult" : "div"));
			accept();
		}
		else
			parseFail();
		
		return exp;
	}
	
	private Element factor()
	{
		Element exp = new Element();
		if(nextToken.getType().contentEquals("ID"))
		{
			exp.setResult(nextToken.getToken());
			accept();
			exp = iden(exp);
		}
		else if(nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT"))
		{
			exp.setResult(nextToken.getToken());
			accept();
		}
		else if(nextToken.getToken().contentEquals("("))
		{
			accept();
			exp.setOp2(expression().getResult());
			if(nextToken.getToken().contentEquals(")"))
			{
				exp.setResult(exp.getOp2());
				accept();
			}
			else
				parseFail();
		}
		
		return exp;
	}
	
	private Element factorAlt(Element exp)
	{
		if(nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT"))
		{
			exp.setResult(nextToken.getToken());
			accept(); 
		}
		else if(nextToken.getToken().contentEquals("("))
		{
			accept();
			exp = expression();
			if(nextToken.getToken().contentEquals(")"))
				accept();
			else
				parseFail();
		}
		
		return exp;
	}
	
	private Element iden(Element exp)
	{
		if(nextToken.getToken().contentEquals("("))
			exp = call(exp);
		else if(nextToken.getToken().contentEquals("["))
			exp = var(exp);
		else if(!(nextToken.getToken().contentEquals("+") ||
				nextToken.getToken().contentEquals("-") ||
				nextToken.getToken().contentEquals("*") ||
				nextToken.getToken().contentEquals("/") ||
				nextToken.getToken().contentEquals("<=") ||
				nextToken.getToken().contentEquals("<") ||
				nextToken.getToken().contentEquals(">") ||
				nextToken.getToken().contentEquals(">=") ||
				nextToken.getToken().contentEquals("==") ||
				nextToken.getToken().contentEquals("!=") ||
				nextToken.getToken().contentEquals(")") ||
				nextToken.getToken().contentEquals("]") ||
				nextToken.getToken().contentEquals(",") ||
				nextToken.getToken().contentEquals(";")))
			parseFail();
		
		return exp;
	}
	
	private Element call(Element exp)
	{
		if(nextToken.getToken().contentEquals("("))
		{
			int lineNo = 0;
			exp.setOperation("call");
			if(exp.getVar() == null)
				exp.setOp1(exp.getResult());
			else
				exp.setOp1(exp.getVar());
			for(int i = 0; i < output.size(); i++)
			{
				if(output.get(i)[2].contentEquals(exp.getOp1()))
				{
					exp.setOp2(output.get(i)[0]);
					break;
				}
			}
			accept();
			args();
			if(nextToken.getToken().contentEquals(")"))
				accept();
			else
				parseFail();
			exp.setResult("_t" + tempIndex++);
			output.add(exp.getArray());
			exp = new Element();
			exp.setResult(output.getResult());
		}
		else
			parseFail();
		
		return exp;
	}
	
	private void args()
	{
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
			argList();
		else if(!(nextToken.getToken().contentEquals(")")))
			parseFail();
	}
	
	private void argList()
	{
		if(nextToken.getType().contentEquals("ID") ||
				nextToken.getType().contentEquals("NUM") ||
				nextToken.getType().contentEquals("FLT") ||
				nextToken.getToken().contentEquals("("))
		{
			Element exp = expression();
			output.add();
			output.setOperation("arg");
			if(exp.getVar() == null)
				output.setResult(exp.getResult());
			else
				output.setResult(exp.getVar());
			argListTwo();
		}
	}
	
	private void argListTwo()
	{
		if(nextToken.getToken().contentEquals(","))
		{
			accept();
			Element exp = expression();
			output.add();
			output.setOperation("arg");
			if(exp.getVar() == null)
				output.setResult(exp.getResult());
			else
				output.setResult(exp.getVar());
			argListTwo();	
		}
		else if(!nextToken.getToken().contentEquals(")"))
			parseFail();
	}
	
	/*	Method for ending parse in error if an
	 * 	undefined state is encountered. 
	 */
	private void parseFail()
	{
		System.out.println("Parse failed.");
		System.exit(1);
	}
}