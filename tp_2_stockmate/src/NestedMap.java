import java.util.HashMap;

class NestedMap<K, V> {
	
	/* borrowed from https://stackoverflow.com/questions/2774608/
	 *Example usage
	 * NestedMap<Character, String> m = new NestedMap<>();

        m.makeChild('f');
        m.getChild('f').makeChild('o');
        m.getChild('f').getChild('o').setValue("bar");
        m.getChild('f').getChild('o').getValue();
	 */

    @SuppressWarnings("rawtypes")
	private final HashMap<K, NestedMap> child;
    private V value;
    private HashMap<String, String> values= new HashMap<String,String>();

    public NestedMap() {
        child = new HashMap<>();
        value = null;
    }

    public boolean hasChild(K k) {
        return this.child.containsKey(k);
    }
    
    public boolean hasChilds(String key) {
        return this.values.containsKey(key);
    }

    @SuppressWarnings("unchecked")
	public NestedMap<K, V> getChild(K k) {
        return this.child.get(k);
    }

    @SuppressWarnings("rawtypes")
	public void makeChild(K k) {
        this.child.put(k, new NestedMap());
    }

    public V getValue() {
        return value;
    }
    
    public String getValues(String key) {
        return values.get(key);
    }
    
    public void setValues(String key, String value) {
    	values.put(key, value);
    }

    public void setValue(V v) {
        value = v;
    }
}