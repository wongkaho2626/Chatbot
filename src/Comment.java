public class Comment {
	private String id;
	private String i;
	private String content;
	private String contentAfterSegmentation;
	private double score;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getI() {
		return i;
	}
	
	public void setI(String i) {
		this.i = i;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContentAfterSegmentation() {
		return contentAfterSegmentation;
	}
	
	public void setContentAfterSegmentation(String contentAfterSegmentation) {
		this.contentAfterSegmentation = contentAfterSegmentation;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
}
