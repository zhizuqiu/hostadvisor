package com.unicom.chanyi.snmp;


import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SnmpTools {



    private Snmp snmp;
    private String ip;
    private String port;
    private CommunityTarget target;


    public SnmpTools(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void init() throws IOException {
        if (this.target == null) {
            this.target = defaultCommunityTarget();
        }
        if (this.snmp == null) {
            this.snmp = new Snmp(new DefaultUdpTransportMapping());
        }
        this.snmp.listen();
    }

    public void close() throws IOException {
        if (this.snmp != null)
            this.snmp.close();
    }

    private CommunityTarget defaultCommunityTarget() {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));//snmpv2的团体名
        target.setVersion(SnmpConstants.version2c);  //snmp版本
        target.setAddress(new UdpAddress(this.ip +"/"+ this.port));
        target.setTimeout(60000); //时延
        target.setRetries(1); //重传
        return target;
    }

    public void setTarget(CommunityTarget target) {
        this.target = target;
    }

    public List<SnmpResult> getList(OID oid) throws IOException {

        List<SnmpResult> result = new ArrayList<SnmpResult>();
        TableUtils utils = new TableUtils(this.snmp, new DefaultPDUFactory(PDU.GETBULK));
        utils.setMaxNumRowsPerPDU(5);
        OID[] columnOids = new OID[]{oid};
        List<TableEvent> l = utils.getTable(this.target, columnOids, null, null); //
        for (TableEvent e : l) {
            VariableBinding[] vbs = e.getColumns();
            for (int i = 0; i < vbs.length; i++) {
                OID vbs_oid = vbs[i].getOid();
                Variable variable = vbs[i].getVariable();
                result.add(new SnmpResult(vbs_oid, variable));
            }
        }
        return result;
    }


    public List<SnmpResult> getOne(List<OID> oids) throws IOException, SnmpToolsException {

        List<SnmpResult> list = new ArrayList<SnmpResult>();
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);

        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        ResponseEvent responseEvent = this.snmp.send(pdu, this.target);
        PDU response = responseEvent.getResponse();

        if (response == null) {
            throw new SnmpToolsException("TimeOut...");
        } else {
            if (response.getErrorStatus() == PDU.noError) {
                Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                for (VariableBinding variableBinding : vbs) {
                    list.add(new SnmpResult(variableBinding.getOid(), variableBinding.getVariable()));
                }
            } else {
                throw new SnmpToolsException("Error:" + response.getErrorStatusText());
            }
        }
        return list;
    }


}
