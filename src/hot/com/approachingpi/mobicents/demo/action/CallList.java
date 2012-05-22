package com.approachingpi.mobicents.demo.action;

import com.approachingpi.mobicents.demo.model.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("callList")
public class CallList extends EntityQuery<PhoneCall> {

	private static final String EJBQL = "select call from PhoneCall call";

	private static final String[] RESTRICTIONS = {
			"lower(call.dtmf) like lower(concat(#{callList.call.dtmf},'%'))",
			"lower(call.from) like lower(concat(#{callList.call.from},'%'))",
			"lower(call.to) like lower(concat(#{callList.call.to},'%'))",};

	private PhoneCall call = new PhoneCall();

	public CallList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public PhoneCall getCall() {
		return call;
	}
}
