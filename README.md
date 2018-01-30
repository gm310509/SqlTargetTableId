# SQL Target Table ID
## for Teradata

A Teradata UDF that identifies the target of an action query. For example:

```sql
insert into tgt  
select a, b, c  
from src1 S1 join S2 on  
    S1.id = S2.id  
```

The target of the above query is "tgt".

The UDF will parse the query and return the name of the target of the query as the result (i.e. tgt).
For testing purposes, the compiled JAR may also be run as a GUI if it is passed the -ui parameter.
The GUI mode is not accessible when installed on Teradata as a UDF.

The code supports most, if not all preambles including:
* Locking
* Temporal
* Using

It supports the following action query types:
* Insert
* Update
* Delete
* Merge
* Create
* CT

It does not parse the entire query. Once it identifies the target table, the parsing stops.
For example, you could pass the query `Insert into X;` and the code will return X - despite the fact that such a query is invalid.

The UDF will recognise fully qualified target names (e.g. `insert into db.tbl ...`) and partially qualified targets (e.g. `insert into tbl ...`)

If you only wish to compile the code, any IDE should do.
However, If you wish to alter, extend and/or fix the SQL syntax recognised by this code, you will need javacc to recompile the grammar.
