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
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	
	private MapRepresentation myMap;

	private List<String> list_agentNames;
	
	private List<String> Tresure = new ArrayList<>();
	
	private List<String> valeur = new ArrayList<>();

	private String type = "";
	
	private int total = 0;
	
	private boolean blocage = false;
	
	private List<String> path = null;
	
	private int cpt = 0;
	
	private List<String>danger = new ArrayList();
	
	private List<Boolean> move = new ArrayList(); //move en 0 et regroupement en 1
	
	private List<String> talking = new ArrayList<>();
	
	private List<String> rdv = new ArrayList<>();
	
	private int tick = 0;
	
	
/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.move.add(true);
		//this.move.add(false);
		

	}

	@Override
	public void action() {
		this.tick++;
		try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//A un certain intervalle de temps on reinitialise la liste des interlocuteurs
		if (((AbstractDedaleAgent)this.myAgent).getBehavioursCnt()==1 && this.tick%200 == 0) {
			this.talking = new ArrayList<>();
		}
		//On envoie à intervalle régulier des messages pour avertir les autres agents de notre présence
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(tick%2==0) {
			
		
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol("ASK-TOPO");
			
			if (myPosition!=""){
				System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
				//voir pour le contenu
				//msg.setContent("Hello World, I'm at "+myPosition);
				
				for(int i =0; i<this.list_agentNames.size();i++) {
					msg.addReceiver(new AID(this.list_agentNames.get(i),AID.ISLOCALNAME));
					
				}
	
		
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
		}
		//On vérifie si on a reçu un message d'un nouvel agent, si oui on passe en mode communication
		
		MessageTemplate msgTemplate2=MessageTemplate.MatchAll();
		
		ACLMessage msgReceived2=this.myAgent.receive(msgTemplate2);
		
		if(msgReceived2 != null && !this.talking.contains(msgReceived2.getSender().getLocalName()) ) {
			//voir pour mettre le message en paramètre pour skip des étapes
			this.move.set(0, false);
			this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent,list_agentNames,this.move,this.myMap,this.Tresure,this.talking));
			
		}
		
		
		
		
		
		//temps d'attente
		

		
		//initialisation de la map et des behaviour
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,50,this.myMap,list_agentNames));
			//this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent,list_agentNames,this.move,this.myMap,this.Tresure));
			//this.myAgent.addBehaviour(new CollectorBehaviour((AbstractDedaleAgent)this.myAgent));
			
		}
		
		
		if(this.rdv.size()==0 ) {
			this.rdv.add(myPosition);
			System.out.println(this.rdv);
		}

		//0) Retrieve the current position
		
		System.out.println(this.myAgent.getLocalName()+myPosition);
		if (myPosition!=null && this.move.get(0)){
			//List of observable from the agent's current position
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			
			
			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			
			
			if(!this.danger.contains(myPosition)) {
				for (int i=0;i<lobs.size();i++) {
					
					
					//Mise à jour de la carte et choix du premier nouveau noeud
					String nodeId = lobs.get(i).getLeft();
					boolean isNewNode = this.myMap.addNewNode(nodeId);
					if(myPosition!=nodeId) {
						this.myMap.addEdge(myPosition, nodeId);
						if(nextNode == null && isNewNode) {
							nextNode = nodeId;
							this.path = this.myMap.getShortestPathToClosestOpenNode(nextNode);
						}
					}
					
					//Enregistrement des trésors
					if(lobs.get(i).getRight().size() >0 && lobs.get(i).getRight().get(0).getLeft().toString()=="Gold")  {
						System.out.println(i);
						this.Tresure.add(lobs.get(i).getLeft());
						this.valeur.add(lobs.get(i).getRight().get(0).getRight().toString());
					}
					
					
					
					//Fermer les noeuds dangereux
					try {
						if(lobs.get(i).getRight().get(0).toString()!="Gold" && lobs.get(i).getRight().get(0).toString()!="Diamond") {
						
							this.myMap.addNode(lobs.get(i).getLeft(), MapAttribute.closed);
							this.danger.add(lobs.get(i).getLeft());
							System.out.println(lobs.get(i).getRight());
						}
					}catch(Exception e) {
						//break;
					}
					
					
				}
			}
				

			Random r;
			int moveId;
			
			
			System.out.println(this.rdv);
			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode() || this.tick>500){
				//Explo finished
				this.myAgent.addBehaviour(new CollectorBehaviour((AbstractDedaleAgent)this.myAgent,this.list_agentNames,this.myMap, this.Tresure,this.valeur));
				finished=true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				System.out.println(this.rdv);
			}else{


				if(this.path==null || this.cpt>=this.path.size()) {
					this.cpt = 0;
					this.path = this.myMap.getShortestPathToClosestOpenNode(myPosition);
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
