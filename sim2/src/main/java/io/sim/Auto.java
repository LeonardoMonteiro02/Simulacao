package io.sim;

import de.tudresden.sumo.cmd.Vehicle;
import java.util.ArrayList;

import org.antlr.grammar.v3.DefineGrammarItemsWalker.dotLoop_return;
import org.python.modules.synchronize;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auto extends Thread {

	private String idAuto;
	private SumoColor colorAuto;
	private String driverID;
	private SumoTraciConnection sumo;
	private double odometro;
	private double odometroantes;
	private double odometroatual;
	private boolean on_off;
	private long acquisitionRate;
	private int fuelType; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; // price in liters
	private int personCapacity; // the total number of persons that can ride in this vehicle
	private int personNumber; // the total number of persons which are riding in this vehicle

	private ArrayList<DrivingData> drivingRepport;

	public Auto(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo,
			long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) {

		this.on_off = _on_off;
		this.idAuto = _idAuto;
		this.colorAuto = _colorAuto;
		this.driverID = _driverID;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;

		if ((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}

		if ((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}

		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.drivingRepport = new ArrayList<DrivingData>();
	}

	@Override
	public void run() {
		System.out.println("Auto entrei");
		while (this.on_off) {
			try {
				// Auto.sleep(this.acquisitionRate);
				this.atualizaSensores();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void atualizaSensores() {

		try {

			if (!this.getSumo().isClosed()) {
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
				String formattedDateTime = now.format(formatter);

				String idRoute = (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto));
				double velocidade = (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto));
				double distancia = (double) sumo.do_job_get(Vehicle.getDistance(this.idAuto));
				double consumo = (double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto));
				int tipo = getFuelType();
				double cO2 = (double) sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto));

				System.out.println("************************");
				System.out.println("Timestamp atual: " + formattedDateTime);
				System.out.println("AutoID: " + this.getIdAuto());
				System.out.println("RouteID: " + idRoute);
				System.out.println("Velocidade: " + velocidade);
				System.out.println("Distancia: " + distancia);
				System.out.println("Consumo: " + consumo);
				System.out.println("Tipo de combustivel: " + tipo);
				System.out.println("Nivel de CO2: " + cO2);
				System.out.println("Posição em x: " + sumoPosition2D.x);
				System.out.println("Posição em y: " + sumoPosition2D.y);

				System.out.println("************************");

			} else {
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resetodometros() {
		this.odometroantes = 0;
		this.odometroatual = 0;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdAuto() {
		return this.idAuto;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if ((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	public SumoColor getColorAuto() {
		return this.colorAuto;
	}

	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	public void setFuelPreferential(int _fuelPreferential) {
		if ((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}
}