package com.lenovo.czlib.nodex;

import java.util.List;

public interface ZKPathListener {
	public void onDataChange(String path,byte[] value);
	public void onDelete(String path);
	public void onChildrenChange(String path,List<String> subnodes);
	public void onCreate(String path,byte[] value);
}
