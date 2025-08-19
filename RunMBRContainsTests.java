package unittests;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Test runner for MBRContains functionality tests
 * Demonstrates how to run the MBRContains tests and verify the implementation
 */
public class RunMBRContainsTests {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("MBRContains Test Suite Runner");
        System.out.println("Testing ShardingSphere MySQL Parser");
        System.out.println("MBRContains() spatial function support");
        System.out.println("========================================\n");
        
        // Create launcher
        var launcher = LauncherFactory.create();
        var listener = new SummaryGeneratingListener();
        
        // Build discovery request for our test classes
        var request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                DiscoverySelectors.selectClass(SimpleMBRContainsTest.class),
                DiscoverySelectors.selectClass(MBRContainsIntegrationTest.class)
            )
            .build();
        
        // Execute tests
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        // Get results
        TestExecutionSummary summary = listener.getSummary();
        
        // Print summary
        System.out.println("\n========================================");
        System.out.println("Test Execution Summary");
        System.out.println("========================================");
        System.out.printf("Tests found: %d\n", summary.getTestsFoundCount());
        System.out.printf("Tests started: %d\n", summary.getTestsStartedCount());
        System.out.printf("Tests successful: %d\n", summary.getTestsSucceededCount());
        System.out.printf("Tests failed: %d\n", summary.getTestsFailedCount());
        System.out.printf("Tests skipped: %d\n", summary.getTestsSkippedCount());
        System.out.printf("Total time: %d ms\n", summary.getTotalTime().toMillis());
        
        if (summary.getFailures().isEmpty()) {
            System.out.println("\n🎉 ALL TESTS PASSED!");
            System.out.println("✅ MBRContains() function successfully implemented in ShardingSphere");
            System.out.println("✅ Grammar parsing works correctly");
            System.out.println("✅ AST generation handles all parameter types");
            System.out.println("✅ Integration with MySQL parser complete");
        } else {
            System.out.println("\n❌ SOME TESTS FAILED:");
            summary.getFailures().forEach(failure -> {
                System.out.printf("   - %s: %s\n", 
                    failure.getTestIdentifier().getDisplayName(), 
                    failure.getException().getMessage());
            });
        }
        
        System.out.println("\n========================================");
        System.out.println("Implementation Details");
        System.out.println("========================================");
        System.out.println("Files modified:");
        System.out.println("• parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/MySQLKeyword.g4");
        System.out.println("• parser/sql/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4");
        System.out.println("• test/it/parser/src/main/resources/sql/supported/dml/select.xml");
        System.out.println("• test/it/parser/src/main/resources/case/dml/select.xml");
        
        System.out.println("\nFunction signature:");
        System.out.println("• MBRContains(g1, g2) -> INTEGER (0 or 1)");
        System.out.println("• Returns 1 if MBR of g1 contains MBR of g2, 0 otherwise");
        
        System.out.println("\nSupported parameter types:");
        System.out.println("• Geometry columns: MBRContains(geom1, geom2)");
        System.out.println("• User variables: MBRContains(@g1, @g2)");
        System.out.println("• POINT functions: MBRContains(POINT(1,1), POINT(2,2))");
        System.out.println("• Mixed types: MBRContains(boundary, POINT(x, y))");
        System.out.println("• Parameter markers: MBRContains(?, ?)");
        
        System.out.println("\n========================================");
        
        // Exit with appropriate code
        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}

/**
 * Alternative simple test runner without JUnit Platform dependencies
 * This can be used if JUnit Platform is not available
 */
class SimpleMBRContainsTestRunner {
    
    public static void runBasicTests() {
        System.out.println("Running basic MBRContains tests...");
        
        try {
            // Create test instance
            SimpleMBRContainsTest test = new SimpleMBRContainsTest();
            test.setUp();
            
            // Run a few key tests
            System.out.print("Testing MBRContains with POINT functions... ");
            test.testMBRContainsWithPointFunctions();
            System.out.println("✅");
            
            System.out.print("Testing MBRContains with geometry columns... ");
            test.testMBRContainsWithGeometryColumns();
            System.out.println("✅");
            
            System.out.print("Testing MBRContains with user variables... ");
            test.testMBRContainsWithUserVariables();
            System.out.println("✅");
            
            System.out.print("Testing case insensitive parsing... ");
            test.testCaseInsensitiveMBRContains();
            System.out.println("✅");
            
            System.out.println("\n🎉 Basic tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Basic tests failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        if (args.length > 0 && "simple".equals(args[0])) {
            runBasicTests();
        } else {
            System.out.println("Use 'java RunMBRContainsTests simple' for simple test runner");
            System.out.println("Or run the main method for full JUnit Platform execution");
        }
    }
}