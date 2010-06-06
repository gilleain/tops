package tops.view.tops2D.app;

/** 
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class TableMap extends AbstractTableModel implements TableModelListener {

    protected TableModel model;

    public TableModel getModel() {
        return this.model;
    }

    public void setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }

    // By default, implement TableModel by forwarding all messages
    // to the model.

    public Object getValueAt(int aRow, int aColumn) {
        return this.model.getValueAt(aRow, aColumn);
    }

    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        this.model.setValueAt(aValue, aRow, aColumn);
    }

    public int getRowCount() {
        return (this.model == null) ? 0 : this.model.getRowCount();
    }

    public int getColumnCount() {
        return (this.model == null) ? 0 : this.model.getColumnCount();
    }

    @Override
    public String getColumnName(int aColumn) {
        return this.model.getColumnName(aColumn);
    }

    @Override
    public Class getColumnClass(int aColumn) {
        return this.model.getColumnClass(aColumn);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return this.model.isCellEditable(row, column);
    }

    //
    // Implementation of the TableModelListener interface,
    //
    // By default forward all events to all the listeners.
    public void tableChanged(TableModelEvent e) {
        this.fireTableChanged(e);
    }
}
