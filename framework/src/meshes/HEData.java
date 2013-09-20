package meshes;

import java.util.HashMap;


public class HEData<He_element extends HEElement, Payload>{
	
	

	HashMap<He_element, Payload> myData;
		
	public HEData(){
		myData = new HashMap<He_element, Payload>();
	}

	public void put(He_element v, Payload data) {
		myData.put(v, data);
	}

	public Payload get(He_element v) {
		return myData.get(v);
	}
	
	public int size(){
		return myData.size();
	}

	public void remove(He_element v) {
		myData.remove(v);
	}

}
