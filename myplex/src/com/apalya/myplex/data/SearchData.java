package com.apalya.myplex.data;

import java.util.List;


public class SearchData {
	
	public static class ButtonData{
		public String getButtonName() {
			return buttonName;
		}

		String buttonName;
		boolean isCLicked = false;
		public boolean isCLicked() {
			return isCLicked;
		}
		public void setCLicked(boolean isCLicked) {
			this.isCLicked = isCLicked;
		}
		public ButtonData(String name, boolean clickStatus) {
			this.buttonName = name;
			this.isCLicked = clickStatus;
		}
	}
	
	String categoryName;
//	boolean isVisible = false;
	boolean isSection = false;
//	String[] names;
	List<ButtonData> searchTags;


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

	/*public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}*/

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	/*public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}*/
    public List<ButtonData> getSearchTags() {
		return searchTags;
	}

	public void setSearchTags(List<ButtonData> searchTags) {
		this.searchTags = searchTags;
	}

}
