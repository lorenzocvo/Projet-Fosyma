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
import java.util.Scanner;


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
public class ControlledBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	
	private List<String> Tresure = new ArrayList<>();

	private String type = "";
	
	private int total = 0;
/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ControlledBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		
		
		
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,50,this.myMap,list_agentNames));
			//this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent,list_agentNames));
			//this.myAgent.addBehaviour(new CollectorBehaviour((AbstractDedaleAgent)this.myAgent));
			
		}

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(myPosition);
		if (myPosition!=null){
			//List of observable from the agent's current position
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//System.out.println(lobs);
			System.out.println("Ma postition :"+myPosition);
			//int j = 0;
			for (int i=0;i<lobs.size();i++) {
				if(lobs.get(i).getRight().size() >0 && lobs.get(i).getRight().get(0).getLeft().toString()=="Gold")  {
					this.Tresure.add(lobs.get(i).getLeft());
					//System.out.println(((AbstractDedaleAgent) this.myAgent).pick());
					
				}
				
				if(lobs.get(i).getLeft()!=myPosition) {
					System.out.println("Tapez "+i+" pour aller en : "+lobs.get(i).getLeft());
					
				}

			}

			Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Enter a number: ");
			int n = reader.nextInt(); // Scans the next token of the input as an int.
			//once finished
			//reader.close();
			String nextNode;
			switch (n){
				case 1:
					nextNode = lobs.get(1).getLeft();
					break;
				case 2:
					nextNode = lobs.get(2).getLeft();
					break;
					
				case 3 :
					nextNode = lobs.get(3).getLeft();
					break;
					
				case 4 : 
					nextNode = lobs.get(4).getLeft();
					break;
					
				case 5 :
					nextNode = lobs.get(5).getLeft();
					break;
					
				default:
					nextNode = myPosition;
					break;
			}
				
			
			
			
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			//String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			
				
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			

		}
	}

	@Override
	public boolean done() {
		return finished;
		
	}

}
