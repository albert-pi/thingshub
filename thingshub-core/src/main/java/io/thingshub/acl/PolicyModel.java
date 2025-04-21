package io.thingshub.acl;

import lombok.Data;

@Data
public class PolicyModel {

	private String subject;

	private String source;

	private AclAction action;

	private AclType aclType;

}
