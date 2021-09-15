// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */
public class EchoBot extends ActivityHandler {

    private int control = 0;

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext){
        String text = turnContext.getActivity().getText().toLowerCase();

        if (control == 100){
            return  turnContext
                    .sendActivity(MessageFactory.text(""))
                    .thenCompose(response -> sendResponseAnswerCard(turnContext))
                    .thenApply(result -> null);
        }

        //Papers Please!
        if (control == 597) {
            return turnContext
                    .sendActivity(MessageFactory.text(""))
                    .thenCompose(response -> sendIdCard(turnContext))
                    .thenApply(result -> null);
        }

        if (text.equals("145213571892")) {
            return turnContext
                    .sendActivity(MessageFactory.text("Hehe i am so smart boyo"))
                    .thenCompose(response -> sendEndCard(turnContext))
                    .thenApply(result -> null);
            }

        if (text.equals("rueckmeldung1")) {
            return turnContext
                    .sendActivity(MessageFactory.text("Schreiben sie hier einfach ihre Rückmeldung, wir leiten sie daraufhin an unseren Mitarbeiter weiter"))
                    .thenCompose(response -> sendAnswerCard(turnContext))
                    .thenApply(sendResult -> null);
        }
        //Mitarbeiterproblem
        if(text.matches(".*arbeiter.*"))
        {
            return turnContext.sendActivity(
                    MessageFactory.text("Mitarbeiterproblem"))
                    .thenCompose(response -> sendWorkerCard(turnContext))
                    .thenApply(sendResult -> null);
        }
        // Anders Problem / Mit Mitarbeiter Sprechen
        if (text.matches(".*sprech.*")
                || text.matches(".*ander.*") )
        {
            return turnContext
                    .sendActivity(MessageFactory.text(""))
                    .thenCompose(response -> sendOtherCard(turnContext))
                    .thenCompose(response -> sendEndCard(turnContext))
                    .thenApply(result -> null);
        }

        // ECHOOOOOOOO
        return turnContext.sendActivity(
            MessageFactory.text("Echo: " + turnContext.getActivity().getText())
        ).thenApply(sendResult -> null);
    }

    // Wilkommensnachricht, standard bei jedem chatfenster
    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            ).map(channel -> turnContext.sendActivity(MessageFactory.text("Hallo, wie Kann man ihnen weiterhelfen?")))
            .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }

    //copy paste "

    //        turnContext.setLocale("locale");
    //        turnContext.getLocale();
    //        Overwrite von Eingabe (sehr gefährlich
    //        turnContext.getActivity().setText("mitarbeiter");



    //Mitarbeiter END Card
    private CompletableFuture<ResourceResponse> sendOtherCard(TurnContext turnContext) {
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
    private CompletableFuture<ResourceResponse> sendEndCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Vielen Dank");
        card.setText("Wir hoffen, das wir ihnen weiterhelfen konnten");
        Activity response = MessageFactory.attachment((card.toAttachment()));
        return  turnContext.sendActivity(response);


    }

    //Dialog für Mitarbeiter
    private CompletableFuture<ResourceResponse> sendWorkerCard(TurnContext turnContext) {
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
    private CompletableFuture<ResourceResponse> sendAnswerCard(TurnContext turnContext) {
        control = 100;
        Activity response = MessageFactory.text("");
        return turnContext.sendActivity(response);
    }


    // Für die Antwort auf die Rückmeldung / Noch in Datei Schreiben Lassen!!!
    private CompletableFuture<ResourceResponse> sendResponseAnswerCard(TurnContext turnContext) {
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

    private CompletableFuture<ResourceResponse> sendIdCard(TurnContext turnContext) {
        Activity response = null;
        return turnContext.sendActivity(response);
    }



// ---------------------------------------------------------------------------------------------------------------------
    // COPY PASTE"          DAS DARUNTER NOCH ENTFERNEN KAPPA
// ---------------------------------------------------------------------------------------------------------------------
    private CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Welcome to Bot Framework!");
        card.setText(
                "Welcome to Welcome Users bot sample! This Introduction card "
                        + "is a great way to introduce your Bot to the user and suggest "
                        + "some things to get them started. We use this opportunity to "
                        + "recommend a few next steps for learning more creating and deploying bots."
        );

        CardImage image = new CardImage();
        image.setUrl("https://aka.ms/bf-welcome-card-image");

        card.setImages(Collections.singletonList(image));

        CardAction overviewAction = new CardAction();
        overviewAction.setType(ActionTypes.OPEN_URL);
        overviewAction.setTitle("Get an overview");
        overviewAction.setText("Get an overview");
        overviewAction.setDisplayText("Get an overview");
        overviewAction.setValue(
                "https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0"
        );

        CardAction questionAction = new CardAction();
        questionAction.setType(ActionTypes.OPEN_URL);
        questionAction.setTitle("Ask a question");
        questionAction.setText("Ask a question");
        questionAction.setDisplayText("Ask a question");
        questionAction.setValue("https://stackoverflow.com/questions/tagged/botframework");

        CardAction deployAction = new CardAction();
        deployAction.setType(ActionTypes.OPEN_URL);
        deployAction.setTitle("Learn how to deploy");
        deployAction.setText("Learn how to deploy");
        deployAction.setDisplayText("Learn how to deploy");
        deployAction.setValue(
                "https://docs.microsoft.com/en-us/azure/bot-service/bot-builder-howto-deploy-azure?view=azure-bot-service-4.0"
        );
        card.setButtons(Arrays.asList(overviewAction, questionAction, deployAction));

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }
}
