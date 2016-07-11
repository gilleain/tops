package tops.port.calculate;

import tops.port.model.Chain;

public interface Calculation {
    
    public void calculate(Chain chain);
    
    public void setParameter(String key, double value);

}
