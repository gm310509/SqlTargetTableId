call sqlj.install_jar('cj!dist/SqlTargetTableId.jar', 'SqlTargetTableIdJAR', 0);


REPLACE FUNCTION SqlTarget(queryText varchar(63000))
RETURNS varchar(80)
LANGUAGE JAVA
NO SQL
PARAMETER STYLE JAVA
RETURNS NULL ON NULL INPUT
specific SqlTargetNoDefault
EXTERNAL NAME 'SqlTargetTableIdJAR:com.teradata.edwdecoded.sql.idtarget.Main.idTargetTableUDF(java.lang.String)';

REPLACE FUNCTION SqlTarget(defaultDb varchar(30), queryText varchar(63000))
RETURNS varchar(80)
LANGUAGE JAVA
NO SQL
PARAMETER STYLE JAVA
RETURNS NULL ON NULL INPUT
specific SqlTargetWithDefault
EXTERNAL NAME 'SqlTargetTableIdJAR:com.teradata.edwdecoded.sql.idtarget.Main.idTargetTableUDF(java.lang.String, java.lang.String)';


drop function SqlTarget (varchar(30), varchar(63000));
drop function SqlTarget (varchar(63000));

call sqlj.remove_jar('SqlTargetTableIdJAR', 0);
