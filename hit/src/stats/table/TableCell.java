package stats.table;

public class TableCell {
	private boolean isHeader;
	private String formatedContent;

	public String getFormatedContent() {
		return formatedContent;
	}

	public void setFormatedContent(String formatedContent) {
		this.formatedContent = formatedContent;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public boolean isHeader() {
		return isHeader;
	}

}
