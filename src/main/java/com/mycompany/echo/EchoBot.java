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
public class EchoBot extends ActivityHandler {

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
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
