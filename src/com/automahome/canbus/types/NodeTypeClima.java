package com.automahome.canbus.types;

import java.util.ArrayList;
import java.util.Arrays;

import com.automahome.canbus.R;
import com.automahome.canbus.Values;
import com.automahome.canbus.mvc.CanNode;

/**
 * Lampada:
 * NODE_STATUS[0]: estado da lampada
 *                  - ligado:    0x01
 *                  - desligado: 0x02
 * NODE_STATUS[1]: cor da lampada
 *                  - R: 0x01
 *                  - G: 0x02
 *                  - B: 0x03
 *                  - W: 0x04
 * NODE_STATUS[2]: sem uso
 * 
 * @author vhorta
 *
 */
public class NodeTypeClima extends NodeType {
    
    public NodeTypeClima(){
        this.iconResource = R.drawable.ic_clima;
        this.typeCode = Values.TYPE_CLIMA;
        this.typeName = "Climatizador";
        this.comandos = new ArrayList<Comando>();
       
        comandos.add(new Comando("ON",        (byte)0x00, (byte)0x01));
        comandos.add(new Comando("OFF",       (byte)0x00, (byte)0x02));
        comandos.add(new Comando("Manual",    (byte)0x01, (byte)0x00));
        comandos.add(new Comando("Auto 30",   (byte)0x01, (byte)0x1E));
        comandos.add(new Comando("Auto 25",   (byte)0x01, (byte)0x19));
        comandos.add(new Comando("Auto 20",   (byte)0x01, (byte)0x14));
    }
    
    @Override
    public void refreshStatus(CanNode canNode) {
        byte[] tempStatus = Arrays.copyOf(canNode.getStatus(), canNode.getStatus().length);
        StringBuffer tempStatusString = new StringBuffer();
        
        switch(tempStatus[0]) {
        case 0x01:
            tempStatusString.append("ON");
            break;
        case 0x02:
            tempStatusString.append("OFF");
            
            break;
        }
        
        if (tempStatus[0] == 0x01) {
            switch (tempStatus[1]) {
            case 0x00:
                tempStatusString.append(" - Man");
                break;
            case 0x14:
                tempStatusString.append(" - A20");
                break;
            case 0x19:
                tempStatusString.append(" - A25");
                break;
            case 0x1E:
                tempStatusString.append(" - A30");
                break;
            }
        }
        tempStatusString.append(" - " + Byte.toString(tempStatus[2]));
        
        canNode.setStatusText(tempStatusString.toString());
    }

}
