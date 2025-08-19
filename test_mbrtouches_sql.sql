-- Test SQL examples for MBRTouches() function
-- Based on MySQL documentation: https://dev.mysql.com/doc/refman/8.0/en/spatial-relation-functions-mbr.html#function_mbrtouches

-- Basic usage with column references
SELECT MBRTouches(g1.geom, g2.geom) FROM geometry_table g1, geometry_table g2;

-- Usage with table and column names that should be extracted
SELECT id, name FROM locations WHERE MBRTouches(boundary, ST_GeomFromText('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))'));

-- Usage in JOIN conditions
SELECT l1.name, l2.name 
FROM locations l1 
JOIN locations l2 ON MBRTouches(l1.boundary, l2.boundary);

-- Usage with nested function calls
SELECT * FROM regions WHERE MBRTouches(region_boundary, ST_Buffer(point_location, 100));

-- Usage in WHERE clause with multiple conditions
SELECT * FROM spatial_data 
WHERE MBRTouches(geom_column, @spatial_param) 
AND status = 'active';