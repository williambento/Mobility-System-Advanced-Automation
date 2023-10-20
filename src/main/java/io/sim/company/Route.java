package io.sim.company;

import java.io.Serializable;

public class Route implements Serializable{

    private String idRoute;
    private String edges;

    public Route(String _routeID, String _edges){
        this.idRoute = _routeID;
        this.edges = _edges;
    }
    
    public String getRouteID(){
        return idRoute;
    }

    public String getEdges(){
        return edges;
    }

    //uso interno
    @Override
    public String toString() {
        return idRoute + "," + edges;
    }
}
