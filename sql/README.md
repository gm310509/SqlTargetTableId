# SQL for creating the UDF on Teradata

The setup.sql file consists of two parts.
1. Queries to install the JAR and create two overloaded functions.
1. Queries to drop the two overloaded functions and remove the JAR.

When "upgrading" the JAR, it is always, IMHO, a good idea to drop the UDFs and remove the JAR then run the installation queries as a JAR.
I do not advise simply replacing the JAR. Usually simply replacing the JAR works, but I've sometimes experienced "weird behaviour" when doing that.

The two overloaded functions are as follows:
* Single Parameter variant. The parameter is the SQL to parse. The target table is identified and returned as it appears in the query.
* Double Parameter variant. The second parameter is the SQL to parse. The first parameter is the name of the defaultDB. Ideally this is the default DB that is active when the query was run.  
The Double Parameter variant will parse the query and identify the target table.  
If the target table is fully qualified (i.e. looks like db.tbl) then it will be returned as is.
Otherwise the defaultDB is prepended with a "period" to fully qualify the target. For example if the query is "insert into X ..." the target is X. The defaultDB value will be prepended to the target and returned as "DB.X" (assuming DefaultDB = "DB")
