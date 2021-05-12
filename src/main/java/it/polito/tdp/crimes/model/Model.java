package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo; //vertici tipo di eventi
	private EventsDao dao;
	private List<String> percorsoMigliore;
	
	public Model() {
		
		dao=new EventsDao();
	}

	public List<String> getCategorie(){
		
		return dao.getCategorie();
	}

	public void creaGrafo(String categoria, int mese) {
		
		this.grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		
		//vertici
		
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		//archi
		
		for(Connessione c: dao.getConnessioni(categoria, mese)) {
			if(this.grafo.getEdge(c.getV1(), c.getV2())==null) {
				
				Graphs.addEdgeWithVertices(grafo, c.getV1(), c.getV2(), c.getPeso());
			}
		}
		
		System.out.println("NUmero vertici: "+ grafo.vertexSet().size());
		System.out.println("Numero archi: "+ grafo.edgeSet().size());
		
		
	}
	
	public List<Connessione> getArchi(){
		
		//calcolo peso medio degli archi presenti nl grafo 
		
		double pesoMedio=0.0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoMedio+= this.grafo.getEdgeWeight(e);
		}
		
		pesoMedio= pesoMedio/this.grafo.edgeSet().size();
		
		//filtro archi tenendo conto solo quelli che hanno peso maggiore del peso medio
		
		List<Connessione> result= new ArrayList<Connessione>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > pesoMedio)
				result.add(new Connessione(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), this.grafo.getEdgeWeight(e)));
			
		}
		
		return result;
		
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione){
		
		//ricorsione
		
		this.percorsoMigliore= new ArrayList<>();
		
		List<String> parziale= new ArrayList<>();
		
		parziale.add(sorgente);
		
		cerca(destinazione, parziale);
		
		return this.percorsoMigliore;
		
		
	}
	
	private void cerca(String destinazione, List<String> parziale) {
		
		//caso terminale

		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			if(parziale.size() >this.percorsoMigliore.size()) {
				this.percorsoMigliore=new ArrayList<>(parziale);
			}
			return;
		}
		
		
		//altrimenti scorro i vicini dell'ultimo inserito e provo a inserirli uno a uno
		
		for(String vicino: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione, parziale);
				parziale.remove(parziale.size()-1);
			}
			
		}
		
	}
}
