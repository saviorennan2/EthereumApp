package br.ufrn.labcomu.ethApp;

import java.io.Serializable;

public class PatientMedicalRecordTransaction implements Serializable{
    private static final long serialVersionUID = 1L;

    private String from;

    private String to;

    private String addresses;

    private String operations;

    private String type;

    private Integer expiresIn;

    private String others;

    public PatientMedicalRecordTransaction(String from, String to, String addresses, String operations, String type,
                                           Integer expiresIn, String others) {
        this.from = from;
        this.to = to;
        this.addresses = addresses;
        this.operations = operations;
        this.type = type;
        this.expiresIn = expiresIn;
        this.others = others;
    }

    public String getFrom() {
        return this.from;
    }

    public String getTo() {
        return this.to;
    }

    public String getAddresses() {
        return addresses;
    }

    public String getOperations() {
        return operations;
    }

    public String getType() {
        return type;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getOthers() {
        return others;
    }

    @Override
    public String toString() {
        return "PatientMedicalRecordTransaction [" +
                "From=" + getFrom()  +
                ", To=" + getTo() +
                ", Addresses=" + getAddresses() +
                ", Operations=" + getOperations() +
                ", Type=" + getType() +
                ", ExpiresIn=" + getExpiresIn() +
                ", Others=" + getOthers() +
                "]";
    }
}