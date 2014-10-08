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
public class NodeTypeLampada extends NodeType {
    
    public NodeTypeLampada(){
        this.iconResource = R.drawable.ic_lightbulb;
        this.typeCode = Values.TYPE_LAMP;
        this.typeName = "Lâmpada";
        this.comandos = new ArrayList<Comando>();
        comandos.add(new Comando("ON",  (byte)0x00, (byte)0x01));
        comandos.add(new Comando("OFF", (byte)0x00, (byte)0x02));
        comandos.add(new Comando("R",   (byte)0x01, (byte)0x01));
        comandos.add(new Comando("G",   (byte)0x01, (byte)0x02));
        comandos.add(new Comando("B",   (byte)0x01, (byte)0x03));
        comandos.add(new Comando("W",   (byte)0x01, (byte)0x04));
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
            case 0x01:
                tempStatusString.append(" - R");
                break;
            case 0x02:
                tempStatusString.append(" - G");
                break;
            case 0x03:
                tempStatusString.append(" - B");
                break;
            }
        }
        
        canNode.setStatusText(tempStatusString.toString());
    }

}
