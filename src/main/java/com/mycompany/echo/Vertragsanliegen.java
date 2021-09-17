// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class Vertragsanliegen extends ActivityHandler {

    private int control = 0;

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        String text = turnContext.getActivity().getText().toLowerCase();

        if (text.matches(".*vertragsanliegen.*")) {
            return turnContext
                    .sendActivity(MessageFactory.text("Sie haben Vertragsanliegen gewaehlt."))
                    .thenCompose(response -> continueVertragsdetails(turnContext))
                    .thenCompose(response -> vertragsdetails(turnContext))
                    .thenCompose(response -> continueKuendigungOderAnpassen(turnContext))
                    .thenApply(result -> null);
        }
        if (text.matches(".*kuendigung.*")) {
            return turnContext
                    .sendActivity(MessageFactory.text(""))
                    .thenCompose(response -> sendKuendigung(turnContext))
                    .thenApply(result -> null);
        }

        return turnContext.sendActivity(
                MessageFactory.text("Echo: " + turnContext.getActivity().getText())
        ).thenApply(sendResult -> null);
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext
    ) {
        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> turnContext.sendActivity(MessageFactory.text("Hello and welcome!")))
                .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }

    private CompletableFuture<ResourceResponse> continueVertragsdetails(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Sie haben Vertragsanliegen gewaehlt.");
        card.setText(
                "Zunächst benötigen wir Ihre Vertragsdaten "
        );
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    private CompletableFuture<ResourceResponse> vertragsdetails(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Vertragsdetails");
        card.setText(
                "Max Mustermann, Musterstraße 11, 10099 Musterstadt " + "Kuendigungsdatum 01.01.2022"
                        + " Danke, wir haben die Vertragsdaten erhalten. Nun können wir weiter machen. "
        );
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    private CompletableFuture<ResourceResponse> continueKuendigungOderAnpassen(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Waehlen Sie nun aus folgenden Punkten: ");
        CardAction kuendigung = new CardAction();
        kuendigung.setType(ActionTypes.MESSAGE_BACK);
        kuendigung.setTitle("Kuendigung");
        kuendigung.setText("Schade, dass Sie uns verlassen wollen. Hier finden Sie das Kuendigungsformular, es wird anschließend an unseren Support weitergeleitet: " +
                "www.solutionsgmbh.de/kuendigung/sendonsave");
        card.setButtons(kuendigung);
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }


    private CompletableFuture<ResourceResponse> sendKuendigung(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Sie haben Kuendigung gewaehlt.");
        card.setText(
                "Bitte gebe deinen Kuendigungsgrund an. Das Kuendigungsformular findest du hier: " + ""
        );
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    private CompletableFuture<ResourceResponse> continueKuendigung(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Vielen Dank");
        card.setText("Wir hoffen, das wir ihnen weiterhelfen konnten");
        Activity responce = MessageFactory.attachment((card.toAttachment()));
        return turnContext.sendActivity(responce);


    }

    private CompletableFuture<ResourceResponse> sendEndCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Vielen Dank");
        card.setText("Wir hoffen, das wir ihnen weiterhelfen konnten");
        Activity responce = MessageFactory.attachment((card.toAttachment()));
        return turnContext.sendActivity(responce);


    }

    private CompletableFuture<ResourceResponse> sendVertragsanliegen(TurnContext turnContext) {
        control = 1;
        HeroCard card = new HeroCard();
        card.setTitle("Vertragsanliegen");
        card.setText(
                "Nennen Sie zunächst ihre Vertrags/Projektdetails"
        );

        CardImage image = new CardImage();
        image.setUrl("https://aka.ms/bf-welcome-card-image");

        card.setImages(Collections.singletonList(image));

        CardAction talkAction = new CardAction();
        talkAction.setType(ActionTypes.MESSAGE_BACK);
        talkAction.setTitle("Vertragsdetails");
        CardAction deinemudda = new CardAction();
        deinemudda.setType(ActionTypes.MESSAGE_BACK);
        deinemudda.setTitle("Projektdetails");
        deinemudda.setDisplayText("Projektdetails");
        deinemudda.setText("Ihre ProjektdetailsID lautet: 29999291");
        deinemudda.setValue("Ihre ProjektdetailsID lautet: 29999291");
        card.setButtons(Arrays.asList(talkAction, deinemudda));
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }


    // COPY PASTE"

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
