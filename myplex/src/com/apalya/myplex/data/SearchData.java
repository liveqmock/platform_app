package com.apalya.myplex.data;

public class SearchData {
	String categoryName;
	boolean isVisible = false;
	boolean isSection = false;
	String[] names;
    boolean isExpanded=false;
    public static final int SECTION = 1;

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	public boolean isSection() {
		return isSection;
	}

	public void setSection(boolean isSection) {
		this.isSection = isSection;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}

}
