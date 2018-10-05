package com.ericsson.eea.billing.model;

import java.io.Serializable;
import java.util.List;

import lombok.ToString;

@ToString
public class MessageEnvelope<T> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private List<T> data;
	private List<Message> messages;
    public List<T> getData() {
        return data;
    }
    public void setData(List<T> data) {
        this.data = data;
    }
    public List<Message> getMessages() {
        return messages;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

}
