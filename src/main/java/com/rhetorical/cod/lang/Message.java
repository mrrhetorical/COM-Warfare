package com.rhetorical.cod.lang;

public class Message {

	private String messageContent;

	Message() {}

	void setMessageContents(String contents) {
		messageContent = contents;
	}

	public String getMessage() {
		return messageContent;
	}

	void translateCodes() {
		String sec = "\u00a7";
		setMessageContents(messageContent.replace("&", sec));
	}

}