package decisiontree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * String value;<br>
 * boolean isLeafNode;<br>
 * Map of String, DTNode nodes;
 * 
 * @author mcrowder65
 *
 */
public class DTNode {
	private String value;
	private boolean isLeafNode;
	private Map<String, DTNode> nodes;
	transient private ObjectMapper mapper = new ObjectMapper();

	public DTNode() {
		nodes = new HashMap<>();
		this.value = null;
		this.isLeafNode = false;
	}

	/**
	 * Sets this.value to value, and isLeafNode to false
	 * 
	 * @param value
	 *            String
	 */
	public DTNode(String value) {
		nodes = new HashMap<>();
		this.value = value;
		this.isLeafNode = false;
	}

	public DTNode(String value, boolean isLeafNode) {
		nodes = new HashMap<>();
		this.value = value;
		this.isLeafNode = isLeafNode;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isLeafNode() {
		return isLeafNode;
	}

	public void setLeafNode(boolean isLeafNode) {
		this.isLeafNode = isLeafNode;
	}

	public Map<String, DTNode> getNodes() {
		return nodes;
	}

	public void setNodes(Map<String, DTNode> nodes) {
		this.nodes = nodes;
	}

	public DTNode getNode(String key) {
		return nodes.get(key);
	}

	public void setNode(String key, DTNode value) {
		nodes.put(key, value);
	}

	@Override
	public String toString() {

		try {
			return mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
