package io.sim;

import java.util.Objects;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Route {
    private String id;
    private ArrayList<String> edges;
    private double departTime;

    @JsonCreator
    public Route(@JsonProperty("id") String id, @JsonProperty("edges") ArrayList<String> edges,
            @JsonProperty("departTime") double departTime) {
        this.id = id;
        this.edges = edges;
        this.departTime = departTime;
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getEdges() {
        return edges;
    }

    public double getDepartTime() {
        return departTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Route route = (Route) o;
        return Double.compare(route.departTime, departTime) == 0 &&
                Objects.equals(id, route.id) &&
                Objects.equals(edges, route.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, edges, departTime);
    }

    @Override
    public String toString() {
        return "Route{" +
                "id='" + id + '\'' +
                ", edges=" + edges +
                ", departTime=" + departTime +
                '}';
    }
}