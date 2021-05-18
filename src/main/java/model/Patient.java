package model;

import java.time.LocalTime;

public class Patient implements Comparable<Patient> {
	
	public enum ColorCode{
		NEW, //in triage (hp: no coda nel triage, il cliente viene subito valutato)
		WHITE,YELLOW,RED,BLACK, //in sala d'attesa
		TREATING, //dentro studio medico
		OUT //a casa (abbandonato o curato)
	};
	
	private int num;//num progressivo univoco, per hashcode e equals
	private LocalTime arrivalTime;
	private ColorCode color;
	
	public Patient(int num,LocalTime arrivalTime, ColorCode color) {
		super();
		this.num=num;
		this.arrivalTime = arrivalTime;
		this.color = color;
	}
	public LocalTime getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(LocalTime arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public ColorCode getColor() {
		return color;
	}
	public void setColor(ColorCode color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return "Patient [num=" + num + ", arrivalTime=" + arrivalTime + ", color=" + color + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + num;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (num != other.num)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Patient other) {//criterio con cui dico se un paziente ha priorita' maggiore di un altro
		//Qui ci sta il criterio della sala d'attesa (coda)
		//Se 2 p hanno stesso colore passa chi arrivato prima
		if(this.color.equals(other.color)) {
			return this.arrivalTime.compareTo(other.arrivalTime);
		}
		//Altrimenti...
		//caso uno rosso
		else if(this.color.equals(Patient.ColorCode.RED)) 
			return -1;//deve passare per primo this, la coda prioritaria da' l'elemento piu' piccolo, quindi negativo
		
		else if(other.color.equals(Patient.ColorCode.RED))
			return +1;
		//caso b-g o g-b
		else if(this.color.equals(Patient.ColorCode.YELLOW))//g-b
			return -1;
		else
			return +1;//b-g
	}
	
	
	

}
