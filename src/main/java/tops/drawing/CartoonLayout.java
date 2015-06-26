package tops.drawing;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tops.drawing.model.Helix;
import tops.drawing.model.SSE;
import tops.drawing.model.TSE;
import tops.drawing.model.Topology;
import tops.drawing.symbols.Circle;
import tops.drawing.symbols.EquilateralTriangle;
import tops.drawing.symbols.SSESymbol;

public class CartoonLayout {
	
	private int sseSeparation;
	private int layerSeparation;
	private int sseRadius;
	private int startX;
	private int startY;
	
	public CartoonLayout() {
		this.sseSeparation = 50;
		this.layerSeparation = 60;
		this.sseRadius = 20;
		this.startX = this.layerSeparation;
		this.startY = this.layerSeparation;
	}
	
	public Cartoon layout(Topology topology) {
		Cartoon cartoon = new Cartoon();
		
		int x = this.startX;
		int y = this.startY;
		HashMap<Integer, SSESymbol> symbolMap = new HashMap<Integer, SSESymbol>();
		
		List<TSE> tses = topology.getTSES();
		for (TSE tse : tses) {
			ArrayList<SSE> sses = tse.getSSEs();
			for (SSE sse : sses) {
				SSESymbol sseSymbol;
				int sseNumber = sse.getSSENumber();
				boolean isDown = !sse.isUp();
				
				if (sse instanceof Helix) {
					sseSymbol = new Circle(sseNumber, x, y, this.sseRadius, isDown);
					sseSymbol.recreateShape();
				} else {
					sseSymbol = new EquilateralTriangle(sseNumber, x, y, this.sseRadius, isDown);
					Rectangle bounds = sseSymbol.getShape().getBounds(); 
					int cornerY = bounds.y;
					int upperYAxis = y - (bounds.height / 2);
					if (isDown) {	// nudge upwards
						sseSymbol.move(0, -(cornerY - upperYAxis));
					} else {		// nudge downwards
						sseSymbol.move(0, upperYAxis - cornerY);
					}
				}
				
				symbolMap.put(sseNumber, sseSymbol);
				
				x += this.sseSeparation;
			}
			x = this.startX;
			y += this.layerSeparation;
		}
		
		// XXX : note that this assumes that all symbols in the range [1,N] exist!
		for (int i = 1; i <= symbolMap.keySet().size(); i++) {
			SSESymbol sseSymbol = symbolMap.get(i);
			cartoon.addSSESymbolBeforeCTerminus(sseSymbol, false);
		}
		cartoon.fixTerminalPositions();
		
		return cartoon;
	}
}
