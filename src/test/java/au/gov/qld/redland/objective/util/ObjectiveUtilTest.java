/**
 * 
 */
package au.gov.qld.redland.objective.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author danielma
 *
 */
public class ObjectiveUtilTest {
   
    @Test
    public void testGetOjiSession() {
	try {
	    assertNotNull(ObjectiveUtil.getOjiPRDSession());
	} catch (Exception e) {
	    fail(e.getMessage());
	}
    }
}
