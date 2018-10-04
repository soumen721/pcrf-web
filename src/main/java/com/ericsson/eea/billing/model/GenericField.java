package com.ericsson.eea.billing.model;

public class GenericField {

    private String name;
    private Object value;

    private GenericFieldType type;
    private GenericDataStructureType datStructureType; // Should be present if Type is MAP or LIST

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenericFieldType getType() {
        return type;
    }

    public void setType(GenericFieldType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public GenericDataStructureType getDatStructureType() {
        return datStructureType;
    }

    public void setDatStructureType(GenericDataStructureType datStructureType) {
        this.datStructureType = datStructureType;
    }



}
