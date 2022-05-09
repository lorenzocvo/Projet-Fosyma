package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import dataStructures.tuple.Couple;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.CollectorBehaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class RegroupementBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	
	private List<String> Tresure;

	private String type = "";
	
	private int total = 0;
	
	private boolean blocage = false;
	
	private List<String> path = null;
	
	private int cpt = 0;
	
	private List<String>danger = new ArrayList();
	
	private List<Boolean> move = new ArrayList(); //move en 0 et regroupement en 1
	
	
	private boolean arrive = false;
	
/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public RegroupementBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames,List<String> tresor) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.move.add(true);
		this.Tresure=tresor;
		
		
	}

	@Override
	public void action() {

		
		
		//Envoi de l'information aux agents qu'il croise
		ACLMessage newmsg=new ACLMessage(ACLMessage.INFORM);
		newmsg.setSender(this.myAgent.getAID());
		newmsg.setProtocol("MEET");
		//List <String> tresor = this.Tresure.getRight();

	

		
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
		
		
		
		
		
		MessageTemplate msgTemplate2=MessageTemplate.MatchAll();
		ACLMessage msgReceived2=this.myAgent.receive(msgTemplate2);
		if(msgReceived2!=null && msgReceived2.getProtocol() == "GOLD") {
			
			//((AbstractDedaleAgent)this.myAgent).addBehaviour(new CollectorBehaviour());
			
			
		}
		
		
		
		
		
		
		try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.myAgent.getLocalName()+myPosition);
		if (myPosition!=null && this.move.get(0)){
			//List of observable from the agent's current position
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			
			
			Random r;
			int moveId;
			
		
			
			//changer condition d'activation
			if (this.arrive){
				//Explo finished
				finished=true;
				this.move.set(1, true);
				//System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				//this.myAgent.addBehaviour(new CollectorBehaviour((AbstractDedaleAgent)this.myAgent,this.Tresure,this.myMap));
				
				
			}else{


				if(this.path==null || this.cpt>=this.path.size()) {
					this.cpt = 0;
				}
				
				if(this.blocage) {
					
					this.blocage = false;
					
					r = new Random();
					moveId=1+r.nextInt(lobs.size()-1);
					nextNode = lobs.get(moveId).getLeft();	
					this.path = null;
					this.cpt = 0;
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}
				else {
					nextNode = this.path.get(this.cpt);
					this.cpt++;
					if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
						this.blocage=true;
					}
				}
			}

			
			
		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
