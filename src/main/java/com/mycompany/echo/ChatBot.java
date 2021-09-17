// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
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

    private final com.microsoft.bot.builder.UserState userState;
    protected final BotState conversationState;
    private final Dialog dialog;
    private static final String LOGIN = "Probleme mit der Anmeldung";
    private static final String SERVER = "Server-spezifische Probleme";
    private static final String PROGRAM = "Probleme mit einem unserer Programme";
    private static final String CONTRACT = "Vertragsanliegen";
    private static final String PROJECT = "Projekt-/Mitarbeiteranliegen";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    @Autowired
    public ChatBot(com.microsoft.bot.builder.UserState withUserState, ConversationState withConversationState, Dialog withDialog) {
        userState = withUserState;
        conversationState = withConversationState;
        dialog = withDialog;
    }

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

    private CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Willkommen im Kunden-Chatsupport!");
        card.setText("Hallo! Ich unterstütze Sie bei support-spezifischen Fragen. Welches Anliegen haben Sie?");

        List<String> actions = new ArrayList<>();
        actions.add(LOGIN);
        actions.add(SERVER);
        actions.add(PROGRAM);
        actions.add(CONTRACT);
        actions.add(PROJECT);

        List<CardAction> cardActions = new ArrayList<>();
        for (String action : actions) {
            CardAction cardAction = returnNewCardAction(action);
            cardActions.add(cardAction);
        }

        card.setButtons(cardActions);

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    private CardAction returnNewCardAction(String type) {
        CardAction action = new CardAction();
        action.setType(ActionTypes.MESSAGE_BACK);
        action.setTitle(type);
        action.setText(type);
        action.setDisplayText(type);
        action.setValue(type);
        return action;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        // Get state data from UserState.
        /*StatePropertyAccessor<UserState> stateAccessor =
                userState.createProperty("NewUserState");
        CompletableFuture<UserState> stateFuture =
                stateAccessor.get(turnContext, () -> new UserState());*/

        String usereingabe = turnContext.getActivity().getText();

        switch (usereingabe) {
            case "Passwort vergessen":
            case "Benutzername vergessen":
                enterAnmeldeDatenVergessen(turnContext);
            case LOGIN:
                return enterAnmeldeProbleme(turnContext);
            case SERVER:
                return onTurn(turnContext);
            case PROGRAM:
                //return turnContext.sendActivity("[IMPLEMENTIERUNG PROGRAM]");
            case CONTRACT:
                //return turnContext.sendActivity("[IMPLEMENTIERUNG CONTRACT]");
            case PROJECT:
                //return turnContext.sendActivity("[IMPLEMENTIERUNG PROJECT]");
            default:
                if (mailAddressIsValid(turnContext.getActivity().getText())){
                    turnContext.sendActivity("Es hat geklappt. Sie erhalten in Kürze eine Mail.");
                    sendIntroCard(turnContext);
                }
                else{
                    turnContext.sendActivity("Ungültige Eingabe.");
                }
                return null;
            //return turnContext.sendActivity("Ungültige Eingabe. Bitte wählen Sie ein Anliegen aus.");
        }
    }

    private CompletableFuture<Void> enterAnmeldeProbleme(TurnContext turnContext) {
        //onTurn(turnContext);
        String text = turnContext.getActivity().getText();
        if (text.equals(LOGIN)) {
            HeroCard card = new HeroCard();
            card.setText("Bitte wählen Sie eine der folgenden Optionen: ");
            List<String> actions = new ArrayList<>();
            actions.add("Passwort vergessen");
            actions.add("Benutzername vergessen");
            List<CardAction> cardActions = new ArrayList<>();
            for (String action : actions) {
                CardAction cardAction = returnNewCardAction(action);
                cardActions.add(cardAction);
            }
            card.setButtons(cardActions);

            Activity response = MessageFactory.attachment(card.toAttachment());
            turnContext.sendActivity(response);
        }
        return null;
    }

    private void enterAnmeldeDatenVergessen(TurnContext turnContext) {
        if (turnContext.getActivity().getText().equals("Passwort vergessen")){
            turnContext.sendActivity(MessageFactory.text("Bitte geben Sie Ihre E-Mailadresse an, mit der Sie bei uns registriert sind. " +
                    "Sie erhalten dann in Kürze eine E-Mail zum Zurücksetzen Ihres Passworts."));
        }
        else{
            turnContext.sendActivity(MessageFactory.text("Bitte geben Sie Ihre E-Mailadresse an, mit der Sie bei uns registriert sind. " +
                    "Sie erhalten dann in Kürze eine E-Mail mit Ihren Benutzerdaten."));
        }
    }

    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
                .thenCompose(result -> conversationState.saveChanges(turnContext))
                // Save any state changes that might have occurred during the turn.
                .thenCompose(result -> userState.saveChanges(turnContext));
    }

    public static boolean mailAddressIsValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }


}