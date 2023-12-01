package io.sim;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Itinerary {

	private boolean on;
	private String idItinerary;
	private Route route;

	// Construtor padrão vazio necessário para a desserialização do JSON
	public Itinerary() {
	}

	@JsonCreator
	public Itinerary(@JsonProperty("on") boolean on,
			@JsonProperty("route") Route route,
			@JsonProperty("iditinerary") String idItinerary) {
		this.on = on;
		this.route = route;
		this.idItinerary = idItinerary;
	}

	public String getIDItinerary() {
		return this.idItinerary;
	}

	public Route getRoute() {
		return this.route;
	}

	public boolean isOn() {
		return this.on;
	}

	public void setroute(Route rota) {
		this.route = rota;
	}
}
