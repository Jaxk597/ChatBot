package com.mycompany.echo;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class Problems {

    //    private final com.microsoft.bot.builder.UserState userState;
//    protected final BotState conversationState;
    private static final String LOGIN = "Probleme mit der Anmeldung";
    private static final String SERVER = "Server-spezifische Probleme";
    private static final String PROGRAM = "Probleme mit einem unserer Programme";
    private static final String CONTRACT = "Vertragsanliegen";
    private static final String PROJECT = "Projekt-/Mitarbeiteranliegen";
    private static final String MITARBEITER = "Mitarbeiteranliegen";


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


    // INTRO
    CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Willkommen im Kunden-Chatsupport!");
        card.setText("Hallo! Ich unterstütze Sie bei support-spezifischen Fragen. Welches Anliegen haben Sie?");

        List<String> actions = new ArrayList<>();
        actions.add(LOGIN);
        actions.add(SERVER);
        actions.add(PROGRAM);
        actions.add(CONTRACT);
        actions.add(PROJECT);
        actions.add(MITARBEITER);
        List<CardAction> cardActions = actions.stream().map(this::returnNewCardAction).collect(Collectors.toList());

        card.setButtons(cardActions);

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    //Mitarbeiter END Card
    protected CompletableFuture<ResourceResponse> sendOtherCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Bitte Melden sie sich bei einem Mitarbeiter!");
        card.setText(
                "Sie können einen unserer Mitarbeiter erreichen über Email:"
                        + " itsupport@unternehmen.com oder über Telefon: "
                        + "0900/1234567890"
        );
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }



    //Standard END Card
    protected CompletableFuture<ResourceResponse> sendEndCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Vielen Dank");
        card.setText("Wir hoffen, das wir ihnen weiterhelfen konnten");
        Activity response = MessageFactory.attachment((card.toAttachment()));
        return turnContext.sendActivity(response);


    }

    //Dialog für Mitarbeiter
    protected CompletableFuture<ResourceResponse> sendWorkerCard(TurnContext turnContext) {
        control = 1;
        HeroCard card = new HeroCard();
        card.setTitle("Probleme mit unseren Mitarbeitern?");
        card.setText(
                "Sie können..."
        );

        CardImage image = new CardImage();
        image.setUrl("https://aka.ms/bf-welcome-card-image");

        card.setImages(Collections.singletonList(image));

        CardAction talkAction = new CardAction();
        talkAction.setType(ActionTypes.MESSAGE_BACK);
        talkAction.setTitle("Mit Mitarbeiter Reden");
        talkAction.setText("ander");
        talkAction.setDisplayText("Mit Mitarbeiter Reden");
        talkAction.setValue("ander");

        CardAction lobAction = new CardAction();
        lobAction.setType(ActionTypes.MESSAGE_BACK);
        lobAction.setTitle("Einen Mitarbeiter loben/bescherde einreichen");
        lobAction.setText("Rueckmeldung1");
        lobAction.setDisplayText("Einen Mitarbeiter loben/bescherde einreichen");
        lobAction.setValue("Rueckmeldung1");

        card.setButtons(Arrays.asList(talkAction, lobAction));

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    //Initialisierung von der Rückmeldung
    protected CompletableFuture<ResourceResponse> sendAnswerCard(TurnContext turnContext) {
        control = 100;
        Activity response = MessageFactory.text("");
        return turnContext.sendActivity(response);
    }


    // Für die Antwort auf die Rückmeldung / Noch in Datei Schreiben Lassen!!!
    protected CompletableFuture<ResourceResponse> sendResponseAnswerCard(TurnContext turnContext) {
        Writer fileWriter;
        File file;
        try {
            file = new File("Rueckmeldung.txt");
            fileWriter = new FileWriter("Rueckmeldung.txt");
            fileWriter.write(turnContext.getActivity().getText());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HeroCard card = new HeroCard();
        card.setTitle("Vielen Dank für ihre Rückmeldung!");
        card.setText("Wir freuen uns über ihre Rückmeldung über unseren Service");
        Activity response = MessageFactory.attachment((card.toAttachment()));
        return turnContext.sendActivity(response);
    }

    protected static CompletableFuture<ResourceResponse> sendIdCard(TurnContext turnContext) {
        Activity response = null;
        return turnContext.sendActivity(response);
    }



    protected int getControl () {
           return  this.control;
    }

    protected void setControl(int control) {
           this.control = control;
    }
}
