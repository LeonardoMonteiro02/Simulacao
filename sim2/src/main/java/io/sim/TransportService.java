package io.sim;

import java.util.ArrayList;

import org.antlr.grammar.v3.ANTLRParser.elementNoOptionSpec_return;
import org.python.antlr.ast.boolopType;
import org.python.core.exceptions;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.util.SumoCommand;
import it.polito.appeal.traci.SumoTraciConnection;

public class TransportService extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;

	public TransportService(boolean _on_off, String _idTransportService, SumoTraciConnection _sumo) {

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;

		this.sumo = _sumo;
	}

	@Override
	public void run() {
		// Inicializa as rotas iniciais
		System.out.println("Inicia simulação");

		try {
			while (on_off) {
				sumo.do_timestep();
				Thread.sleep(100);
				if (this.getSumo().isClosed()) {
					this.on_off = false;
					System.out.println("SUMO is closed...");
				}
			}
		} catch (Exception e) {
			System.out.println("Falha de simulação" + e.getMessage());

		}

	}

	public void stopSimulation() {
		this.on_off = false;
	}

	public boolean isOn_off() {
		return on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

}