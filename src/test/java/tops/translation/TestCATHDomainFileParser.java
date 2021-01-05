package tops.translation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import tops.translation.model.Domain;

public class TestCATHDomainFileParser {
	
	@Test
	public void testParseLineWithMultipleDomainsAndFragments() {
		final String line =  "9rubA D02 F02  1  A    5 - A  136 -  1  A  137 - A  439 -  A    2 - A    4 - (3)  A  440 - A  460 - (21)";
		List<Domain> domains = CATHDomainFileParser.parseLine(line);
		assertEquals(2, domains.size());
		
		// assumes domains are in order ...
		Domain domain1 = domains.get(0);
		assertEquals(1, domain1.getSegments().size());
		assertEquals(5, domain1.getSegments().get(0).getStart());
		assertEquals(136, domain1.getSegments().get(0).getEnd());
		
		Domain domain2 = domains.get(1);
		assertEquals(1, domain2.getSegments().size());
		assertEquals(137, domain2.getSegments().get(0).getStart());
		assertEquals(439, domain2.getSegments().get(0).getEnd());
		
	}

}
