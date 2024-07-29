package com.noesis.telco.summary.manager.dto;

public class UserSummaryData {
	
	private String username;
    private String smscId;
    private int submitted;
    private int delivered;
    private int failed;

    // Constructors
    public UserSummaryData() {
    }

    public UserSummaryData(String username, String smscId, int submitted, int delivered, int failed) {
        this.username = username;
        this.smscId = smscId;
        this.submitted = submitted;
        this.delivered = delivered;
        this.failed = failed;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSmscId() {
        return smscId;
    }

    public void setSmscId(String smscId) {
        this.smscId = smscId;
    }

    public int getSubmitted() {
        return submitted;
    }

    public void setSubmitted(int submitted) {
        this.submitted = submitted;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

	@Override
	public String toString() {
		return "UserSummaryData [username=" + username + ", smscId=" + smscId + ", submitted=" + submitted
				+ ", delivered=" + delivered + ", failed=" + failed + "]";
	}

    

}
