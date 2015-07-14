package tops.drawing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tops.drawing.symbols.Circle;

public class TestCartoon {
	
	/**
	 * Newly created cartoons should have terminii.
	 */
	@Test
	public void testCreateCartoon() {
		Cartoon cartoon = new Cartoon();
		assertEquals("Expect exactly two terminal symbols", 2, cartoon.numberOfSSESymbols());
		assertEquals("Terminii should be connected", 1, cartoon.numberOfConnectors());
	}
	
	/**
	 * Test adding a symbol without selected connector.
	 */
	@Test
	public void testAddWithoutSelectedConnector() {
		Cartoon cartoon = new Cartoon();
		cartoon.addSSESymbol(new Circle(1, true));
		assertEquals("Three symbols expected", 3, cartoon.numberOfSSESymbols());
	}
	
	/**
	 * Test adding a symbol with selected connector.
	 */
	@Test
	public void testAddWithSelectedConnector() {
		Cartoon cartoon = new Cartoon();
		cartoon.setSelectedConnector(cartoon.getCartoonConnector(0));
		cartoon.addSSESymbol(new Circle(1, true));
		assertEquals("Three symbols expected", 3, cartoon.numberOfSSESymbols());
	}
	
	

}
