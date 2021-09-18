// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;


/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */
    public class ChatBot extends ActivityHandler {
    protected final BotState conversationState;
    private final com.microsoft.bot.builder.UserState userState;
    private static final String LOGIN = "Probleme mit der Anmeldung";
    private static final String SERVER = "Server-spezifische Probleme";
    private static final String PROGRAM = "Probleme mit einem unserer Programme";
    private static final String CONTRACT = "Vertragsanliegen";
    private static final String PROJECT = "Projekt-/Mitarbeiteranliegen";
    private static final String MITARBEITER = "Mitarbeiteranliegen";
    private static final String MITARBEITER2 = "Mit Mitarbeiter Reden";
    private static final String MITARBEITER3 = "Einen Mitarbeiter loben/bescherde einreichen";
    private static final String ANDERES = "Anderes Problem";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    Problems problems = new Problems();

    @Autowired
    public ChatBot(com.microsoft.bot.builder.UserState withUserState, ConversationState withConversationState) {
        userState = withUserState;
        conversationState = withConversationState;
    }


//
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        // Get state data from UserState.
//        StatePropertyAccessor<UserState> stateAccessor =
//                UserState.createProperty("NewUserState");
//        CompletableFuture<UserState> stateFuture =
//                stateAccessor.get(turnContext, UserState::new);

        return turnContext
                .sendActivity(MessageFactory.text(""))

//                stateFuture
                        .thenApply(thisUserState -> {
                    if (problems.getControl() == 100){
                        problems.setControl(0);
                        return problems.sendResponseAnswerCard(turnContext);
                    }
                    // This example hard-codes specific utterances.
                    // You should use LUIS or QnA for more advance language understanding.
                    String text = turnContext.getActivity().getText();
                    switch (text) {
                        case LOGIN: return turnContext.sendActivity("[IMPLEMENTIERUNG LOGIN]");
                        case SERVER: return onTurn(turnContext);
                        case PROGRAM: return turnContext.sendActivity("[IMPLEMENTIERUNG PROGRAM]");
                        case CONTRACT: return turnContext.sendActivity("[IMPLEMENTIERUNG CONTRACT]");
                        case PROJECT: return turnContext.sendActivity("[IMPLEMENTIERUNG PROJECT]");
                        case MITARBEITER: return problems.sendWorkerCard(turnContext);
                        case MITARBEITER2: return problems.sendOtherCard(turnContext);
                        case MITARBEITER3: return problems.sendAnswerCard(turnContext);
                        case ANDERES: return problems.sendOtherCard(turnContext);
                        default:
                            return turnContext.sendActivity("Ungültige Eingabe. Bitte wählen Sie ein Anliegen aus.");
                    }
                })
                // Save any state changes.
//                .thenApply(response -> userState.saveChanges(turnContext))
                // make the return value happy.
                .thenApply(task -> null);
    }



    // Wilkommensnachricht, standard bei jedem chatfenster
    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext
    ) {
        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> sendIntroCard(turnContext))
                .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
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
        actions.add(ANDERES);
        List<CardAction> cardActions = new ArrayList<>();
        for (String action : actions) {
            CardAction cardAction = returnNewCardAction(action);
            cardActions.add(cardAction);
        }
        card.setButtons(cardActions);
        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }


    //ALlgemeiner Header für CardActions... sehr vereinfacht und übersichtlich gemacht
    private CardAction returnNewCardAction(String type) {
        CardAction action = new CardAction();
        action.setType(ActionTypes.MESSAGE_BACK);
        action.setTitle(type);
        action.setText(type);
        action.setDisplayText(type);
        action.setValue(type);
        return action;
    }


}





//    @Override
//    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
//        String text = turnContext.getActivity().getText().toLowerCase();
//
//        if (problems.getControl() == 100) {
//            return turnContext
//                    .sendActivity(MessageFactory.text(""))
//                    .thenCompose(response -> problems.sendResponseAnswerCard(turnContext))
//                    .thenApply(result -> null);
//        }
//
//        //Papers Please!
//        if (problems.getControl() == 597) {
//            return turnContext
//                    .sendActivity(MessageFactory.text(""))
//                    .thenCompose(response -> problems.sendIdCard(turnContext))
//                    .thenApply(result -> null);
//        }
//
//        if (text.equals("145213571892")) {
//            return turnContext
//                    .sendActivity(MessageFactory.text("Hehe i am so smart boyo"))
//                    .thenCompose(response -> problems.sendEndCard(turnContext))
//                    .thenApply(result -> null);
//        }
//
//        if (text.equals("rueckmeldung1")) {
//            return turnContext
//                    .sendActivity(MessageFactory.text("Schreiben sie hier einfach ihre Rückmeldung, wir leiten sie daraufhin an unseren Mitarbeiter weiter"))
//                    .thenCompose(response -> problems.sendAnswerCard(turnContext))
//                    .thenApply(sendResult -> null);
//        }
//        //Mitarbeiterproblem
//        if (text.matches(".*arbeiter.*")) {
//            return turnContext.sendActivity(
//                            MessageFactory.text("Mitarbeiterproblem"))
//                    .thenCompose(response -> problems.sendWorkerCard(turnContext))
//                    .thenApply(sendResult -> null);
//        }
//        // Anders Problem / Mit Mitarbeiter Sprechen
//        if (text.matches(".*sprech.*")
//                || text.matches(".*ander.*")) {
//            return turnContext
//                    .sendActivity(MessageFactory.text(""))
//                    .thenCompose(response -> problems.sendOtherCard(turnContext))
//                    .thenCompose(response -> problems.sendEndCard(turnContext))
//                    .thenApply(result -> null);
//        }
//
//        // ECHOOOOOOOO
//        return turnContext.sendActivity(
//                MessageFactory.text("Echo: " + turnContext.getActivity().getText())
//        ).thenApply(sendResult -> null);
//    }




//copy paste "



//    @Override
//    public CompletableFuture<Void> onTurn(
//            TurnContext turnContext
//    ) {
//        return super.onTurn(turnContext)
//                .thenCompose(result -> conversationState.saveChanges(turnContext))
//                // Save any state changes that might have occurred during the turn.
//                .thenCompose(result -> userState.saveChanges(turnContext));
//    }
//}


//    @Override
//    public CompletableFuture<Void> onWorker(
//            TurnContext turnContext
//    ) {
//          return super.onWorker(turnContext)
//              .sendActivity(MessageFactory.text(""))
//              .thenCompose(response -> problems.sendOtherCard(turnContext))
//              .thenCompose(response -> problems.sendEndCard(turnContext))
//              .thenApply(result -> null);
//    }




// ---------------------------------------------------------------------------------------------------------------------
// COPY PASTE"          DAS DARUNTER NOCH ENTFERNEN KAPPA
// ---------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------


//public class ChatBot extends ActivityHandler {
//