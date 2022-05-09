package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
public class CollectorBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	
	private List<String> tresors;

	private List<String> valeur;
	
	private int mean;
	
	private MapRepresentation myMap;
	
	private List<String> path;
	
	private int cpt = 0;
	
	private boolean blocage = false;
	
	private List<String> list_agentNames;
	
	private int start;
	
	private int tick = 0;
	
	/**
	 * Current knowledge of the agent regarding the environment
	 */

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	//private static final long serialVersionUID = 9088209402507795289L;

	public CollectorBehaviour (final AbstractDedaleAgent myagent, List<String> agents,MapRepresentation Map,List <String> gold, List<String>val) {
		super(myagent);
		this.tresors = gold;
		this.myMap = Map;
		this.start = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight();
		this.valeur = val;
		this.list_agentNames = agents;
		
		
		
		List <String> ordre = new ArrayList<>();
		ordre.add(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		int min;
		int mini;
		
		int total = 0;
		
		for(int i=0;i<this.valeur.size();i++) {
			
			total+=Integer.parseInt(this.valeur.get(i));
			
			
		}
		
		this.mean = total/(this.list_agentNames.size()+1);
		for(int i=0;i<this.tresors.size();i++) {
			min = Integer.MAX_VALUE;
			mini = -1;
			for(int j=0;j<this.tresors.size();j++) {
				if(!ordre.contains(this.tresors.get(j)) ) {
					if(this.myMap.getShortestPath(ordre.get(i), this.tresors.get(j)).size()<min) {
						min = this.myMap.getShortestPath(ordre.get(i), this.tresors.get(j)).size();
						mini = j;
					}
					
				}
			}
			if(mini!=-1) {
				ordre.add(this.tresors.get(mini));
			}
			
			//this.myMap.getShortestPath(this.tresors.get(ordre.get(0)), this.tresors.get(i));
			
			//ordre.add(i);
		}
		this.tresors = ordre;
		this.path = this.myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.tresors.get(this.cpt));
		System.out.println("trésors");
		System.out.println(this.tresors);
	}
	
	
	
	@Override
	public void action() {
		tick++;
		try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Example to retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.myAgent.getLocalName()+" -- myCurrentPosition is: "+myPosition);
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);

			//list of observations associated to the currentPosition
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			List<String> other = new ArrayList();
			//example related to the use of the backpack for the treasure hunt
			Boolean b=false;
			for(Couple<Observation,Integer> o:lObservations){
				switch (o.getLeft()) {
				case DIAMOND:case GOLD:
					System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
					//((AbstractDedaleAgent) this.myAgent).openLock(o.getLeft());
					System.out.println(this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
					System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					b=true;
					break;
					
				default:

					break;
				}
			}

			for(int i=1;i<lobs.size();i++) {
				try {
					lobs.get(i).getRight().get(0);
					other.add(lobs.get(i).getLeft());
					System.out.println(lobs.get(i).getRight());
				}catch(Exception e) {
					//break;
				}
			}
			System.out.println(other);
			//If the agent picked (part of) the treasure
			if (b){
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				System.out.println(this.myAgent.getLocalName()+" - State of the observations after trying to pick something "+lobs2);
			}
			String nextNode=null;
			Random r;
			int moveId;//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
			
			
			if (this.tresors.get(this.cpt)!= ((AbstractDedaleAgent)this.myAgent).getCurrentPosition()){
				//no directly accessible openNode
				//chose one, compute the path and take the first step.
				nextNode=this.myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.tresors.get(cpt)).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
				
				
					
				
				while(other.contains(nextNode)) {
					r = new Random();
					moveId=1+r.nextInt(lobs.size()-1);
					nextNode = lobs.get(moveId).getLeft();
						
						
				}
				
					
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			else {
				this.cpt++;
				nextNode = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			System.out.println(this.mean<this.start-((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight());
			if(this.cpt>=this.tresors.size() || this.mean<this.start-((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0).getRight() || this.tick>500) {
				System.out.println("je m'efface");
				this.myAgent.doDelete();
			}
			if(blocage) {
				r = new Random();
				moveId=1+r.nextInt(lobs.size()-1);
				nextNode = lobs.get(moveId).getLeft();
				this.blocage = false;
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			else {
				if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
					this.blocage=true;
				}
			}
		}

	}

	@Override
	public boolean done() {
		return finished;
	}

}
