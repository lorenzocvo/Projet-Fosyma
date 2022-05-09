package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.util.Pair;
import dataStructures.tuple.Couple;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class SayHelloBehaviour extends TickerBehaviour{

	/**
	 * 
	 */
	private List<String> list_agentNames;
	private static final long serialVersionUID = -2058134622078521998L;
	private List<Boolean> stop;
	private MapRepresentation Map;
	private List<String> talking;
	private List <String> mytresor;
	private List<String> valeur;
	private String rdv;
	private int tick = 0;
	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SayHelloBehaviour (final Agent myagent, List<String> agentNames,List <Boolean> move, MapRepresentation mymap, List<String> tresor,List<String> talk) {
		super(myagent, 1000);
		this.list_agentNames = agentNames;
		this.stop = move;
		this.Map = mymap;
		this.mytresor = tresor;
		this.talking = talk;
		
	}

	@Override
	public void onTick() {
		tick++;
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		
		MessageTemplate msgTemplate2=MessageTemplate.MatchAll();
		
		ACLMessage msgReceived2=this.myAgent.receive(msgTemplate2);
		if (msgReceived2 == null  ) {
			
			this.stop.set(0, true);
			((AbstractDedaleAgent)this.myAgent).removeBehaviour(this);
		}
		else if(msgReceived2.getProtocol() == "ASK-TOPO" && !this.talking.contains(msgReceived2.getSender().getLocalName())) {
			System.out.println(msgReceived2.getSender().getLocalName());
			System.out.println(msgReceived2.getSender().getLocalName().substring(msgReceived2.getSender().getLocalName().length()-1));
			System.out.println(((AbstractDedaleAgent)this.myAgent).getLocalName().substring(((AbstractDedaleAgent)this.myAgent).getLocalName().length()-1));
			if(Integer.parseInt( msgReceived2.getSender().getLocalName().substring(msgReceived2.getSender().getLocalName().length()-1))<Integer.parseInt( ((AbstractDedaleAgent)this.myAgent).getLocalName().substring(((AbstractDedaleAgent)this.myAgent).getLocalName().length()-1))) {
				
				
				ACLMessage newmsg = msgReceived2.createReply();

				newmsg.setSender(this.myAgent.getAID());

				newmsg.setProtocol("SHARE-MAP");
							
				
				try {					
					SerializableSimpleGraph<String, MapAttribute> sg=this.Map.getSerializableGraph();	
					newmsg.setContentObject(sg);
			
				} catch (Exception e) {
					e.printStackTrace();
				}
				

				((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
			}
			this.talking.add(msgReceived2.getSender().getLocalName());
			this.stop.set(0, false);
			
		}else if (msgReceived2.getProtocol() == "SHARE-MAP"){
			
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived2.getContentObject();
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
			
			this.Map.mergeMap(sgreceived);
			
			ACLMessage newmsg = msgReceived2.createReply();

			
			
			try {					
				SerializableSimpleGraph<String, MapAttribute> sg=this.Map.getSerializableGraph();
				newmsg.setContentObject(sg);
			} catch (IOException e) {
				
				e.printStackTrace();
				
			}
			
			
			newmsg.setSender(this.myAgent.getAID());
			
			newmsg.setProtocol("SHARE-MAP2");
			
			Random r = new Random();
			
			//faire un message pour la destination
			
			//destination == nextnode de l'interlocuteur
			//String destination = this.Map.getOpenNodes().get(r.nextInt(this.Map.getOpenNodes().size()-1));
			//newmsg.setContent(destination);
			
			
			((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
			
		}else if (msgReceived2.getProtocol() == "SHARE-MAP2"){
			
			
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived2.getContentObject();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			this.Map.mergeMap(sgreceived);
			
			ACLMessage newmsg = msgReceived2.createReply();
			newmsg.setSender(this.myAgent.getAID());
			newmsg.setProtocol("RDV");
			
			
			String contenu = String.join(",",this.mytresor);
			String contenu2 = String.join(",", this.valeur);
			contenu = contenu+"/"+contenu2;
			
			newmsg.setContent(contenu);
			
			
			
			((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
			
			
		}else if (msgReceived2.getProtocol() == "RDV"){
			
			//récupérer contenu et changer le rdv
			
			List<String> full = new ArrayList<String>(Arrays.asList(msgReceived2.getContent().split("/")));
			List<String> newtresor = new ArrayList<String>(Arrays.asList(full.get(0).split(",")));
			List<String> newvaleur = new ArrayList<String>(Arrays.asList(full.get(1).split(",")));
			
			for(int i=0;i<newtresor.size();i++) {
				if(!this.mytresor.contains(newtresor.get(i))) {
					this.mytresor.add(newtresor.get(i));
					this.valeur.add(newvaleur.get(i));
				}
			}
			
			
			
			ACLMessage newmsg = msgReceived2.createReply();
			newmsg.setSender(this.myAgent.getAID());
			newmsg.setProtocol("GO");
			
			String contenu = String.join(",",this.mytresor);
			String contenu2 = String.join(",", this.valeur);
			contenu = contenu+"/"+contenu2;
			
			newmsg.setContent(contenu);
			
			
			((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
			
			
			
			
		}else if (msgReceived2.getProtocol() == "GO"){
			
			//récupérer contenu
			List<String> full = new ArrayList<String>(Arrays.asList(msgReceived2.getContent().split("/")));
			List<String> newtresor = new ArrayList<String>(Arrays.asList(full.get(0).split(",")));
			List<String> newvaleur = new ArrayList<String>(Arrays.asList(full.get(1).split(",")));
			
			for(int i=0;i<newtresor.size();i++) {
				if(!this.mytresor.contains(newtresor.get(i))) {
					this.mytresor.add(newtresor.get(i));
					this.valeur.add(newvaleur.get(i));
				}
			}
			
			
			
			
			
		}
		
		
		
		
		else {
			
			
			//addbehaviour explo coop
			System.out.println("End of communication");
			
			if(this.talking.size()==1) {
				this.stop.set(0, true);
			}
			((AbstractDedaleAgent)this.myAgent).removeBehaviour(this);
		}

		if (this.tick>50) {
			this.stop.set(0, true);
			((AbstractDedaleAgent)this.myAgent).removeBehaviour(this);
			
		}
		
	}
}