package com.unicom.chanyi.influxdb;


public class InsertValue {
    private String table;
    private String value;

    @Override
    public String toString() {
        return "InsertValue{" +
                "table='" + table + '\'' +
                ", value=" + value +
                '}';
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public InsertValue() {

    }

    public InsertValue(String table, String value) {

        this.table = table;
        this.value = value;
    }
}
