package com.mycompany.echo;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class Problems {

    private static final String MITARBEITER2 = "talk to an employee.";
    private static final String MITARBEITER3 = "commend/make a complaint.";


    private int control = 0;


    private CardAction returnNewCardAction(String type) {
        CardAction action = new CardAction();
        action.setType(ActionTypes.MESSAGE_BACK);
        action.setTitle(type);
        action.setText(type);
        action.setDisplayText(type);
        action.setValue(type);
        return action;
    }

    //Mitarbeiter END Card
    protected CompletableFuture<ResourceResponse> sendOtherCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setText("Please reach out to our support team if you have a complex issue.\n" +
                "Mo - Sa from 8am - 8pm : +49 221 - 6392678\n" +
                "or via e-mail: support@-solutionsit.de"
        );
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    //Dialog für Mitarbeiter
    protected CompletableFuture<ResourceResponse> sendWorkerCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("You have an issue about one of our employee?");
        card.setText(
                "You can..."
        );

        List<String> actions = new ArrayList<>();
        actions.add(MITARBEITER2);
        actions.add(MITARBEITER3);
        List<CardAction> cardActions = new ArrayList<>();
        for (String action : actions) {
            CardAction cardAction = returnNewCardAction(action);
            cardActions.add(cardAction);
        }
        card.setButtons(cardActions);
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    //Initialisierung von der Rückmeldung
    protected CompletableFuture<ResourceResponse> sendAnswerCard(TurnContext turnContext) {
        control = 100;
        Activity response = MessageFactory.text("Please enter your message now. We will make sure that it reaches the right department.");
        return turnContext.sendActivity(response);
    }

    // Für die Antwort auf die Rückmeldung
    protected CompletableFuture<ResourceResponse> sendResponseAnswerCard(TurnContext turnContext) {
        Writer fileWriter;
        File file;
        try {
            file = new File("feedback.txt");
            fileWriter = new FileWriter("feedback.txt");
            fileWriter.write(turnContext.getActivity().getText());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HeroCard card = new HeroCard();
        card.setTitle("Thank you for your feedback!");
        card.setText("We work on our service continuously.");
        Activity response = MessageFactory.attachment((card.toAttachment()));
        return turnContext.sendActivity(response);
    }

    protected int getControl() {
        return this.control;
    }
}