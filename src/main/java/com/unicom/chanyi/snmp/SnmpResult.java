package com.unicom.chanyi.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

public class SnmpResult {
    OID oid;
    Variable variable;

    @Override
    public String toString() {
        return "SnmpResult{" +
                "oid=" + oid +
                ", variable=" + variable +
                '}';
    }

    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public SnmpResult(OID oid, Variable variable) {

        this.oid = oid;
        this.variable = variable;
    }
}
