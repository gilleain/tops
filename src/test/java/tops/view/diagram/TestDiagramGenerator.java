package tops.view.diagram;

import org.junit.Test;

import tops.view.model.Shape;

public class TestDiagramGenerator {
    
    private class PrintVisitor implements Shape.Visitor {

        @Override
        public void visit(Shape shape) {
            System.out.println(shape.getClass().getName());
        }
        
    }
    
    @Test
    public void testVerticesOnly() {
        DiagramGenerator dg = new DiagramGenerator();
        Shape shape = dg.generate("NEEC", "", "");
        shape.accept(new PrintVisitor());
    }
    
    @Test
    public void testVerticesAndEdges() {
        DiagramGenerator dg = new DiagramGenerator();
        Shape shape = dg.generate("NEEC", "1:2P", "");
        shape.accept(new PrintVisitor());
    }

}
