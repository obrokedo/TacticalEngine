package tactical.engine.message;

public class LoadChapterMessage extends Message {
	private int triggerId;
	private String header;
	private String description;
	
	public LoadChapterMessage(int triggerId,
			String header, String description) {
		super(MessageType.LOAD_CHAPTER, false, false);
		this.triggerId = triggerId;
		this.header = header;
		this.description = description;
	}

	public int getTriggerId() {
		return triggerId;
	}

	public String getHeader() {
		return header;
	}

	public String getDescription() {
		return description;
	}
}
