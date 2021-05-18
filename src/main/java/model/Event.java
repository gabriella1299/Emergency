package model;

import java.time.LocalTime;

public class Event implements Comparable<Event>{
	
	enum EventType{//CAUSANO VARIAZIONE STATO PAZIENTE
		
		ARRIVAL, //arriva nuovo paziente, entra in triage (per hp fatta in 'Patient'
		TRIAGE, //finito il triage, entro in sala d'attesa (con un colore)
		TIMEOUT, //passa un certo tempo di attesa
		FREE_STUDIO, //si e' liberato uno studio, chiamiamo qualcuno (qualcuno puo' essere ammesso)
		TREATED, //paziente CURATO
		TICK //timer per controllare se ci sono studi liberi (se rimane vuoto per piu di tot minuti passo un paziente al free studio)
	};
	
	private LocalTime time;
	private EventType type;
	private Patient patients;//devo far si' che l'evento si ricordi del tipo di paziente
	
	public Event(LocalTime time, EventType type, Patient patients) {
		super();
		this.time = time;
		this.type = type;
		this.patients = patients;
	}
	
	public LocalTime getTime() {
		return time;
	}
	public void setTime(LocalTime time) {
		this.time = time;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	
	public Patient getPatients() {
		return patients;
	}
	public void setPatients(Patient patients) {
		this.patients = patients;
	}
	@Override
	public int compareTo(Event other) { //la coda degli eventi e' CRONOLOGICA CRESCENTE
		return this.time.compareTo(other.time);
	}

	@Override
	public String toString() {
		return "Event [time=" + time + ", type=" + type + ", patients=" + patients + "]";
	}

	

}
