package com.automahome.canbus.mvc;

import java.util.List;

import com.automahome.canbus.Main;
import com.automahome.canbus.Values;

/**
 * Recebe os intents do BTService e trata as Strings, atualiza as views (Main
 * activity) e gerencia nosso modelo (CanNode).
 * 
 * @author vhorta
 * 
 */
public class EngineController {
    public void handleIncomingDataFromMaster(byte[] encodedBytes) {
        if (encodedBytes[0] == Values.INSTRUCT_UPDATE) {
            // 1. Instanciando valores da mensagem
            byte tempType = encodedBytes[1];
            byte tempId = encodedBytes[2];
            byte[] tempStatus = new byte[Values.NODE_QTD_STATUS];

            for (int i = 0; i < tempStatus.length; i++) {
                tempStatus[i] = encodedBytes[3 + i];
            }

            // 2. Checando se já existe o nó no sistema e dando refresh
            CanNode tempNode;

            int index = getCanIndexById(tempId);

            if (index < 0) { // eh um novo elemento!
                tempNode = new CanNode(tempId, tempType);
                tempNode.setStatus(tempStatus);
                tempNode.getMyType().refreshStatus(tempNode);
                Values.canNodes.add(tempNode);
            } else { // devemos atualizar o elemento!
                Values.canNodes.get(index).setStatus(tempStatus);
                Values.canNodes.get(index).getMyType().refreshStatus(Values.canNodes.get(index));
                tempNode = Values.canNodes.get(index); 
            } 

            // 3. Notificando dataset TODO: notifydataset
            Main.atualizarListView(tempNode.getMyType().getTypeName(), tempNode.getStatusText(), tempNode.getMyType().getIconResource());

        }
    }

    public void updateListView() {

    }

    public void updateModel(List<CanNode> allNodes) {

    }

    public void requestUpdateToMaster() {

    }

    public int getCanIndexById(byte id) {
        for (CanNode _item : Values.canNodes) {
            if (_item.getMyId() == id)
                return Values.canNodes.indexOf(_item);
        }
        return -1;
    }

}
