package br.com.simulatorAppServer.utils;

import br.com.simulatorAppServer.models.ServerlessFunctionModel;

import java.util.Comparator;

public class ServerlessFunctionSortingComparator implements Comparator<ServerlessFunctionModel> {
    @Override
    public int compare(ServerlessFunctionModel serverlessFunctionModel, ServerlessFunctionModel t1) {
        //ta parando aqui - NPE - talvez perguntar se valor é null
        if(serverlessFunctionModel.getCurrentTime() > t1.getCurrentTime())
            return 1;
        else if(serverlessFunctionModel.getCurrentTime() < t1.getCurrentTime())
            return -1;
        return 0;
    }
}
