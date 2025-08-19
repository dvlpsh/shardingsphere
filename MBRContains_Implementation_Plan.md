# MBRContains() Function Implementation Plan for ShardingSphere

## Overview
This document outlines the implementation plan for adding support for MySQL's MBRContains() spatial function in ShardingSphere's SQL parser. The MBRContains() function returns 1 or 0 to indicate whether the minimum bounding rectangle of the first geometry contains the minimum bounding rectangle of the second geometry.

## Function Syntax
```sql
MBRContains(g1, g2)
```
- **Parameters**: Two geometry expressions
- **Return Type**: Integer (1 or 0)
- **Usage Examples**:
  - `SELECT MBRContains(@g1, @g2)`
  - `SELECT * FROM t_order WHERE MBRContains(POINT(1,1), POINT(user_id,order_id))`
  - `SELECT * FROM t_order WHERE MBRContains(geom1, geom2)`

## Implementation Changes Required

### 1. ANTLR Grammar Changes

#### Files Modified:
- **MySQLKeyword.g4** (`parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/MySQLKeyword.g4`)
  - Added `MBRCONTAINS` keyword definition (lines 1470-1472)

- **BaseRule.g4** (`parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4`)
  - Added `MBRCONTAINS` to `regularFunctionName` rule (line 737)
  - Added `MBRCONTAINS` to `unreservedWord` list (line 130)

### 2. Parser Visitor Implementation
No specific visitor changes are required since MBRContains follows the standard function pattern that's already handled by the existing `regularFunction` and `completeRegularFunction` rules in the grammar. The existing `FunctionSegment` visitor implementation will properly extract the function name and parameters.

### 3. Test Cases Added

#### SQL Test Cases (`test/it/parser/src/main/resources/sql/supported/dml/select.xml`):
1. **select_with_mbrcontains_function**: Basic MBRContains with POINT functions
   ```sql
   SELECT * FROM t_order WHERE MBRContains(POINT(1,1), POINT(user_id,order_id))
   ```

2. **select_mbrcontains_with_variables**: MBRContains with user variables
   ```sql
   SELECT MBRContains(@g1, @g2) FROM t_order
   ```

3. **select_mbrcontains_with_geometry_columns**: MBRContains with geometry columns
   ```sql
   SELECT * FROM t_order WHERE MBRContains(geom1, geom2)
   ```

#### Parser Assertion Cases (`test/it/parser/src/main/resources/case/dml/select.xml`):
- Added corresponding assertion cases for all three SQL test cases above
- Each test case includes proper `<function>` element with `function-name="MBRContains"`
- Parameters are correctly parsed as `<function>`, `<variable-expression>`, or `<column-reference>` elements

### 4. Build Process
To compile and generate the updated parser classes:
```bash
mvn -T 2C clean install -DskipTests -pl parser
```

### 5. Testing
Run the parser integration tests to validate the implementation:
```bash
mvn test -pl test/it/parser -Dtest=InternalSQLParserIT
```

## Verification Steps

### 1. SQL Syntax Verification
The following SQL statements should be valid in MySQL 8.0 and successfully parsed by ShardingSphere:

```sql
-- Basic usage with POINT functions
SELECT * FROM orders WHERE MBRContains(POINT(1,1), POINT(2,2));

-- With user variables
SET @g1 = POINT(0,0);
SET @g2 = POINT(1,1); 
SELECT MBRContains(@g1, @g2);

-- With geometry columns
SELECT * FROM spatial_table WHERE MBRContains(boundary_geom, point_geom);

-- In complex queries
SELECT order_id, MBRContains(region_bounds, delivery_location) as is_in_region
FROM orders 
WHERE MBRContains(service_area, customer_location) = 1;
```

### 2. Parser Verification
1. The parser should correctly identify `MBRContains` as a function name
2. Function parameters should be properly extracted as `ExpressionSegment` objects
3. The parser should handle nested functions (e.g., `MBRContains(POINT(x,y), geometry_col)`)
4. Variable expressions and column references should be correctly parsed

### 3. Expected AST Structure
For `SELECT MBRContains(geom1, geom2)`, the expected AST should contain:
```xml
<function function-name="MBRContains" start-index="7" stop-index="28" text="MBRContains(geom1, geom2)">
    <parameter>
        <column-reference name="geom1" start-index="19" stop-index="23" />
    </parameter>
    <parameter>
        <column-reference name="geom2" start-index="26" stop-index="30" />
    </parameter>
</function>
```

## Files Changed Summary

| File | Type | Change Description |
|------|------|-------------------|
| `MySQLKeyword.g4` | Grammar | Added MBRCONTAINS keyword |
| `BaseRule.g4` | Grammar | Added MBRCONTAINS to function rules |
| `select.xml` (sql/supported) | Test | Added 3 SQL test cases |
| `select.xml` (case/dml) | Test | Added 3 parser assertion cases |

## Notes
- The implementation leverages existing infrastructure for spatial functions
- No new visitor methods are required as MBRContains follows standard function patterns
- The grammar changes ensure MBRContains is treated as both a keyword and function name
- Test cases cover various parameter types: literals, variables, columns, and nested functions

## Next Steps
1. Run the parser tests to ensure all test cases pass
2. Consider adding additional MBR spatial functions (MBRDisjoint, MBRIntersects, etc.) following the same pattern
3. Update documentation if needed

This implementation follows ShardingSphere's existing patterns for spatial functions and should integrate seamlessly with the current parser infrastructure.