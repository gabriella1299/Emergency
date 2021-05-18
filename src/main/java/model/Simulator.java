package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import model.Event.EventType;
import model.Patient.ColorCode;

public class Simulator {
	
	//CODA DEGLI EVENTI
	PriorityQueue<Event> queue; //posso inserirla in una coda prioritaria perche' ho implementato 'Comparable'
	
	//MODELLO DEL MONDO
	private List<Patient> patients; //ho messo TUTTI i pazienti
	private PriorityQueue<Patient> waitingRoom; //contiene SOLO i pazienti in lista di attesa (WHITE,YELLOW,RED)
	
	private int freeStudios; //numero studi liberi
	
	private Patient.ColorCode ultimoColore;
	
	//PARAMETRI DI INPUT
	//Abbiamo dato un valore di default ma dovranno essere impostate dall'ext
	private int totStudios=3;//NS
	
	private int numPatients=120;//NP
	private Duration T_ARRIVAL=Duration.ofMinutes(5);
	
	private Duration DURATION_TRIAGE=Duration.ofMinutes(5);
	private Duration DURATION_WHITE=Duration.ofMinutes(10);
	private Duration DURATION_YELLOW=Duration.ofMinutes(15);
	private Duration DURATION_RED=Duration.ofMinutes(30);
	
	private Duration TIMEOUT_WHITE=Duration.ofMinutes(60);
	private Duration TIMEOUT_YELLOW=Duration.ofMinutes(30);
	private Duration TIMEOUT_RED=Duration.ofMinutes(30);
	
	private LocalTime startTime=LocalTime.of(8, 00);
	private LocalTime endTime=LocalTime.of(20, 00);
	
	//PARAMETRI DI OUTPUT
	private int patientsTreated;
	private int patientsAbandoned;
	private int patientsDead;
	
	//INIZIALIZZA IL SIMULATORE: creo eventi iniziali
	//non modifico piu' i paramtetri
	public void init() {//ogni volta che chiamo init riparte tutto da capo
		//inizializza coda eventi
		this.queue=new PriorityQueue<>();
		
		//inizializza modello del mondo
		this.patients=new ArrayList<>();
		this.freeStudios=this.totStudios;//inizialmente sono tutti liberi
		this.waitingRoom=new PriorityQueue<>();
		
		ultimoColore=ColorCode.RED;
		
		//inizializza parametri output, ogni volta che inizia init devono essere puliti
		this.patientsAbandoned=0;
		this.patientsDead=0;
		this.patientsTreated=0;
		
		//inietta gli eventi di input(ARRIVAL)
		LocalTime ora=this.startTime;
		int inseriti=0;
		//Patient.ColorCode colore=ColorCode.WHITE;
		
		this.queue.add(new Event(ora,EventType.TICK,null));
		
		while(ora.isBefore(this.endTime) && inseriti<this.numPatients) {
			
			Patient p=new Patient(inseriti,ora, ColorCode.NEW); //new e' il colore del paziente
			
			Event e=new Event(ora, EventType.ARRIVAL, p);
			
			this.queue.add(e);
			this.patients.add(p);
			
			inseriti++;
			ora=ora.plus(T_ARRIVAL);
		}
	
	}
	
	
	private Patient.ColorCode prossimoColore(){
		
		if(ultimoColore.equals(ColorCode.WHITE))
			ultimoColore=ColorCode.YELLOW;
		else if(ultimoColore.equals(ColorCode.YELLOW))
			ultimoColore=ColorCode.RED;
		else
			ultimoColore=ColorCode.WHITE;
		
		return ultimoColore;
	}
	
	//ESEGUE LA SIMULAZIONE
	public void run() {
		while(!this.queue.isEmpty()) {
			Event e=this.queue.poll();
			System.out.println(e);
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		Patient p= e.getPatients();
		LocalTime ora=e.getTime();
		
		switch(e.getType()) {
			
			case ARRIVAL://arriva e va al triage
				this.queue.add(new Event(ora.plus(DURATION_TRIAGE), EventType.TRIAGE, p));
				break;
			
			case TRIAGE://l'utente avra' un colore terminato il triage
				p.setColor(prossimoColore());
				
				if(p.getColor().equals(Patient.ColorCode.WHITE)) {
					this.queue.add(new Event(ora.plus(TIMEOUT_WHITE), EventType.TIMEOUT, p));
					this.waitingRoom.add(p);
				}else if(p.getColor().equals(Patient.ColorCode.YELLOW)) {
					this.queue.add(new Event(ora.plus(TIMEOUT_YELLOW), EventType.TIMEOUT, p));
					this.waitingRoom.add(p);
				}else if(p.getColor().equals(Patient.ColorCode.RED)) {
					this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIMEOUT, p));
					this.waitingRoom.add(p);
				}
				break;
			
			case FREE_STUDIO://Quale paziente ha diritto di entrare? Con priorita' piu' alta nella waiting room
				//il tick si puo'accorgere che i freestudios sono 0 proprio nell'istante in cui entra un paziente: conflitto quindi faccio questo controllo
				if(this.freeStudios==0)
					return;
				
				Patient primo= this.waitingRoom.poll();//la coda potrebbe essere vuota
				if(primo!=null) {
					//ammetti paziente nello studio
					if(primo.getColor().equals(ColorCode.WHITE))
						this.queue.add(new Event(ora.plus(DURATION_WHITE), EventType.TREATED,primo));
					if(primo.getColor().equals(ColorCode.YELLOW))
						this.queue.add(new Event(ora.plus(DURATION_YELLOW), EventType.TREATED,primo));
					if(primo.getColor().equals(ColorCode.RED))
						this.queue.add(new Event(ora.plus(DURATION_RED), EventType.TREATED,primo));
					
					primo.setColor(ColorCode.TREATING);
					
					this.freeStudios--;					
				}
				break;
			
			case TIMEOUT://mi tolgo da un colore e vado in uno stato diverso
				Patient.ColorCode colore=p.getColor();
				
				switch(colore) {
				case WHITE:
					this.waitingRoom.remove(p);
					p.setColor(ColorCode.OUT);
					this.patientsAbandoned++;
					break;
				case YELLOW:
					this.waitingRoom.remove(p);
					p.setColor(ColorCode.RED);//fa cambiare la priorita' al paziente, devo farlo io perche' la priority queue non lo fa!
					//schedulo un time out di 'rossi'
					this.queue.add(new Event(ora.plus(TIMEOUT_RED), EventType.TIMEOUT, p));	
					this.waitingRoom.add(p);
					break;
				case RED:
					this.waitingRoom.remove(p);
					p.setColor(ColorCode.BLACK);
					this.patientsDead++;
					break;
				default:
					System.out.println("ERRORE: TIME OUT CON COLORE "+colore);
				}
				break;
			
			case TREATED://paziente esce
				this.patientsTreated++;
				p.setColor(ColorCode.OUT);
				this.freeStudios++;//schedulo evento 'chiama qualcuno'
				this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
				//non togliamo dalla waiting room perche il caso 'free studio' fa il poll, che lo rimuove
				break;
				
			case TICK:
				if(this.freeStudios>0 && !this.waitingRoom.isEmpty()) {
					this.queue.add(new Event(ora, EventType.FREE_STUDIO, null));
				}
				if(ora.isBefore(endTime))
					this.queue.add(new Event(ora.plus(Duration.ofMinutes(5)), EventType.TICK, null));//si auto-rigenera da solo
				break;
		}
	}

	//SETTERS per parametri di input	
	public void setTotStudios(int totStudios) {
		this.totStudios = totStudios;
	}

	public void setNumPatients(int numPatients) {
		this.numPatients = numPatients;
	}

	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public void setDURATION_TRIAGE(Duration dURATION_TRIAGE) {
		DURATION_TRIAGE = dURATION_TRIAGE;
	}

	public void setDURATION_WHITE(Duration dURATION_WHITE) {
		DURATION_WHITE = dURATION_WHITE;
	}
	
	public void setDURATION_YELLOW(Duration dURATION_YELLOW) {
		DURATION_YELLOW = dURATION_YELLOW;
	}

	public void setDURATION_RED(Duration dURATION_RED) {
		DURATION_RED = dURATION_RED;
	}
	
	public void setTIMEOUT_WHITE(Duration tIMEOUT_WHITE) {
		TIMEOUT_WHITE = tIMEOUT_WHITE;
	}

	public void setTIMEOUT_YELLOW(Duration tIMEOUT_YELLOW) {
		TIMEOUT_YELLOW = tIMEOUT_YELLOW;
	}

	public void setTIMEOUT_RED(Duration tIMEOUT_RED) {
		TIMEOUT_RED = tIMEOUT_RED;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}
	
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	//GETTERS per parametri di output
	public int getPatientsTreated() {
		return patientsTreated;
	}

	public int getPatientsAbandoned() {
		return patientsAbandoned;
	}

	public int getPatientsDead() {
		return patientsDead;
	}

}
