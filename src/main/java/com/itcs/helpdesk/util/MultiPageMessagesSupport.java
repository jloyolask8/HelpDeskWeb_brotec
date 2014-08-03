
package com.itcs.helpdesk.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class MultiPageMessagesSupport implements PhaseListener {

private static final long serialVersionUID = 1250469273857785274L;
private static final String sessionToken = "MULTI_PAGE_MESSAGES_SUPPORT";

@Override
public PhaseId getPhaseId() {
    return PhaseId.ANY_PHASE;
}

/*
 * Check to see if we are "naturally" in the RENDER_RESPONSE phase. If we
 * have arrived here and the response is already complete, then the page is
 * not going to show up: don't display messages yet.
 */
@Override
public void beforePhase(final PhaseEvent event) {
    FacesContext facesContext = event.getFacesContext();
    int msg = this.saveMessages(facesContext);

    if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
        if (!facesContext.getResponseComplete()) {
            this.restoreMessages(facesContext);
        }
    }
}

/*
 * Save messages into the session after every phase.
 */
@Override
public void afterPhase(final PhaseEvent event) {
    if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES ||
            event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS ||
            event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
        FacesContext facesContext = event.getFacesContext();
        int msg = this.saveMessages(facesContext);
    }
}

@SuppressWarnings("unchecked")
private int saveMessages(final FacesContext facesContext) {
    List<FacesMessage> messages = new ArrayList<FacesMessage>();
    for (Iterator<FacesMessage> iter = facesContext.getMessages(null); iter.hasNext();) {
        messages.add(iter.next());
        iter.remove();
    }

    if (messages.isEmpty()) {
        return 0;
    }

    Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
    List<FacesMessage> existingMessages = (List<FacesMessage>) sessionMap.get(sessionToken);
    if (existingMessages != null) {
        existingMessages.addAll(messages);
    } else {
        sessionMap.put(sessionToken, messages);
    }
    return messages.size();
}

@SuppressWarnings("unchecked")
private int restoreMessages(final FacesContext facesContext) {
    Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
    List<FacesMessage> messages = (List<FacesMessage>) sessionMap.remove(sessionToken);

    if (messages == null) {
        return 0;
    }

    int restoredCount = messages.size();
    for (Object element : messages) {
        facesContext.addMessage(null, (FacesMessage) element);
    }
    return restoredCount;
}
}