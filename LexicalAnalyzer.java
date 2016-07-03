/*	COP4620 - Project 1
 *	Stephen Repper
 *
 *	main() has been moved to Parser.java for Project 2
 *	Added methods getNextToken(), getMoreTokens()
 *	Changed print() to addToken [to Vector]
 */

import java.io.File;
import java.util.Scanner;
import java.util.Vector;

public class LexicalAnalyzer {
	
	int commentLayer = 0;
	boolean specSym = false;
	Vector<Token> tokens= new Vector<Token>();
	File file;
	Scanner input;
	String specialSymbols[] = {"+", "-", "<", ">", "=", ";", ",", "(", ")", 
							"[", "]", "{", "}", "!", "/", "*"};
	


	public LexicalAnalyzer(File f) throws Exception
	{
		file = f;
						
		//file read fail
		if (!(file.isFile() && file.canRead()))
		{	System.out.println("Error: " + file.getName() + 
			" cannot be read.");
			System.exit(1);
		}
			
		input = new Scanner(file);
	}

	//retrieves next token from Vector
	public Token getNextToken()
	{
		Token returnToken;
		while(tokens.isEmpty())
		{
			if(!getMoreTokens())
			{
				returnToken = new Token("$", "ENDOFFILE");
				return returnToken;
			}
		}
		
		returnToken = tokens.get(0);
		tokens.remove(0);
		return returnToken;
	}
	
	//fills Token Vector with next line from file
	private boolean getMoreTokens()
	{
		while(tokens.isEmpty())
		{
			if(input.hasNextLine())
				analyze(input.nextLine().trim());
			else
				return false;
		}
		
		return true;			
	}
	
	//analyzes next file line to extract tokens
	private void analyze(String s)
	{
		 if(s.length() == 0)
			 return;
		 
		 //System.out.println("INPUT:  " + s);
		 
		 int sPos = 0;
		 
		 //for every position in the file line
		 while(sPos < s.length())
		 {
			 String token = "";
			 
			 //skip whitespace
			 if(Character.isWhitespace(s.charAt(sPos)))
			 {
				 sPos++;
				 continue;
			 }
			 //catch line comments and block comment openings
			 else if(s.charAt(sPos) == '/')
			 {
				 //if line comment, skip remainder of line
				 if(sPos + 1 < s.length() && s.charAt(sPos+ 1) == '/')
					 break;
				 //if block comment, increment commentLayer & skip '*' char
				 else if(sPos + 1 < s.length() && s.charAt(sPos+ 1) == '*')
				 {
					 commentLayer++;
					 sPos += 2;
				 }
				 //lone '/' character - add to token
				 else
				 {
					 specSym = true;
					 token = token + s.charAt(sPos++);
				 }
			 }
			 //catch closing block comments
			 else if(s.charAt(sPos) == '*')
			 {
				 //if next char is '/', decrement commentLayer & skip '/' char
				 if(sPos + 1 < s.length() && s.charAt(sPos + 1) == '/' && commentLayer > 0)
				 {
					 commentLayer--;
					 sPos += 2;
				 }
				 //lone '*' character - add to token
				 else
				 { 
					 specSym = true;
					 token = token + s.charAt(sPos++);
				 }
			 }
			 //handle keywords and identifiers
			 else if(Character.isLetter(s.charAt(sPos)))
			 {
				 token = token + s.charAt(sPos++);
				 
				 //while next character is a letter
				 while(sPos < s.length() && Character.isLetter(s.charAt(sPos)))
				 {
					 //if next char isn't a letter or a special symbol, error
					 if(sPos + 1 < s.length() &&
						!Character.isLetter(s.charAt(sPos + 1)) && 
						!Character.isWhitespace(s.charAt(sPos + 1)) &&
						!isSpecialSymbol(Character.toString(s.charAt(sPos + 1))))
					 {
						 token = token + s.charAt(sPos++);
						 while(sPos < s.length() &&
							!Character.isWhitespace(s.charAt(sPos)) &&
							!isSpecialSymbol(Character.toString(s.charAt(sPos))))
						 {
							 token = token + s.charAt(sPos++);
						 }
						 break;
					 }
					 //if token has 8 characters, skip remaining characters
					 if(token.length() == 8)
					 {
						 while(sPos + 1 < s.length() && 
								 Character.isLetter(s.charAt(++sPos)))
							 ;
						 break;
					 }
					 token = token + s.charAt(sPos++);
				 }
			 }
			 //handle integers and floats
			 else if(Character.isDigit(s.charAt(sPos)))
			 {
				 token = token + s.charAt(sPos++);
				 
				 //add digits to token while they remain
				 while(sPos < s.length() && Character.isDigit(s.charAt(sPos)))
				 {
					 token = token + s.charAt(sPos++);
				 }
				 //detect decimal point
				 if(sPos < s.length() && s.charAt(sPos) == '.')
				 {
					 token = token + s.charAt(sPos++);
					 
					 //add digits to token while they remain
					 while(sPos < s.length() &&
						   Character.isDigit(s.charAt(sPos)))
					 {
						 token = token + s.charAt(sPos++);
					 }
				 }
				 //detect 'E' & add to token
				 if(sPos < s.length() && s.charAt(sPos) == 'E')
				 {
					 token = token + s.charAt(sPos++);
					 //if next char is '+' or '-', add to token
					 if(sPos < s.length() && (s.charAt(sPos) == '+' ||
						s.charAt(sPos) == '-'))
					 {
						 token = token + s.charAt(sPos++);
					 }
					 //add digits to token while they remain
					 while(sPos < s.length() && 
							   Character.isDigit(s.charAt(sPos)))
					 {
						 token = token + s.charAt(sPos++);
					 }
	
				 }
				 
			 }
			 //if character is special symbol, add to token 
			 else if(isSpecialSymbol(Character.toString(s.charAt(sPos))))
			 {
				 specSym = true;
				 token = token + s.charAt(sPos);
				 
				 //detect potential double-character symbols
				 if(s.charAt(sPos) == '!')
				 {
					 if(++sPos < s.length() && s.charAt(sPos) == '=')
						 token = token + s.charAt(sPos++);
					 else
						 specSym = false;
				 }
				 else if(s.charAt(sPos) == '<' ||
						 s.charAt(sPos) == '>' || s.charAt(sPos) == '=')
				 {
					 if(++sPos < s.length() && s.charAt(sPos) == '=')
						 token = token + s.charAt(sPos++);
				 }
				 else
					 sPos++;
			 }
			 //any unrecognized chars are added to token to be passed
			 else
			 {
				 token = token + s.charAt(sPos++);
				 while(sPos < s.length() &&
					   !Character.isWhitespace(s.charAt(sPos)) &&
					   !isSpecialSymbol(Character.toString(s.charAt(sPos))))
				 {
					 token = token + s.charAt(sPos++);
				 }
			 }
			 
			 addToken(token);
		 }
		 
	}
	
	//adds current token to Token Vector
	private void addToken(String token)
	{
		Token t;
		//check for comment block
		if(commentLayer != 0)
		{
			if(specSym)
				specSym = false;
		}
		//verify token exists
		else if(token.length() != 0)
		{
			 //handle special symbols
			 if(specSym)
			 {
				 t = new Token(token, "symbol");
				 tokens.add(t);
				 specSym = false;
				 return;				
			 }
			 //check token against Integer
			 if(token.matches("\\d+"))
			 {
				 t = new Token(token, "NUM");
				 tokens.add(t);
			 }
			 //check token against Float
			 else if(token.matches("\\d+(\\.{1}\\d+)?(E{1}[+-]?\\d+)?"))
			 {
				 t = new Token(token, "FLT");
				 tokens.add(t);
			 }
			 //check for keywords
			 else if(token.contentEquals("else") ||
					 token.contentEquals("if") ||
					 token.contentEquals("int") ||
					 token.contentEquals("return") ||
					 token.contentEquals("void") ||
					 token.contentEquals("while"))
			 {
				 t = new Token(token, "keyword");
				 tokens.add(t);
			 }
			 //check token against ID
			 else if(token.matches("[a-zA-Z]{1,8}"))
			 {
				 t = new Token(token, "ID");
				 tokens.add(t);
			 }
			 //unrecognized token; print error
			 else 
			 {
				 System.out.println("Error:  invalid token " + token);
				 System.exit(1);
			 }
		 }
	}
	
	//determine whether token is special symbol
	private boolean isSpecialSymbol(String token)
	{
		for(int i = 0; i < specialSymbols.length; i++)
		 {
			 if(token.contentEquals(specialSymbols[i]))
				 return true;
		 }
		return false;
	}
}