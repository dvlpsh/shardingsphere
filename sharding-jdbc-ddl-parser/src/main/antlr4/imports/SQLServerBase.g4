grammar SQLServerBase;

import SQLServerKeyword,Keyword,Symbol,BaseRule,DataType;

ID: 
	(LEFT_BRACKET? [a-zA-Z_$#][a-zA-Z0-9_$#]* RIGHT_BRACKET? DOT*)?
	(LEFT_BRACKET? [a-zA-Z_$#][a-zA-Z0-9_$#]* RIGHT_BRACKET?)
	|[a-zA-Z0-9_$]+ DOT ASTERISK
	;
	
dataType: 
	typeName   
    (
    	LEFT_PAREN  
	    	(
	    		(NUMBER ( COMMA NUMBER )?)
	    		| MAX 
	    		|((CONTENT | DOCUMENT)? xmlSchemaCollection) 
	    	)
    	RIGHT_PAREN 
    )?   
	;
	
	privateExprOfDb:
	windowedFunction
	|atTimeZoneExpr
	|castExpr
	|convertExpr
	;

atTimeZoneExpr:
	ID (WITH TIME ZONE)? STRING
	;
	
castExpr:
	CAST LEFT_PAREN expr AS dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? RIGHT_PAREN  
	;
	
convertExpr:
	CONVERT ( dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? COMMA expr (COMMA NUMBER)?)
	;
	
windowedFunction:
 	functionCall overClause
 	;
 	
 overClause:
	OVER 
		LEFT_PAREN     
	      partitionByClause?
	      orderByClause?  
	      rowRangeClause? 
	    RIGHT_PAREN 
	;
	
partitionByClause:  
	PARTITION BY expr (COMMA expr)*  
	;
	
orderByClause:   
	ORDER BY orderByExpr (COMMA orderByExpr)*
   ;
  
orderByExpr:
  	expr (COLLATE collationName)? (ASC | DESC)? 
	;
	
rowRangeClause:  
 	(ROWS | RANGE) windowFrameExtent
 	;
 
 windowFrameExtent: 
	windowFramePreceding
  	| windowFrameBetween 
	; 
	
windowFrameBetween:
  	BETWEEN windowFrameBound AND windowFrameBound  
	;
	
windowFrameBound:  
	windowFramePreceding 
  	| windowFrameFollowing 
	;
	
windowFramePreceding:  
    (UNBOUNDED PRECEDING)  
  | NUMBER PRECEDING  
  | CURRENT ROW  
; 

windowFrameFollowing:
    UNBOUNDED FOLLOWING  
  | NUMBER FOLLOWING  
  | CURRENT ROW  
;

columnNameWithSortsWithParen:
	LEFT_PAREN columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
	;
		
columnNameWithSort:
	columnName ( ASC | DESC )?
	;

indexOption :
	(FILLFACTOR EQ_OR_ASSIGN NUMBER) 
	| eqOnOffOption
	| ((COMPRESSION_DELAY | MAX_DURATION) eqTime)
	| MAXDOP EQ_OR_ASSIGN NUMBER
	| (compressionOption
	   ( ON PARTITIONS LEFT_PAREN partitionExpressions  RIGHT_PAREN )?)
	;
	
compressionOption:
	DATA_COMPRESSION EQ_OR_ASSIGN ( NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE)
	;
	
eqTime:
	EQ_OR_ASSIGN NUMBER (MINUTES)?
	;
	
eqOnOffOption:
  	(
  		PAD_INDEX
  		|SORT_IN_TEMPDB
  		|IGNORE_DUP_KEY
  		|STATISTICS_NORECOMPUTE
  		|STATISTICS_INCREMENTAL
  		|DROP_EXISTING
  		|ONLINE
  		|RESUMABLE
  		|ALLOW_ROW_LOCKS
  		|ALLOW_PAGE_LOCKS
  		|COMPRESSION_DELAY
  		|SORT_IN_TEMPDB
  	)
  	eqOnOff 
  	;
 
eqOnOff:
  	EQ_OR_ASSIGN ( ON | OFF )
  	;

partitionExpressions:
	partitionExpression (COMMA partitionExpression)*
	;

partitionExpression:
	NUMBER 
	|numberRange
	;
	
numberRange:   
	NUMBER TO NUMBER  
	;
	