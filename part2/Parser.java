/* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
	     block();
    }

    private void block(){
	     declaration_list();
	     statement_list();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	   while( is(TK.DECLARE) ) {
	      declaration();
     }

    }

    private void declaration() {
	    mustbe(TK.DECLARE);
	    mustbe(TK.ID);
      while( is(TK.COMMA) ) {
	       scan();
	       mustbe(TK.ID);
	    }
    }

    private void statement_list() {
      //check what type of statement I am looking for
      while(is(TK.ID)|| is(TK.TILDE) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)) {

        if(is(TK.ID)||is(TK.TILDE))
        {
            assignment();
        }
        else if( is(TK.PRINT))
        {
          printFunc();
        }
        else if(is(TK.DO))
        {
          mustbe(TK.DO);
          doFunc();
          mustbe(TK.ENDDO);
        }
        else if(is(TK.IF))
        {
          mustbe(TK.IF);
          ifFunc();
          mustbe(TK.ENDIF);
        }

      }
    }

    private void assignment(){ // if its an assignmnet I check for an id, an = then an expr
      if(is(TK.ID))
      {
        refID();
        mustbe(TK.ASSIGN);
        expr();
      }
      else if(is(TK.TILDE)) // if I see a ~ i do the same thing
      {
        refID();
        mustbe(TK.ASSIGN);
        expr();

      }

    }

    private void printFunc(){ // check for ! then expression
      mustbe(TK.PRINT);
      expr();
    }
    private void refID(){
      if(is(TK.TILDE))
      {
        mustbe(TK.TILDE);
        if(is(TK.NUM))
        {
          mustbe(TK.NUM);
        }
      }
      mustbe(TK.ID);
    }
    private void doFunc(){ // if a doFunc is called check for guarded_command
      guarded_command();
    }
    private void ifFunc(){
      //mustbe(TK.IF);
      //scan();
      guarded_command();
      //scan();
      if(is(TK.ELSEIF)){
        while( is(TK.ELSEIF)) { // until no more | is found look for a guarded_command
        //scan();
          mustbe(TK.ELSEIF);
          guarded_command();
        }
      }
      if(is(TK.ELSE))
      {
        mustbe(TK.ELSE);
        block();
      }
      //scan();
      //mustbe(TK.ENDIF);
    }
    private void expr(){
      term();
      while(is(TK.PLUS) || is(TK.MINUS)) { // allows for additon and subtraction
	       scan();
	       term();
	    }

    }
    private void guarded_command(){
      expr();
      mustbe(TK.THEN);
      block();
    }
    private void term(){
      factor();
      while(is(TK.TIMES) || is(TK.DIVIDE)) {
	       scan();
	       factor();
	    }

    }
    private void factor(){
        if(is(TK.LPAREN)) // if theres a ( there has to be a )
        {
          scan();
          expr();
          //scan();
          mustbe(TK.RPAREN);
        }
        else if(is(TK.ID))
        {
          mustbe(TK.ID);
        }
        else if(is(TK.TILDE))
        {
          refID();
        }
        else if(is(TK.NUM))
        {
          mustbe(TK.NUM);
        }
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " + tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line " + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
