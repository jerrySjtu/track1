package data;

public class CategoryKey {
	private int layer;
	private int parent;
	private int child;

	public CategoryKey(int layer, int parent, int child) {
		this.layer = layer;
		this.parent = parent;
		this.child = child;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public int getChild() {
		return child;
	}

	public void setChild(int child) {
		this.child = child;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		CategoryKey key = (CategoryKey) obj;
		return (key.getLayer() == layer && key.getParent() == parent && key.getChild() == child);
	}
	
	@Override
	public int hashCode(){
		int hash = 7;
		hash = hash * 31 + layer;
		hash = hash * 31 + parent;
		hash = hash * 31 + child;
		return hash;
	}

	@Override
	public String toString() {
		return layer + "," + parent + "," + child;
	}

}
