package tops.port.model;

public class DomDefError {
    public String errorString;
    public ErrorType errorType;
    
    public DomDefError(String errorString, ErrorType errorType) {
        this.errorString = errorString;
        this.errorType = errorType;
    }
    
    public boolean isOk() {
        return errorType == ErrorType.NO_DOMAIN_ERRORS;
    }
}
