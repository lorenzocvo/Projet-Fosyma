package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
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
public class NewPathBehaviour extends TickerBehaviour{

	/**
	 * 
	 */
	private List<String> list_agentNames;
	private static final long serialVersionUID = -2058134622078521998L;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public NewPathBehaviour (final Agent myagent, List<String> agentNames) {
		super(myagent, 500);
		this.list_agentNames = agentNames;
		//super(myagent);
	}

	@Override
	public void onTick() {
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("Useless protocol");

		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			msg.setContent("Hello World, I'm at "+myPosition);
			
			for(int i =0; i<this.list_agentNames.size();i++) {
				msg.addReceiver(new AID(this.list_agentNames.get(i),AID.ISLOCALNAME));
				
			}

			
			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
		MessageTemplate msgTemplate=MessageTemplate.or(MessageTemplate.MatchProtocol("Useless protocol"), MessageTemplate.MatchProtocol("reponse"));
		
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if(msgReceived != null) {
			ACLMessage newmsg = msgReceived.createReply();
			//ACLMessage newmsg = msgReceived.createReply(getTickCount());
			System.out.println("sender");
			newmsg.setSender(this.myAgent.getAID());
			newmsg.setContent("C'est ma reponse.");
			System.out.println(newmsg);
			System.out.println("receiver");
			System.out.println(msgReceived);
			((AbstractDedaleAgent)this.myAgent).sendMessage(newmsg);
		}

		
		
	}
}