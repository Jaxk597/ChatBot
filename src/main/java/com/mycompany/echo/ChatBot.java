// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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
    protected final Dialog dialog;
    private static final String LOGIN = "Probleme mit der Anmeldung";
    private static final String SERVER = "Server-spezifische Probleme";
    private static final String PROGRAM = "Probleme mit einem unserer Programme";
    private static final String CONTRACT = "Vertragsanliegen";
    private static final String PROJECT = "Projekt-/Mitarbeiteranliegen";
    private static final String PASSWORD = "Passwort vergessen";
    private static final String USERNAME = "Benutzername vergessen";
    private static final String OTHER = "Anderes Problem";
    private static final String REPLY_OTHER = "Bitte wenden Sie sich bei komplexeren Anliegen direkt an einen unserer Mitarbeiter.\n" +
            "Telefonisch werktags von 8 - 20 Uhr: 0221 - 6392678\n" +
            "oder per E-Mail an: support@itech-bs14.de";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final String MITARBEITER2 = "Mit Mitarbeiter Reden";
    private static final String MITARBEITER3 = "Einen Mitarbeiter loben/bescherde einreichen";



    Problems problems = new Problems();

    @Autowired
    public ChatBot(ConversationState withConversationState, UserState withUserState, Dialog withDialog) {
        userState = withUserState;
        conversationState = withConversationState;
        dialog = withDialog;
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

        List<String> actions = Arrays.asList(LOGIN, SERVER, PROGRAM, CONTRACT, PROJECT, OTHER);

        List<CardAction> cardActions = actions.stream().map(this::returnNewCardAction).collect(Collectors.toList());

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

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {

        String userInput = turnContext.getActivity().getText();

        switch (userInput) {
            case LOGIN: {
                return onLoginProblems(turnContext);
            }
            case PASSWORD:
            case USERNAME:
                return onLoginProblems(turnContext, userInput);
            case SERVER:
                return onServerProblems(turnContext);
            case PROGRAM:
            case "Kuendigen":
            case "Aendern/Anpassen":
            case "Anderes":
                enterVertragsanliegenAttributes(turnContext);
            case CONTRACT:
                return enterVertragsanliegen(turnContext);
            case PROJECT:
                return problems.sendWorkerCard(turnContext).thenApply(result -> null);
            case OTHER:
                return turnContext.sendActivity(REPLY_OTHER).thenApply(result -> null);
            case MITARBEITER2:
                return problems.sendOtherCard(turnContext).thenApply(result -> null);
            case MITARBEITER3:
                return problems.sendAnswerCard(turnContext).thenApply(result -> null);
            default:
                if (isValidMailAddress(userInput)) {
                    turnContext.sendActivity("Vielen Dank! Bitte überprüfen Sie Ihr E-Mail-Postfach.");
                } else if (userInput.contains("1234")) {
                    onServerProblems(turnContext);
                    if (isValidMailAddress(userInput)) {
                        turnContext.sendActivity("Vielen Dank! Bitte überprüfen Sie Ihr E-Mail-Postfach.");
                    } else if (userInput.contains("1234")) {
                        onServerProblems(turnContext);
                    } else if (problems.getControl() == 100) {
                        return problems.sendResponseAnswerCard(turnContext).thenApply(result -> null);
                    } else {
                        turnContext.sendActivity("Ungültige Eingabe.");
                    }
                }
        }
        return null;
    }

    private CompletableFuture<Void> onLoginProblems(TurnContext turnContext) {

        Activity reply = MessageFactory.text("Bitte wählen Sie Ihr Problem: ");

        CardAction passwordAction = new CardAction();
        passwordAction.setTitle(PASSWORD);
        passwordAction.setType(ActionTypes.IM_BACK);
        passwordAction.setValue(PASSWORD);

        CardAction usernameAction = new CardAction();
        usernameAction.setTitle(USERNAME);
        usernameAction.setType(ActionTypes.IM_BACK);
        usernameAction.setValue(USERNAME);

        SuggestedActions actions = new SuggestedActions();
        actions.setActions(Arrays.asList(passwordAction, usernameAction));
        reply.setSuggestedActions(actions);
        return turnContext.sendActivity(reply).thenApply(sendResult -> null);
    }

    private CompletableFuture<Void> onLoginProblems(TurnContext turnContext, String text) {

        switch (text) {
            case PASSWORD:
                return turnContext.sendActivity("Bitte geben Sie Ihre E-Mailadresse an, mit der Sie bei uns registriert sind. " +
                        "Sie erhalten dann in Kürze eine E-Mail zum Zurücksetzen Ihres Passworts.").thenApply(result -> null);
            case USERNAME:
                return turnContext.sendActivity("Bitte geben Sie Ihre E-Mailadresse an, mit der Sie bei uns registriert sind. " +
                        "Sie erhalten dann in Kürze eine E-Mail mit Ihren Benutzerdaten.").thenApply(result -> null);
            case OTHER:
                return turnContext.sendActivity(REPLY_OTHER).thenApply(r -> null);
            default:
                return turnContext.sendActivity("Ungültige Eingabe.").thenApply(r -> null);
        }
    }

    public CompletableFuture<Void> onServerProblems(TurnContext turnContext) {
        return Dialog.run(dialog, turnContext, conversationState.createProperty("DialogState"));
    }

    public static boolean isValidMailAddress(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private CompletableFuture<Void> enterVertragsanliegen(TurnContext turnContext) {
        //onTurn(turnContext);
        String text = turnContext.getActivity().getText();
        if (text.equals(CONTRACT)) {
            HeroCard card = new HeroCard();
            card.setText("Bitte wählen Sie eine der folgenden Optionen: ");
            List<String> actions = new ArrayList<>();
            actions.add("Kuendigen");
            actions.add("Aendern/Anpassen");
            actions.add("Anderes");
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


    private void enterVertragsanliegenAttributes(TurnContext turnContext) {
        if (turnContext.getActivity().getText().equals("Kuendigen")) {
            turnContext.sendActivity(MessageFactory.text("Schade, dass Sie uns verlassen wollen. Hier finden Sie das Kuendigungsformular, " +
                    "es wird anschließend an unseren Support weitergeleitet: \" +\n" +
                    "                \"www.solutionsgmbh.de/kuendigung/sendonsave\""));
        } else if (turnContext.getActivity().getText().equals("Aendern/Anpassen")) {
            turnContext.sendActivity(MessageFactory.text("Sie können Sie Ihre Vertagsdatenändern. Ihre ID lautet: 1191112. " +
                    " \" www.solutionsgmbh.de/changeContractData"));
        } else if (turnContext.getActivity().getText().equals("Anderes")) {
            turnContext.sendActivity(REPLY_OTHER);
        }
    }
}