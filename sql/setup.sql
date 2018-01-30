/*****************************************************************
 * Install the JAR and Create the UDF
 ****************************************************************/
call sqlj.install_jar('cj!dist/SqlTargetTableId.jar', 'SqlTargetTableIdJAR', 0);

/*****************************************************************
 * First overloaded function that simply parses the query text and
 * returns the identified target - as it appears in the query.
 ****************************************************************/
REPLACE FUNCTION SqlTarget(queryText varchar(63000))
RETURNS varchar(80)
LANGUAGE JAVA
NO SQL
PARAMETER STYLE JAVA
RETURNS NULL ON NULL INPUT
specific SqlTargetNoDefault
EXTERNAL NAME 'SqlTargetTableIdJAR:com.teradata.edwdecoded.sql.idtarget.Main.idTargetTableUDF(java.lang.String)';

/*****************************************************************
 * Second overloaded function that parses the query text and
 * returns the fully qualified target.
 * If the target is fully qualified in the query, then what is read
 * from the query is returned.
 * However, if the target is only partially qualified in the query,
 * then the "defaultDB" is prepended to the target prior to returning.
 ****************************************************************/
REPLACE FUNCTION SqlTarget(defaultDb varchar(30), queryText varchar(63000))
RETURNS varchar(80)
LANGUAGE JAVA
NO SQL
PARAMETER STYLE JAVA
RETURNS NULL ON NULL INPUT
specific SqlTargetWithDefault
EXTERNAL NAME 'SqlTargetTableIdJAR:com.teradata.edwdecoded.sql.idtarget.Main.idTargetTableUDF(java.lang.String, java.lang.String)';


/*****************************************************************
 * Drop the UDF and remove the JAR
 ****************************************************************/

drop function SqlTarget (varchar(30), varchar(63000));
drop function SqlTarget (varchar(63000));

call sqlj.remove_jar('SqlTargetTableIdJAR', 0);
