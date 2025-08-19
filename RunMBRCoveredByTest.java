import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;

/**
 * Simple runner to test MBRCoveredBy function parsing in ShardingSphere
 */
public class RunMBRCoveredByTest {
    
    public static void main(String[] args) {
        System.out.println("=== MBRCoveredBy Function Parsing Test ===");
        System.out.println("Testing whether ShardingSphere can parse MBRCoveredBy() function calls");
        System.out.println();
        
        CacheOption cacheOption = new CacheOption(128, 1024L);
        SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
        SQLStatementVisitorEngine visitorEngine = new SQLStatementVisitorEngine("MySQL");
        
        String[] testSqls = {
            "SELECT MBRCoveredBy(@g1, @g2) FROM t_order",
            "SELECT * FROM t_order WHERE MBRCoveredBy(POINT(1,1), POINT(user_id,order_id))",
            "SELECT * FROM t_order WHERE MBRCoveredBy(geom1, geom2)"
        };
        
        int successCount = 0;
        int totalTests = testSqls.length;
        
        for (String sql : testSqls) {
            System.out.println("Testing SQL: " + sql);
            
            try {
                // Parse SQL to AST
                ParseASTNode astNode = parserEngine.parse(sql, false);
                System.out.println("  ✅ SQL parsed to AST successfully");
                
                // Convert to SQLStatement
                SQLStatement statement = visitorEngine.visit(astNode);
                System.out.println("  ✅ AST converted to SQLStatement successfully");
                
                if (statement instanceof SelectStatement) {
                    SelectStatement select = (SelectStatement) statement;
                    boolean foundFunction = findMBRCoveredByFunction(select);
                    
                    if (foundFunction) {
                        System.out.println("  ✅ MBRCoveredBy function found and parsed correctly");
                        successCount++;
                    } else {
                        System.out.println("  ❌ MBRCoveredBy function not found in parsed statement");
                    }
                } else {
                    System.out.println("  ❌ Statement is not a SelectStatement");
                }
                
            } catch (Exception e) {
                System.out.println("  ❌ Parsing failed: " + e.getMessage());
            }
            
            System.out.println();
        }
        
        System.out.println("=== RESULTS ===");
        System.out.println("Successful parses: " + successCount + "/" + totalTests);
        if (successCount == totalTests) {
            System.out.println("✅ MBRCoveredBy function is FULLY SUPPORTED by current ShardingSphere parser");
        } else if (successCount > 0) {
            System.out.println("⚠️ MBRCoveredBy function is PARTIALLY SUPPORTED by current ShardingSphere parser");
        } else {
            System.out.println("❌ MBRCoveredBy function is NOT SUPPORTED by current ShardingSphere parser");
            System.out.println("   This function would need to be added to the grammar and parser configuration");
        }
        
        System.out.println();
        System.out.println("MBRCoveredBy() is a MySQL spatial function that:");
        System.out.println("- Returns 1 if the MBR of g1 is covered by the MBR of g2");
        System.out.println("- Returns 0 otherwise");
        System.out.println("- Takes two geometry arguments (g1, g2)");
        System.out.println("- Can be used in SELECT clauses and WHERE conditions");
    }
    
    /**
     * Helper method to check if MBRCoveredBy function exists in the statement
     */
    private static boolean findMBRCoveredByFunction(SelectStatement select) {
        // This is a simplified check - in a real implementation,
        // you'd traverse the entire expression tree recursively
        try {
            String sqlText = select.toString().toLowerCase();
            return sqlText.contains("mbrcoveredby");
        } catch (Exception e) {
            return false;
        }
    }
}