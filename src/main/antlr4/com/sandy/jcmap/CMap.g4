grammar CMap ;

cmap               : (include_list)? (no_root_tag)? (graph_attrs)? (node_attrs)? (edge_attrs)? cmap_statements ;

include_list       : include_stmt ( ';' include_stmt )* ';' ;
include_stmt       : '@import' id_list_or_string ;

no_root_tag        : '@noroot' ';' ;

cmap_statements    : cmap_statement ( ';' cmap_statement )* ';' ;
cmap_statement     : concept_list_stmt 
                   | export_concept_stmt
                   | rank_concept_stmt
                   | preposition_stmt ;

graph_attrs        : '@graph' '{' nvp_list '}' ;
node_attrs         : '@node' '{' nvp_list '}' ;
edge_attrs         : '@edge' '{' nvp_list '}' ;

concept_list_stmt  : concept_list ;
export_concept_stmt: '@export' '{' concept_list '}';
rank_concept_stmt  : '@rank' '{' concept_list '}' ;

concept_list       : naked_concept_list  
                   | cluster_concept_list ;
                   
naked_concept_list   : concept (',' concept)* ;
cluster_concept_list : '{' naked_concept_list '}' ( '[' nvp_list ']' )? ;   
                   
concept            : concept_alias ( concept_label )? (is_link)? ( '[' nvp_list ']' )? ;
concept_alias      : id_list_or_string ;
concept_label      : '(' id_list_or_string ')' ;
is_link            : '@L' ;

//preposition_stmt   : concept_list '>' (linking_phrase)? '>' concept_list ( '>' (linking_phrase)? '>' concept_list )* ;
preposition_stmt   : concept_list link concept_list ( link concept_list )* ;
link               : '>' (linking_phrase)? '>' ;

linking_phrase     : lp_label ( '[' nvp_list ']' )? ;
lp_label           : id_list_or_string ; 

id_list_or_string  : (ID)+ | STRING ;

nvp_list           : nvp ( ',' nvp )* ;
nvp                : id ( '=' value )? ;

id                 : ID ;
value              : ID
                   | INT
                   | STRING ;

ID                 : (LETTER|DIGIT)+ ;
INT                : (DIGIT)+ ;
LETTER             : [\-a-zA-Z\u0080-\u00FF\u002e\u007e\u0040\u0023\u0024\u0025\u005e\u0026\u002a\u005f\u002b\u003f\u003a\u0027\u002f] ;
DIGIT              : [0-9] ;
STRING             : '"' ('\\"'|~'"')*? '"' ;

WS                 : [ \t\n\r]+                     -> skip ;
COMMENT            : '/*' .*? '*/'                  -> skip ;
LINE_COMMENT       : '//' .*? '\r'? '\n'            -> skip ;

