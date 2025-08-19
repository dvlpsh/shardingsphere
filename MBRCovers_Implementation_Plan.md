# MBRCovers() Function Implementation Plan for ShardingSphere MySQL Parser

## Overview
This document outlines the complete implementation of the MBRCovers() spatial function support in ShardingSphere's MySQL SQL parser, following the same pattern as existing MBRContains() and MBRCoveredBy() functions.

## Background
MBRCovers() is a MySQL spatial function that tests whether the Minimum Bounding Rectangle (MBR) of one geometry completely covers the MBR of another geometry. According to MySQL documentation:
- **Syntax**: `MBRCovers(g1, g2)`
- **Returns**: 1 if the MBR of g1 covers the MBR of g2, 0 otherwise
- **Parameters**: Two geometry expressions (columns, variables, functions)

## Implementation Summary

### 1. ANTLR Grammar Changes ✅

#### Files Modified:
- `parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/MySQLKeyword.g4`
- `parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4`

#### Changes Made:

**MySQLKeyword.g4** - Added new keyword token:
```antlr
MBRCOVERS
    : M B R C O V E R S
    ;
```

**BaseRule.g4** - Added to two rules:
1. **regularFunctionName rule**: Added `MBRCOVERS` to allow function parsing
2. **unreservedWord rule**: Added `MBRCOVERS` to allow it as identifier when needed

### 2. Parser Compilation ✅

#### Command:
```bash
mvn -T 2C clean install -DskipTests -pl parser/sql/dialect/mysql
```

#### Generated Files:
- `MySQLStatementLexer.java` - Contains MBRCOVERS token recognition
- `MySQLStatementParser.java` - Contains parsing logic for MBRCovers function

### 3. Visitor Implementation ✅

The existing visitor infrastructure already supports MBRCovers since spatial functions are handled generically through the `FunctionSegment` mechanism. No specific visitor changes were required.

### 4. Test Case Implementation ✅

#### SQL Test Cases Added:
**File**: `test/it/parser/src/main/resources/sql/supported/dml/select.xml`

Added three test cases:
```xml
<sql-case id="select_with_mbrcovers_function" value="SELECT * FROM t_order WHERE MBRCovers(POINT(1,1), POINT(user_id,order_id))" db-types="MySQL" />
<sql-case id="select_mbrcovers_with_variables" value="SELECT MBRCovers(@g1, @g2) FROM t_order" db-types="MySQL" />
<sql-case id="select_mbrcovers_with_geometry_columns" value="SELECT * FROM t_order WHERE MBRCovers(geom1, geom2)" db-types="MySQL" />
```

#### Case Assertions Added:
**File**: `test/it/parser/src/main/resources/case/dml/select.xml`

Added corresponding assertion structures for:
1. MBRCovers with POINT functions and literal parameters
2. MBRCovers with user variable parameters (@g1, @g2)
3. MBRCovers with geometry column parameters

### 5. Unit Test Implementation ✅

#### File Created:
`test/it/parser/src/test/java/org/apache/shardingsphere/test/it/sql/parser/unittests/SimpleMBRCoversTest.java`

#### Test Coverage (14 test methods):
1. **testMBRCoversWithPointFunctions** - Function with POINT() parameters
2. **testMBRCoversWithGeometryColumns** - Function with column parameters
3. **testMBRCoversWithUserVariables** - Function with @variable parameters
4. **testMBRCoversWithQualifiedColumns** - Function with table.column parameters
5. **testMBRCoversWithMixedParameters** - Mixed parameter types
6. **testMBRCoversInSelectWithAlias** - Function in SELECT with AS alias
7. **testMBRCoversInComplexWhere** - Function in complex WHERE clauses
8. **testNestedMBRCoversWithSpatialFunctions** - Nested with other spatial functions
9. **testCaseInsensitiveMBRCovers** - Case insensitive parsing
10. **testMBRCoversPositioning** - Index positioning validation
11. **testMBRCoversWithParameterMarkers** - Prepared statement parameters (?)
12. **testInvalidMBRCoversUsage** - Error handling tests
13. **testMBRCoversReturnTypeUsage** - Return value usage patterns
14. **debugMBRCoversInfo** - Implementation verification

All tests **PASSED** ✅

## Verification Results

### Parser Integration Test
- **Status**: ✅ PASSED
- **Test Cases**: 3 MBRCovers test cases passed in MySQL parser integration tests
- **Coverage**: All SQL patterns (function calls, variables, columns) work correctly

### Unit Test Results
- **Status**: ✅ PASSED  
- **Tests Run**: 14
- **Failures**: 0
- **Errors**: 0
- **Coverage**: Comprehensive testing of all usage patterns

## Usage Examples

### Basic Usage
```sql
-- Test if region covers a point
SELECT MBRCovers(region_geometry, customer_point) FROM locations;

-- Filter records where one geometry covers another
SELECT * FROM spatial_table WHERE MBRCovers(boundary, location) = 1;
```

### With Variables
```sql
-- Using user variables
SELECT MBRCovers(@polygon, @point) FROM spatial_data;
```

### With Functions
```sql
-- Nested with other spatial functions
SELECT * FROM places WHERE MBRCovers(service_area, POINT(x_coord, y_coord));
```

### Complex Queries
```sql
-- In complex WHERE clauses
SELECT * FROM deliveries 
WHERE delivery_date > '2023-01-01' 
  AND MBRCovers(delivery_zone, customer_location) = 1;
```

## Files Changed Summary

| File Path | Type | Changes |
|-----------|------|---------|
| `parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/MySQLKeyword.g4` | Grammar | Added MBRCOVERS keyword |
| `parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4` | Grammar | Added to regularFunctionName and unreservedWord |
| `test/it/parser/src/main/resources/sql/supported/dml/select.xml` | Test Data | Added 3 SQL test cases |
| `test/it/parser/src/main/resources/case/dml/select.xml` | Test Data | Added 3 case assertion structures |
| `test/it/parser/src/test/java/org/apache/shardingsphere/test/it/sql/parser/unittests/SimpleMBRCoversTest.java` | Test | Created comprehensive unit test suite |

## Implementation Status

✅ **COMPLETED** - All tasks finished successfully

- [x] Research MBRCovers() function syntax from MySQL official documentation
- [x] Fix ANTLR grammar in .g4 files  
- [x] Compile parser module with mvn clean install
- [x] Add SQL test cases in sql/supported directory
- [x] Add case assertions in shardingsphere-test-it-parser module
- [x] Run InternalSQLParserIT tests - PASSED
- [x] Create SimpleMBRCoversTest unit test - 14 tests PASSED
- [x] Create implementation plan document

## Conclusion

The MBRCovers() function has been successfully implemented in ShardingSphere's MySQL parser following the established pattern for spatial functions. The implementation:

1. **Correctly parses** MBRCovers function calls in all contexts
2. **Extracts parameters** properly for binding and rewriting operations  
3. **Handles all parameter types** (columns, variables, functions, literals)
4. **Passes all tests** including integration and comprehensive unit tests
5. **Follows existing patterns** for consistency with other spatial functions

The implementation is production-ready and maintains full compatibility with ShardingSphere's parsing, binding, and rewriting infrastructure.