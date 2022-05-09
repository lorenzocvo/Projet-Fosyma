package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.SimpleBehaviour;

import java.util.Random;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ExploSortMsgBehaviour extends SimpleBehaviour{
	private boolean finished = false;
	private MapRepresentation myMap;
	private List<String> receivers;
	private List<AID> talking;
	private final AbstractDedaleAgent abstr;
	//private int hazard = new Random().nextInt(2);
	
	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ExploSortMsgBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.abstr = myagent;
		this.myMap=myMap;
		this.receivers=agentNames;	
		this.myAgent.addBehaviour(new ExploCoopBehaviour(this.abstr,this.myMap,this.receivers));
		
		//this.talking = receivers;
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			this.myAgent.addBehaviour(new ExploCoopBehaviour(this.abstr,this.myMap,this.receivers));
			
		}
		MessageTemplate msgTemplate=MessageTemplate.MatchAll();		
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if (msgReceived!=null && !(this.talking.contains(msgReceived.getSender()) && msgReceived.getProtocol()=="Hello")) {
			
			
			switch (msgReceived.getProtocol()) {
		
			case "Hello":
				
				this.myAgent.addBehaviour(new ExploCoopBehaviour(this.abstr,this.myMap,this.receivers));
			case "Useless protocol":
				break;
				
			case "SHARE-TOPO":
				break;
			}
		}
		
		
		

			
	}
		
		
	@Override
	public boolean done() {
		return finished;
	}
		
	

}
