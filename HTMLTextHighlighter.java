package CapitalOne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class HTMLTextHighlighter {
	
	/*
	 * Highlighter(String html, HashMap<String, String> tagColorMap): Handles the conversion of HTML
	 * file to the colorCoded output 
	 * 
	 * colorEncoding(String tag, HashMap<String, String> encoding): Adds \color[COLOR] at the 
	 * required places in the output.
	 * 
	 * tagMatches(String open, String close): Matches opening and closing tags with each other
	 */
	
	
	public static String Highlighter(String html, HashMap<String, String> tagColorMap){
		StringBuilder colorCodedText = new StringBuilder(); //Stores the final result with color codes
		
		StringBuilder startTag = new StringBuilder(); //tracks the name of the opening tag
		StringBuilder endTag = new StringBuilder();//tracks the name of the closing tag
		
		Stack<String> tagTracker = new Stack<String>(); //keeps track of the tag couples
		Boolean detectingATag = false; //checks if  loop is in the middle of a tag
		Boolean inAClosingTag = false; //checks if loop is in the middle of a closing tag
		Boolean tagClosed = false; //checks if loop closed a closing tag in the previous iteration of loop
		
		for(int i = 0; i < html.length(); i++){ //loops through HTML input
			char currentCharacter = html.charAt(i); 

			
			/*
			 * checks if the current character is a line break. May be replaced with a system-independent
			 * line separator to avoid issues in portability of the program.
			 */
			if( currentCharacter == '\n'){ 
				colorCodedText.append("\n");
				continue;
			}
			
			if(currentCharacter == '<'){ 
				/* finds a opening bracket*/
				
				detectingATag = true; //tag is detected
				
				if(html.charAt(i+1) == '/'){//confirms a closing tag. Using direct index may be a vulnerability. Can replace with a check for OutOfBounds Exception.
					if(tagClosed){ //if in previous iteration of for loop, a tag was closed
						colorCodedText.append(colorEncoding(tagTracker.peek(), tagColorMap));
						tagClosed = false;
					}
					inAClosingTag = true; //closing tag is detected
					endTag.setLength(0); //sets endTag to null for reuse
				}
				else{
					startTag.setLength(0); //sets startTag to null for reuse
				}	
				
			}
			else if (currentCharacter == '>'){
				/* finds a closing bracket*/
				
				if(!inAClosingTag){ 
					/*closing bracket of an opening tag*/
					
					//pushes last seen opening tag name onto Stack, except if its <br>
					String sTag = startTag.toString();
					if(!sTag.equals("br")){
							tagTracker.push(sTag);
					}
					
					//calls function to append colorCode
					colorCodedText.append(colorEncoding(sTag, tagColorMap));
					
					//adds the last read tag after the colorCode i.e. \color[COLOR]
					colorCodedText.append("<");
					colorCodedText.append(sTag);
					colorCodedText.append(">");
					
					detectingATag = false; //loop is not in a tag
					tagClosed = false; //does not close a closing tag
					startTag.setLength(0); //setting tag title to null
				}
				else{
					/*closing bracket of an closing tag*/
					
					String eTag = endTag.toString(); //gets the last seen closing tag
					String top = tagTracker.peek(); //checks what tag is at top of stack
					
					
					if(tagMatches(top, eTag)){ 
						/*tags have been properly closed.*/

						if( tagTracker.size() >= 1){
							//goes back to same color as before
							String lastone = tagTracker.pop();
							
							colorCodedText.append("<");
							colorCodedText.append(eTag);
							colorCodedText.append(">");
							
							tagClosed = true; //closes a closing tag
						}
					}
					else{
						//In case the input has mismatched brackets i.e. invalid HTML code
						System.out.println("Matching tags are not equal");
					}
					
					detectingATag = false;
					endTag.setLength(0);
					inAClosingTag = false;
				}
				
			}
			else{ 
				/*finds any other character*/
				
				if(detectingATag && !inAClosingTag){ //opening tag title
					startTag.append(currentCharacter);
				}
				else if(detectingATag && inAClosingTag){ //closing tag title
					endTag.append(currentCharacter);
				}
				else if(!detectingATag && !inAClosingTag){
					if(tagClosed){
						colorCodedText.append(colorEncoding(tagTracker.peek(), tagColorMap));
						tagClosed = false;
					}
					colorCodedText.append(currentCharacter);
				}
				else{
					System.out.println("Invalid HTML Code");
				}
			}
		}
	
		return colorCodedText.toString();
	}
	
	public static String colorEncoding(String tag, HashMap<String, String> tagColorMap){
		String tagPrefix = tag.split(" ")[0].toLowerCase();
		StringBuilder colorCode = new StringBuilder();
		
		colorCode.append("\\color[");
		colorCode.append(tagColorMap.get(tagPrefix));
		colorCode.append("]");
		return colorCode.toString();
	}
	
	public static void writeFile(String filename, String text){ //used to write a file
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(filename), "utf-8"))) {
			writer.write(text);
		} catch (Exception e){
			System.out.println("Error in creating file");
		}
	}
	
	public static boolean tagMatches(String open, String close){
		// Assumes close starts with "/"
		String closeNoSlash = close.substring(1).toLowerCase();
		String cleanClosingTag = closeNoSlash.split(" ")[0].toLowerCase(); //selects clause before whitespace
		String cleanOpeningTag = open.split(" ")[0].toLowerCase(); //selects clause before whitespace. Helps in handling A HREF
		return cleanClosingTag.equals(cleanOpeningTag); //matches the starting clauses of these tags
	}
	
	
	public static void main(String[] args){
		
		String path = null;
		
		if(args.length == 0){
			System.out.println("No arguments were given");
			System.exit(-1);
		}
		else{
			path = args[0]; //sets path to what is given in arguments
		}
		
		
		/*HashMap allows for faster lookup. However, manual addition of each color may be a hindrance
		 * if we decide to add a large number of new HTML tags*/
		HashMap<String, String> tagColorMap = new HashMap<String, String>(); 
		tagColorMap.put("html", "RED");
		tagColorMap.put("head", "YELLOW");
		tagColorMap.put("title", "GREEN");
		tagColorMap.put("body", "TURQUOISE");
		tagColorMap.put("h1", "DARKGREEN");
		tagColorMap.put("p", "DARKGRAY");
		tagColorMap.put("br", "PINK");
		tagColorMap.put("a", "BLUE");

		StringBuilder fileString = new StringBuilder(); 
		FileReader in = null;
		try {
			in = new FileReader(path);
		} catch (FileNotFoundException e) {
			System.out.println("The inputted file path is not valid");
			e.printStackTrace();
		}
	    BufferedReader br = new BufferedReader(in);
	    String line;
	    try {
			while((line = br.readLine()) != null){
				fileString.append(line);
				fileString.append("\n");
			}
		} catch (IOException e) {
			System.out.println("The new line is null");
			e.printStackTrace();
		}
	    System.out.println(fileString.toString());
		String colorEncodedText = Highlighter(fileString.toString(), tagColorMap);
		System.out.println();
		System.out.println(colorEncodedText);
		writeFile("newFile.html", colorEncodedText); //produces the output in a file
	}
}
