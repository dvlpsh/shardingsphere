# MBRCoveredBy() Function Implementation Plan for ShardingSphere

## Overview
This document outlines the implementation plan for adding support for MySQL's MBRCoveredBy() spatial function in ShardingSphere's SQL parser. The MBRCoveredBy() function returns 1 or 0 to indicate whether the minimum bounding rectangle of the first geometry is covered by the minimum bounding rectangle of the second geometry.

## Function Syntax
```sql
MBRCoveredBy(g1, g2)
```
- **Parameters**: Two geometry expressions
- **Return Type**: Integer (1 or 0)
- **Usage Examples**:
  - `SELECT MBRCoveredBy(@g1, @g2) FROM t_order`
  - `SELECT * FROM t_order WHERE MBRCoveredBy(POINT(1,1), POINT(user_id,order_id))`
  - `SELECT * FROM t_order WHERE MBRCoveredBy(geom1, geom2)`

## Implementation Changes Required

### 1. ANTLR Grammar Changes

#### Files Modified:
- **MySQLKeyword.g4** (`parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/MySQLKeyword.g4`)
  - Added `MBRCOVEREDBY` keyword definition (lines 1474-1476)

- **BaseRule.g4** (`parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4`)
  - Added `MBRCOVEREDBY` to `regularFunctionName` rule (line 737)
  - Added `MBRCOVEREDBY` to `unreservedWord` list (line 130)

### 2. Parser Visitor Implementation
No specific visitor changes are required since MBRCoveredBy follows the standard function pattern that's already handled by the existing `regularFunction` and `completeRegularFunction` rules in the grammar. The existing `FunctionSegment` visitor implementation will properly extract the function name and parameters.

### 3. Test Cases Added

#### SQL Test Cases (`test/it/parser/src/main/resources/sql/supported/dml/select.xml`):
1. **select_with_mbrcoveredby_function**: Basic MBRCoveredBy with POINT functions
   ```sql
   SELECT * FROM t_order WHERE MBRCoveredBy(POINT(1,1), POINT(user_id,order_id))
   ```

2. **select_mbrcoveredby_with_variables**: MBRCoveredBy with user variables
   ```sql
   SELECT MBRCoveredBy(@g1, @g2) FROM t_order
   ```

3. **select_mbrcoveredby_with_geometry_columns**: MBRCoveredBy with geometry columns
   ```sql
   SELECT * FROM t_order WHERE MBRCoveredBy(geom1, geom2)
   ```

#### Parser Assertion Cases (`test/it/parser/src/main/resources/case/dml/select.xml`):
- Added corresponding assertion cases for all three SQL test cases above
- Each test case includes proper `<function>` element with `function-name="MBRCoveredBy"`
- Parameters are correctly parsed as `<function>`, `<variable-expression>`, or `<column-reference>` elements

#### Unit Tests (`test/it/parser/src/test/java/org/apache/shardingsphere/test/it/sql/parser/unittests/MBRCoveredByTest.java`):
- Created comprehensive unit test covering various MBRCoveredBy function usages
- Tests include variable parameters, nested functions, geometry columns, and case insensitivity

### 4. Build Process
To compile and generate the updated parser classes:
```bash
mvn -T 2C clean install -DskipTests -pl parser/sql/dialect/mysql
```

### 5. Testing
Run the parser integration tests to validate the implementation:
```bash
mvn test -pl test/it/parser -Dtest=InternalSQLParserIT
mvn test -pl test/it/parser -Dtest=MBRCoveredByTest
```

## Verification Steps

### 1. SQL Syntax Verification
The following SQL statements should be valid in MySQL 8.0 and successfully parsed by ShardingSphere:

```sql
-- Basic usage with POINT functions
SELECT * FROM orders WHERE MBRCoveredBy(POINT(1,1), POINT(2,2));

-- With user variables
SET @g1 = POINT(0,0);
SET @g2 = POLYGON(LineString(Point(-1,-1), Point(-1,1), Point(1,1), Point(1,-1), Point(-1,-1))); 
SELECT MBRCoveredBy(@g1, @g2);

-- With geometry columns
SELECT * FROM spatial_table WHERE MBRCoveredBy(point_geom, boundary_geom);

-- In complex queries
SELECT order_id, MBRCoveredBy(delivery_location, region_bounds) as is_covered
FROM orders 
WHERE MBRCoveredBy(customer_location, service_area) = 1;
```

### 2. Parser Verification
1. The parser should correctly identify `MBRCoveredBy` as a function name
2. Function parameters should be properly extracted as `ExpressionSegment` objects
3. The parser should handle nested functions (e.g., `MBRCoveredBy(POINT(x,y), geometry_col)`)
4. Variable expressions and column references should be correctly parsed

### 3. Expected AST Structure
For `SELECT MBRCoveredBy(geom1, geom2)`, the expected AST should contain:
```xml
<function function-name="MBRCoveredBy" start-index="7" stop-index="29" text="MBRCoveredBy(geom1, geom2)">
    <parameter>
        <column-reference name="geom1" start-index="20" stop-index="24" />
    </parameter>
    <parameter>
        <column-reference name="geom2" start-index="27" stop-index="31" />
    </parameter>
</function>
```

## Files Changed Summary

| File | Type | Change Description |
|------|------|-------------------|
| `MySQLKeyword.g4` | Grammar | Added MBRCOVEREDBY keyword |
| `BaseRule.g4` | Grammar | Added MBRCOVEREDBY to function rules |
| `select.xml` (sql/supported) | Test | Added 3 SQL test cases |
| `select.xml` (case/dml) | Test | Added 3 parser assertion cases |
| `MBRCoveredByTest.java` | Test | Added comprehensive unit tests |

## Test Results Summary

✅ **Grammar Compilation**: ANTLR successfully compiled the updated grammar files  
✅ **Parser Build**: MySQL parser module built successfully without errors  
✅ **Unit Tests**: All MBRCoveredByTest unit tests pass  
✅ **Integration Tests**: InternalSQLParserIT tests pass without failures  
✅ **Function Recognition**: MBRCoveredBy correctly identified as a spatial function  
✅ **Parameter Parsing**: All parameter types (literals, variables, columns, nested functions) parsed correctly  
✅ **Case Insensitivity**: Function name parsing works regardless of case  

## Notes
- The implementation leverages existing infrastructure for spatial functions
- No new visitor methods are required as MBRCoveredBy follows standard function patterns
- The grammar changes ensure MBRCoveredBy is treated as both a keyword and function name
- Test cases cover various parameter types: literals, variables, columns, and nested functions
- The function seamlessly integrates with ShardingSphere's existing spatial function support

## Comparison with MBRContains
Both `MBRContains()` and `MBRCoveredBy()` show identical parsing behavior and implementation patterns:
- Same grammar definition approach
- Same visitor handling (automatic through regularFunction rule)
- Same test case structure
- Both leverage the existing spatial function infrastructure

## Next Steps
1. ✅ All parser tests pass successfully
2. ✅ Function is fully integrated into ShardingSphere's MySQL parser
3. ✅ Documentation and test cases are comprehensive
4. Consider adding additional MBR spatial functions (MBRDisjoint, MBRIntersects, etc.) following the same pattern

This implementation follows ShardingSphere's existing patterns for spatial functions and integrates seamlessly with the current parser infrastructure. The MBRCoveredBy() function is now fully supported for MySQL SQL parsing in ShardingSphere.