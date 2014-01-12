package tops.view.tops3D;

import java.util.Iterator;

public class Structure extends ProteinComponent {

    public Structure() {
        super("Structure");
    }

    public Structure(String l) {
        super(l);
    }

    @Override
    public void addSubComponent(int index, ProteinComponent sub) {
        index = index - 1; // map numberings
        this.subComponents.add(index, sub);
    }

    @Override
    public ProteinComponent getSubComponent(ProteinComponent sub) {
        int pos = this.subComponents.indexOf(sub);
        if (pos == -1) {
            return null;
        } else
            return (ProteinComponent) this.subComponents.get(pos);
    }

    @Override
    public void removeSubComponent(ProteinComponent sub) {
        int pos = this.subComponents.indexOf(sub);
        if (pos != -1)
            this.subComponents.remove(pos);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.typeLabel + " Z-plane : [" + this.order
                + "]\n");
        Iterator<ProteinComponent> i = this.subComponents.iterator();
        int k = 0;
        while (i.hasNext()) {
            ProteinComponent pc = (ProteinComponent) i.next();
            sb.append(pc.toString());
            sb.append(" sheetOrder [").append(k).append("]");
            sb.append("\n");
            k++;
        }
        return sb.toString();
    }
}
