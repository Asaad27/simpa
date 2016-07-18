grammar DotMealy;

		
graph
	:	STRICT? ( GRAPH | DIGRAPH ) ID? '{' mealy_list '}' 
	;


mealy_list
	: ( mealy_trans ';'? )* //| ( mealy_trans ? )*
	;


mealy_trans
	:	edge ( '[' mealy_attrs ']' )+
    |	label_name '=' ( value | '"'* ) 
    |	state (  '[' mealy_attrs ']' )*
//    |	label_name ('=' quote*)
    ;

//attr_list
//	: 	( '[' mealy_attrs ']' )+
//	;


mealy_attrs
//	:	label_name ('=' '"'? mealy_io '"'? )*
	:	label_name ('=' '"'? ( input '/' output )* '"'? )*
	|	label_name ('=' '"'? value* '"'? )*  
    ;


 

label_name
	: ID
	;


edge
 	:	//ID '->' ID
 	|	(ID '->' (ID)*)* ';'? 	
 //	|	(state edgeop (state)*)* ';'?
 	;
 	
state
	: 	ID
	
	;
 	
edgeop
 	: '->' | '--' 
 	;	
 	
 	

trans
	:	edge ( '[' mealy_attrs ']' )*
	;

value
	:	ID | HTML_STRING | NUMBER  
	;

input
	:	ID
	|	NUMBER	
	;

output
	:	ID 
	|	NUMBER
	;

//sentence
//  :  (STRING)+
//  ;
//  
//word
//  :  ( LETTER | DIGIT) ( LETTER | DIGIT)*
//  ;






/***************************** */



// "The keywords node, edge, graph, digraph, subgraph, and strict are
// case-independent"

STRICT
   : [Ss] [Tt] [Rr] [Ii] [Cc] [Tt]
   ;


GRAPH
   : [Gg] [Rr] [Aa] [Pp] [Hh]
//   :  'graph' | 'digraph'
   ;


DIGRAPH
   : [Dd] [Ii] [Gg] [Rr] [Aa] [Pp] [Hh]
   ;


NODE
   : [Nn] [Oo] [Dd] [Ee]
   ;


EDGE
   : [Ee] [Dd] [Gg] [Ee]
   ;


SUBGRAPH
   : [Ss] [Uu] [Bb] [Gg] [Rr] [Aa] [Pp] [Hh]
   ;


/** "a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )" */ 
NUMBER
   : '-'? ( '.' DIGIT+ | DIGIT+ ( '.' DIGIT* )? )
   ;


fragment DIGIT
   : ('0'..'9')
   ;

/** "Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores
 *  ('_') or digits ([0-9])"
 */ 
ID
   : LETTER ( LETTER | DIGIT )*
   | DIGIT ( LETTER | DIGIT )*   
   ;

//STRING 
//	: ('a'..'z'|'A'..'Z'|'0'..'9'|'_')+ 
//	;



/** "HTML strings, angle brackets must occur in matched pairs, and
 *  unescaped newlines are allowed."
 */ 
HTML_STRING
   : '<' ( TAG | ~ [<>] )* '>'
   ;
   
fragment LETTER
   : [a-zA-Z\u0080-\u00FF_]
   ;

fragment TAG
   : '<' .*? '>'
   ;


COMMENT
   : '/*' .*? '*/' -> skip
   ;


LINE_COMMENT
   : '//' .*? '\r'? '\n' -> skip
   ;


/** "a '#' character is considered a line output from a C preprocessor (e.g.,
 *  # 34 to indicate line 34 ) and discarded"
 */ 
PREPROC
   : '#' .*? '\n' -> skip
   ;


WS
   : [ \t\n\r]+ -> skip
   ;
   
 
