/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.ejb.notifications;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author jonathan
 */
@Stateless
public class EventNotifier {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @Inject
    Event<NotificationData> notificationEvents;

    public void fire(NotificationData event) {
        notificationEvents.fire(event);
    }
    
}
