package fi.helsinki.cs.tmc.data.json;

import fi.helsinki.cs.tmc.data.SubmissionResult;
import fi.helsinki.cs.tmc.utilities.json.parsers.JSONSubmissionResultParser;
import static fi.helsinki.cs.tmc.data.SubmissionResult.Status.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class JSONSubmissionResultParserTest {
    
    private SubmissionResult parse(String json) {
        return JSONSubmissionResultParser.parseJson(json);
    }
    
    @Test
    public void testOk() {
        String input = "{status: \"ok\"}";
        
        SubmissionResult result = parse(input);
        
        assertEquals(OK, result.getStatus());
        assertNull(result.getError());
        assertTrue(result.getTestFailures().isEmpty());
    }
    
    @Test
    public void testError() {
        String input = "{status: \"error\", error: \"Failed to compile.\"}";
        
        SubmissionResult result = parse(input);
        
        assertEquals(ERROR, result.getStatus());
        assertEquals("Failed to compile.", result.getError());
        assertTrue(result.getTestFailures().isEmpty());
    }
    
    @Test
    public void testFail() {
        String input = "{status: \"fail\", test_failures: [\"one\", \"two\"]}";
        
        SubmissionResult result = parse(input);
        
        assertEquals(FAIL, result.getStatus());
        assertNull(result.getError());
        assertEquals(2, result.getTestFailures().size());
        assertEquals("one", result.getTestFailures().get(0));
        assertEquals("two", result.getTestFailures().get(1));
    }
}
