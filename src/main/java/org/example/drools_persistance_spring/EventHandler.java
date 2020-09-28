package org.example.drools_persistance_spring;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Slf4j
public class EventHandler implements RuleRuntimeEventListener {
    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        log.info(getRuleName(event.getRule()) + " inserted:\n" + event.getObject());
    }

    private String getRuleName(Rule rule) {
        String ruleName = "Manually";
        if(rule != null && rule.getName() != null) {
            ruleName = rule.getName();
        }
        return ruleName;
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        log.info(getRuleName(event.getRule()) + " updated:\n" + event.getObject());
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        log.info(getRuleName(event.getRule()) + " deleted:\n" + event.getOldObject());
    }
}
