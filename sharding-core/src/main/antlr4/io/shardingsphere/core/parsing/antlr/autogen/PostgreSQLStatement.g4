grammar PostgreSQLStatement;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, PostgreSQLCreateIndex, PostgreSQLAlterIndex
       , PostgreSQLDropIndex, PostgreSQLCreateTable, PostgreSQLAlterTable, PostgreSQLDropTable, PostgreSQLTruncateTable
       , PostgreSQLTCLStatement
       ;

execute
    : createIndex
    | alterIndex
    | dropIndex
    | createTable
    | alterTable
    | dropTable
    | truncateTable
    | setTransaction
    | commit
    | rollback
    | savepoint
    | beginWork
    ;
 