
package eu.europa.ec.fisheries.schema.exchange.module.v1;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.6
 * 2015-08-28T14:54:53.342+02:00
 * Generated source version: 2.7.6
 */

@WebFault(name = "exchangeFault", targetNamespace = "urn:common.exchange.schema.fisheries.ec.europa.eu:v1")
public class ExchangeFault extends Exception {
    
    private eu.europa.ec.fisheries.schema.exchange.common.v1.ExchangeFault exchangeFault;

    public ExchangeFault() {
        super();
    }
    
    public ExchangeFault(String message) {
        super(message);
    }
    
    public ExchangeFault(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeFault(String message, eu.europa.ec.fisheries.schema.exchange.common.v1.ExchangeFault exchangeFault) {
        super(message);
        this.exchangeFault = exchangeFault;
    }

    public ExchangeFault(String message, eu.europa.ec.fisheries.schema.exchange.common.v1.ExchangeFault exchangeFault, Throwable cause) {
        super(message, cause);
        this.exchangeFault = exchangeFault;
    }

    public eu.europa.ec.fisheries.schema.exchange.common.v1.ExchangeFault getFaultInfo() {
        return this.exchangeFault;
    }
}
