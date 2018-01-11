grammar DotMealy;

/*
From https://graphviz.gitlab.io/_pages/doc/info/lang.html
graph 	: 	[ strict ] (graph | digraph) [ ID ] '{' stmt_list '}'
stmt_list 	: 	[ stmt [ ';' ] stmt_list ]
stmt 	: 	node_stmt
	| 	edge_stmt
	| 	attr_stmt
	| 	ID '=' ID
	| 	subgraph
attr_stmt 	: 	(graph | node | edge) attr_list
attr_list 	: 	'[' [ a_list ] ']' [ attr_list ]
a_list 	: 	ID '=' ID [ (';' | ',') ] [ a_list ]
edge_stmt 	: 	(node_id | subgraph) edgeRHS [ attr_list ]
edgeRHS 	: 	edgeop (node_id | subgraph) [ edgeRHS ]
node_stmt 	: 	node_id [ attr_list ]
node_id 	: 	ID [ port ]
port 	: 	':' ID [ ':' compass_pt ]
	| 	':' compass_pt
subgraph 	: 	[ subgraph [ ID ] ] '{' stmt_list '}'
compass_pt 	: 	(n | ne | e | se | s | sw | w | nw | c | _)
*/



graph
	:	STRICT? (GRAPH | DIGRAPH)  id? '{' stmt_list '}'
	;

stmt_list
	:	(stmt  ';'?)*
	;

stmt
	:	node_stmt
	|	edge_stmt
	|	attr_stmt
	|	id '=' id
	|	subgraph
	;

attr_stmt
	:	(GRAPH | NODE | EDGE) attr_list
	;

attr_list
	:	'['  a_list? ']'  attr_list?
	;

a_list
	: 	id '=' id (';' | ',')? a_list?
	;

edge_stmt
	:	(node_id | subgraph) edgeRHS attr_list?
	;

edgeRHS
	:	EDGEOP (node_id | subgraph) edgeRHS?
	;

node_stmt
	:	node_id attr_list?
	;

node_id
	:	id port?
	;

port
	:	':' id ( ':' COMPASS_PT )?
	|	':' COMPASS_PT
	;

subgraph
	:	( SUBGRAPH id? )? '{' stmt_list '}'
	;

COMPASS_PT
	:	[NnSs] [eEwW]?
	|	[eEwW]
	|	[cC_]
	;

EDGEOP
	: '->' | '--'
	;




/***************************** */



// "The keywords node, edge, graph, digraph, subgraph, and strict are
// case-independent"

STRICT
   : [Ss] [Tt] [Rr] [Ii] [Cc] [Tt]
   ;


GRAPH
   : [Gg] [Rr] [Aa] [Pp] [Hh]
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


/***************************** */

id
   :	UNQUOTED_STRING
   |	NUMBER
   |	DOUBLE_QUOTED_STRING
   |	HTML_STRING
   ;

NUMBER
   : '-'? ( '.' DIGIT+ | DIGIT+ ( '.' DIGIT* )? )
   ;


fragment DIGIT
   : ('0'..'9')
   ;

UNQUOTED_STRING 
	: LETTER ( LETTER | DIGIT )*
	;

DOUBLE_QUOTED_STRING
	:	'"' ('\\\\' | '\\"' | ~["\\])* '"'
	;


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


/***************************** */

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


